package gov.procure.audit.infrastructure;

import gov.procure.audit.application.AuditTrail;
import gov.procure.shared.event.EventEnvelope;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/**
 * Builds and persists hash-chained audit records. {@code recordHash = SHA-256(prevHash | eventId |
 * eventType | tenantId | occurredAt | payload)}. Each tenant has its own chain. Lives in
 * infrastructure because it touches the JPA entity and crypto details.
 */
@Component
public class AuditRecordWriter implements AuditTrail {

    private final AuditRecordRepository repository;
    private final Clock clock;

    public AuditRecordWriter(AuditRecordRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void append(EventEnvelope envelope) {
        String prevHash = repository.findLatestHash(envelope.tenantId(), Limit.of(1))
            .stream().findFirst().orElse(null);

        String payload = envelope.payload().toString();
        String recordHash = hash(prevHash, envelope, payload);

        var entity = new AuditRecordEntity(
            UUID.randomUUID(),
            envelope.tenantId(),
            envelope.occurredAt(),
            actorFrom(envelope),
            deriveAction(envelope.eventType()),
            deriveResourceType(envelope.eventType()),
            envelope.aggregateId(),
            envelope.eventType(),
            payload,
            prevHash,
            recordHash);
        repository.save(entity);
    }

    private String hash(String prevHash, EventEnvelope e, String payload) {
        String canonical = String.join("|",
            prevHash == null ? "" : prevHash,
            e.eventId().toString(), e.eventType(), e.tenantId().toString(),
            e.occurredAt().toString(), payload);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private UUID actorFrom(EventEnvelope envelope) {
        var node = envelope.payload().get("requesterId");
        if (node != null && !node.isNull()) {
            try {
                return UUID.fromString(node.asText());
            } catch (IllegalArgumentException ignored) {
                // not a user-attributable event
            }
        }
        return null;
    }

    private static final List<String> VERBS = List.of("Created", "Updated", "Submitted", "Approved",
        "Rejected", "Cancelled", "Deleted", "Transitioned");

    /** Derive a coarse action verb from the event name, e.g. PurchaseRequisitionCreated → CREATED. */
    private String deriveAction(String eventType) {
        return VERBS.stream()
            .filter(eventType::endsWith)
            .findFirst()
            .map(v -> v.toUpperCase())
            .orElse("EVENT");
    }

    /** Derive the resource type, e.g. PurchaseRequisitionCreated → PurchaseRequisition. */
    private String deriveResourceType(String eventType) {
        return VERBS.stream()
            .filter(eventType::endsWith)
            .findFirst()
            .map(v -> eventType.substring(0, eventType.length() - v.length()))
            .orElse(eventType);
    }
}
