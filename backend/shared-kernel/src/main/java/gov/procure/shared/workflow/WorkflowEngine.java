package gov.procure.shared.workflow;

import java.util.UUID;

/**
 * Generic, data-driven state machine. Any aggregate becomes "workflowable" by declaring a
 * {@code WorkflowDefinition} and delegating its lifecycle transitions here. Features add
 * definitions (data) and named {@link WorkflowGuard}/{@link WorkflowAction} beans — never
 * new engine plumbing. See docs/architecture/workflow-engine.md.
 */
public interface WorkflowEngine {

    /** Start a new instance of a definition for an aggregate, returning the initial state. */
    WorkflowInstanceRef start(String definitionKey, UUID aggregateId, WorkflowContext context);

    /**
     * Fire an event against an aggregate's workflow instance. Evaluates guards (which may
     * delegate to the Policy engine), runs actions, persists the transition atomically with
     * the caller's transaction, and emits a {@code WorkflowTransitioned} integration event.
     */
    WorkflowResult fire(UUID aggregateId, WorkflowEvent event, WorkflowContext context);

    /** Current state of an aggregate's instance. */
    WorkflowState currentState(UUID aggregateId);
}
