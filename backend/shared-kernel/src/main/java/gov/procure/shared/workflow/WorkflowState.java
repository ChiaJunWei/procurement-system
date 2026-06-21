package gov.procure.shared.workflow;

import java.util.Objects;

/** A named state in a workflow definition (e.g. DRAFT, PENDING_APPROVAL). */
public record WorkflowState(String name) {
    public WorkflowState {
        Objects.requireNonNull(name, "state name");
    }
    public static WorkflowState of(String name) {
        return new WorkflowState(name);
    }
}
