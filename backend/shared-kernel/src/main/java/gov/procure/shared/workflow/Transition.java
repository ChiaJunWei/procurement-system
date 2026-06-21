package gov.procure.shared.workflow;

import java.util.List;

/**
 * A single edge in a workflow definition: when {@code event} fires while in any of {@code from}
 * states, and all {@code guards} pass, move to {@code to} and run {@code actions}. Guards and
 * actions are referenced by bean name and resolved from the Spring context at runtime.
 */
public record Transition(
    WorkflowEvent event,
    List<WorkflowState> from,
    WorkflowState to,
    List<String> guards,
    List<String> actions
) {
    public Transition {
        from = List.copyOf(from);
        guards = guards == null ? List.of() : List.copyOf(guards);
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    public boolean appliesTo(WorkflowState state, WorkflowEvent firedEvent) {
        return event.equals(firedEvent) && from.contains(state);
    }
}
