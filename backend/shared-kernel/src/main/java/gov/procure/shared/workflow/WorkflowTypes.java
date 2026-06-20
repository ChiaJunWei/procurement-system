package gov.procure.shared.workflow;

import java.util.List;
import java.util.UUID;

/**
 * Small supporting types for the workflow engine, grouped for brevity. In a fuller implementation
 * each may live in its own file; kept together here as part of the framework scaffold.
 */
public final class WorkflowTypes {
    private WorkflowTypes() {}
}

/** A named state in a workflow definition. */
record WorkflowState(String name) {}

/** An event that can drive a transition (e.g. SUBMIT, APPROVE). */
record WorkflowEvent(String name) {
    public static final WorkflowEvent SUBMIT = new WorkflowEvent("SUBMIT");
    public static final WorkflowEvent APPROVE = new WorkflowEvent("APPROVE");
    public static final WorkflowEvent REJECT = new WorkflowEvent("REJECT");
    public static final WorkflowEvent CANCEL = new WorkflowEvent("CANCEL");
}

/** Reference to a started instance. */
record WorkflowInstanceRef(UUID instanceId, UUID aggregateId, WorkflowState state) {}

/** Outcome of firing an event. */
record WorkflowResult(WorkflowState previousState, WorkflowState newState, List<String> firedActions) {}

/** Side effect run on a transition; resolved from the Spring registry by name. */
interface WorkflowAction {
    void execute(WorkflowContext context);
}
