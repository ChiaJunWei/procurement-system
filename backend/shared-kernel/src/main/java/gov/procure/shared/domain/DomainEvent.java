package gov.procure.shared.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker for in-process domain events. Implementations are immutable records.
 * A domain event becomes an {@code IntegrationEvent} when it must cross a context boundary.
 */
public interface DomainEvent {

    UUID eventId();

    Instant occurredAt();

    /** Logical event name, e.g. {@code PurchaseRequisitionCreated}. */
    String eventType();
}
