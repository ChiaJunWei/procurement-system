package gov.procure.procurement.application;

import gov.procure.procurement.domain.BudgetReference;
import gov.procure.procurement.domain.NewLineItem;
import gov.procure.procurement.domain.PurchaseRequisition;
import gov.procure.procurement.domain.PurchaseRequisitionId;
import gov.procure.procurement.domain.PurchaseRequisitionRepository;
import gov.procure.procurement.domain.RequisitionNumber;
import gov.procure.procurement.domain.UserId;
import gov.procure.shared.event.OutboxAppender;
import gov.procure.shared.tenant.TenantContext;
import gov.procure.shared.tenant.TenantId;
import gov.procure.shared.workflow.WorkflowContext;
import gov.procure.shared.workflow.WorkflowEngine;
import java.time.Clock;
import java.util.Currency;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use-case service for "Create Purchase Requisition" — the reference application service.
 *
 * <p>Transaction boundary lives here. In ONE transaction it:
 * <ol>
 *   <li>builds and validates the aggregate (invariants in the domain),</li>
 *   <li>persists it,</li>
 *   <li>starts its workflow instance,</li>
 *   <li>appends the integration event to the outbox (atomic with state).</li>
 * </ol>
 * Audit is handled downstream by the Audit context consuming the event — no audit code here.
 */
@Service
public class CreatePurchaseRequisitionService {

    private final PurchaseRequisitionRepository repository;
    private final OutboxAppender outboxAppender;
    private final WorkflowEngine workflowEngine;
    private final Clock clock;

    public CreatePurchaseRequisitionService(PurchaseRequisitionRepository repository,
                                            OutboxAppender outboxAppender,
                                            WorkflowEngine workflowEngine,
                                            Clock clock) {
        this.repository = repository;
        this.outboxAppender = outboxAppender;
        this.workflowEngine = workflowEngine;
        this.clock = clock;
    }

    @Transactional
    public PurchaseRequisitionId handle(CreatePurchaseRequisitionCommand command) {
        TenantId tenantId = TenantContext.require();
        Currency currency = Currency.getInstance(command.currency());

        List<NewLineItem> items = command.lineItems().stream()
            .map(l -> new NewLineItem(
                l.description(), l.quantity(), l.unitOfMeasure(), l.estimatedUnitPrice(),
                new BudgetReference(l.budgetId(), l.accountingCode())))
            .toList();

        RequisitionNumber number =
            repository.nextRequisitionNumber(clock.instant().atZone(clock.getZone()).getYear());

        PurchaseRequisition requisition = PurchaseRequisition.create(
            tenantId, number, new UserId(command.requesterId()),
            command.justification(), currency, items, clock);

        repository.save(requisition);

        // Start the lifecycle state machine (initial state DRAFT).
        workflowEngine.start("purchase-requisition.approval", requisition.id().value(),
            WorkflowContext.forAggregate(requisition.id().value(), tenantId));

        // Atomic state + event: outbox row committed in this same transaction.
        requisition.domainEvents().stream()
            .filter(e -> e instanceof gov.procure.shared.event.IntegrationEvent)
            .map(e -> (gov.procure.shared.event.IntegrationEvent) e)
            .forEach(outboxAppender::append);

        return requisition.id();
    }
}
