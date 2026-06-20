package gov.procure.shared.workflow;

/**
 * A named predicate gating a transition. Register as a Spring bean whose name matches the
 * {@code guard} key referenced in a WorkflowDefinition (e.g. "withinBudget"). Guards commonly
 * delegate to the Policy engine so authorization and routing share one rule base.
 */
public interface WorkflowGuard {

    boolean isSatisfied(WorkflowContext context);
}
