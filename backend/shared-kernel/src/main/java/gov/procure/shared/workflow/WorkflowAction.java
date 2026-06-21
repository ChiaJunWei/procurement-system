package gov.procure.shared.workflow;

/**
 * A side effect run during a transition (notify, assign task, publish event). Register as a Spring
 * bean whose name matches the {@code action} key in a WorkflowDefinition. Engine resolves by name.
 */
public interface WorkflowAction {
    void execute(WorkflowContext context);
}
