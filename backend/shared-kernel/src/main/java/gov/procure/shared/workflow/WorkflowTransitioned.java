package gov.procure.shared.workflow;

import gov.procure.shared.event.IntegrationEvent;
import gov.procure.shared.tenant.TenantId;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitted on every successful workflow transition. Consumed by Audit (compliance trail) and any
 * context that reacts to lifecycle changes. Published to a single cross-cutting workflow topic.
 */
public record WorkflowTransitioned(
    UUID eventId,
    Instant occurredAt,
    UUID aggregateId,
    TenantId tenantId,
    String definitionKey,
    String fromState,
    String toState,
    String event
) implements IntegrationEvent {

    public static final String TOPIC = "workflow.instance.events.v1";

    @Override public String eventType() { return "WorkflowTransitioned"; }
    @Override public int eventVersion() { return 1; }
    @Override public String topic() { return TOPIC; }
}
