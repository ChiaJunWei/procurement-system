package gov.procure.audit.application;

import gov.procure.shared.event.EventEnvelope;

/**
 * Port for appending to the immutable audit trail. Declared in the application layer and implemented
 * by an infrastructure adapter, so the use-case service depends on this abstraction — not on the
 * hash-chaining/JPA details (hexagonal dependency rule).
 */
public interface AuditTrail {
    void append(EventEnvelope envelope);
}
