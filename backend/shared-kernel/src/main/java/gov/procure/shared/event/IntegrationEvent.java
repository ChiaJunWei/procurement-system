package gov.procure.shared.event;

import gov.procure.shared.tenant.TenantId;
import java.time.Instant;
import java.util.UUID;

/**
 * An event that crosses a bounded-context boundary. Carried on Kafka via the
 * transactional outbox. Implementations are immutable records with additive-only evolution.
 */
public interface IntegrationEvent {

    UUID eventId();

    int eventVersion();

    String eventType();

    UUID aggregateId();

    TenantId tenantId();

    Instant occurredAt();

    /** Kafka topic this event is published to, e.g. {@code procurement.purchase-requisition.events.v1}. */
    String topic();
}
