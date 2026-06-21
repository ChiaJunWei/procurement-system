package gov.procure.shared.workflow;

import java.util.UUID;

/** Reference to a started workflow instance. */
public record WorkflowInstanceRef(UUID instanceId, UUID aggregateId, WorkflowState state) {}
