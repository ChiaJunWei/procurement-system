package gov.procure.shared.workflow;

import java.util.List;

/** Outcome of firing an event against a workflow instance. */
public record WorkflowResult(
    WorkflowState previousState,
    WorkflowState newState,
    List<String> firedActions
) {}
