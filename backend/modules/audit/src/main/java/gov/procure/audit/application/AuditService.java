package gov.procure.audit.application;

import gov.procure.shared.event.EventEnvelope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Turns any integration event into an immutable audit record. The Audit context is a pure consumer:
 * no other context calls it directly, and it owns the entire compliance trail. New event types are
 * captured automatically — no per-event code needed.
 */
@Service
public class AuditService {

    private final AuditTrail auditTrail;

    public AuditService(AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    @Transactional
    public void record(EventEnvelope envelope) {
        auditTrail.append(envelope);
    }
}
