package gov.procure.procurement.domain;

/**
 * Lifecycle states. Mirrors the {@code purchase-requisition.approval} WorkflowDefinition;
 * transitions between these are owned by the Workflow engine, not by ad-hoc setters.
 */
public enum RequisitionStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    CANCELLED
}
