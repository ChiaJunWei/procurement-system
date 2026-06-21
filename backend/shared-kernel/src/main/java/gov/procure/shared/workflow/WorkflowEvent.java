package gov.procure.shared.workflow;

import java.util.Objects;

/** An event that can drive a transition (e.g. SUBMIT, APPROVE). Open set — features add their own. */
public record WorkflowEvent(String name) {
    public WorkflowEvent {
        Objects.requireNonNull(name, "event name");
    }
    public static WorkflowEvent of(String name) {
        return new WorkflowEvent(name);
    }
}
