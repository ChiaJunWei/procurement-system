package gov.procure.shared.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A versioned state-machine blueprint. Features declare one of these (as a bean or loaded from
 * data) and the engine drives any aggregate through it. Definitions are immutable once built;
 * in-flight instances pin the version they started on.
 */
public record WorkflowDefinition(
    String key,
    int version,
    WorkflowState initialState,
    List<WorkflowState> states,
    List<Transition> transitions
) {
    public WorkflowDefinition {
        states = List.copyOf(states);
        transitions = List.copyOf(transitions);
    }

    /** Find the transition applicable to the given state/event, if any. */
    public Optional<Transition> transitionFor(WorkflowState state, WorkflowEvent event) {
        return transitions.stream().filter(t -> t.appliesTo(state, event)).findFirst();
    }

    public static Builder builder(String key, int version) {
        return new Builder(key, version);
    }

    public static final class Builder {
        private final String key;
        private final int version;
        private WorkflowState initialState;
        private final List<WorkflowState> states = new ArrayList<>();
        private final List<Transition> transitions = new ArrayList<>();

        private Builder(String key, int version) {
            this.key = key;
            this.version = version;
        }

        public Builder initial(String state) {
            this.initialState = WorkflowState.of(state);
            states.add(initialState);
            return this;
        }

        public Builder state(String state) {
            states.add(WorkflowState.of(state));
            return this;
        }

        public Builder transition(String event, List<String> from, String to,
                                  List<String> guards, List<String> actions) {
            transitions.add(new Transition(
                WorkflowEvent.of(event),
                from.stream().map(WorkflowState::of).toList(),
                WorkflowState.of(to), guards, actions));
            return this;
        }

        public WorkflowDefinition build() {
            return new WorkflowDefinition(key, version, initialState, states, transitions);
        }
    }
}
