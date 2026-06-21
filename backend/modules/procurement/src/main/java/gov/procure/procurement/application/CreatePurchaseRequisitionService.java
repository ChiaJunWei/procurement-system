package gov.procure.procurement.application;

import gov.procure.procurement.config.ProcurementPolicyConfig;
import gov.procure.procurement.config.ProcurementWorkflowConfig;
import gov.procure.procurement.domain.BudgetReference;
import gov.procure.procurement.domain.NewLineItem;
import gov.procure.procurement.domain.PurchaseRequisition;
import gov.procure.procurement.domain.PurchaseRequisitionId;
import gov.procure.procurement.domain.PurchaseRequisitionRepository;
import gov.procure.procurement.domain.RequisitionNumber;
import gov.procure.procurement.domain.UserId;
import gov.procure.shared.event.IntegrationEvent;
import gov.procure.shared.event.OutboxAppender;
import gov.procure.shared.policy.PolicyEngine;
import gov.procure.shared.policy.PolicyRequest;
import gov.procure.shared.security.CurrentUser;
import gov.procure.shared.security.CurrentUserProvider;
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
 *   <li>enforces authorization/business rules via the Policy engine (PEP),</li>
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
    private final PolicyEngine policyEngine;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    public CreatePurchaseRequisitionService(PurchaseRequisitionRepository repository,
                                            OutboxAppender outboxAppender,
                                            WorkflowEngine workflowEngine,
                                            PolicyEngine policyEngine,
                                            CurrentUserProvider currentUserProvider,
                                            Clock clock) {
        this.repository = repository;
        this.outboxAppender = outboxAppender;
        this.workflowEngine = workflowEngine;
        this.policyEngine = policyEngine;
        this.currentUserProvider = currentUserProvider;
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

        // PEP: enforce authorization + business rules before persisting (fine-grained ABAC).
        enforceCreatePolicy(requisition, tenantId);

        repository.save(requisition);

        // Start the lifecycle state machine (initial state DRAFT).
        workflowEngine.start(ProcurementWorkflowConfig.REQUISITION_APPROVAL,
            requisition.id().value(),
            WorkflowContext.forAggregate(requisition.id().value(), tenantId)
                .with("lineItemCount", requisition.lineItems().size())
                .with("totalAmount", requisition.totalEstimatedCost().amount()));

        // Atomic state + event: outbox row(s) committed in this same transaction. Drain the
        // aggregate's registered events, then clear them so they are published exactly once.
        requisition.domainEvents().stream()
            .filter(IntegrationEvent.class::isInstance)
            .map(IntegrationEvent.class::cast)
            .forEach(outboxAppender::append);
        requisition.clearDomainEvents();

        return requisition.id();
    }

    private void enforceCreatePolicy(PurchaseRequisition requisition, TenantId tenantId) {
        CurrentUser user = currentUserProvider.require();
        PolicyRequest request = PolicyRequest.builder()
            .action(ProcurementPolicyConfig.ACTION_CREATE)
            .subject("id", user.id())
            .subject("agencyId", user.agencyId())
            .resource("tenantId", tenantId.value())
            .resource("requesterId", requisition.requesterId().value())
            .resource("amount", requisition.totalEstimatedCost().amount())
            .build();
        policyEngine.enforce(request); // throws PolicyViolationException (→ HTTP 403) if denied
    }
}
