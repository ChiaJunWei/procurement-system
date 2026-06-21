package gov.procure.procurement.config;

import gov.procure.shared.workflow.WorkflowAction;
import gov.procure.shared.workflow.WorkflowContext;
import gov.procure.shared.workflow.WorkflowDefinition;
import gov.procure.shared.workflow.WorkflowGuard;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the Procurement context's workflow: the {@code purchase-requisition.approval} definition
 * plus its named guards. This is the reference pattern — a new approval flow is a new definition
 * bean + guard beans, with zero changes to the engine.
 */
@Configuration
public class ProcurementWorkflowConfig {

    public static final String REQUISITION_APPROVAL = "purchase-requisition.approval";

    @Bean
    WorkflowDefinition purchaseRequisitionApproval() {
        return WorkflowDefinition.builder(REQUISITION_APPROVAL, 1)
            .initial("DRAFT")
            .state("PENDING_APPROVAL")
            .state("APPROVED")
            .state("REJECTED")
            .state("CANCELLED")
            .transition("SUBMIT", List.of("DRAFT"), "PENDING_APPROVAL",
                List.of("hasLineItems", "withinBudget"), List.of("notifyApprovers"))
            .transition("APPROVE", List.of("PENDING_APPROVAL"), "APPROVED",
                List.of("isAuthorizedApprover"), List.of("notifyRequester"))
            .transition("REJECT", List.of("PENDING_APPROVAL"), "REJECTED",
                List.of(), List.of("notifyRequester"))
            .transition("CANCEL", List.of("DRAFT", "PENDING_APPROVAL"), "CANCELLED",
                List.of(), List.of())
            .build();
    }

    /** Guard bean; resolved by name "hasLineItems" referenced in the definition above. */
    @Bean("hasLineItems")
    WorkflowGuard hasLineItems() {
        return (WorkflowContext ctx) -> ctx.attribute("lineItemCount")
            .map(c -> ((Number) c).intValue() > 0)
            .orElse(false);
    }

    /** Guard bean; delegates budget check to context attribute (could call the Policy engine). */
    @Bean("withinBudget")
    WorkflowGuard withinBudget() {
        return (WorkflowContext ctx) -> {
            BigDecimal amount = (BigDecimal) ctx.attribute("totalAmount").orElse(BigDecimal.ZERO);
            BigDecimal available = (BigDecimal) ctx.attribute("availableBudget")
                .orElse(amount); // default: assume budget present until Agency context is wired
            return amount.compareTo(available) <= 0;
        };
    }

    @Bean("isAuthorizedApprover")
    WorkflowGuard isAuthorizedApprover() {
        return (WorkflowContext ctx) -> ctx.attribute("approverAuthorized")
            .map(Boolean.class::cast)
            .orElse(false);
    }

    private static final Logger log = LoggerFactory.getLogger(ProcurementWorkflowConfig.class);

    /**
     * Action beans. These are intentionally simple hooks — a real build sends notifications via the
     * Integration context (events/email). Named to match the definition's action references.
     */
    @Bean("notifyApprovers")
    WorkflowAction notifyApprovers() {
        return ctx -> log.info("Notify approvers for requisition {}", ctx.aggregateId());
    }

    @Bean("notifyRequester")
    WorkflowAction notifyRequester() {
        return ctx -> log.info("Notify requester for requisition {}", ctx.aggregateId());
    }
}
