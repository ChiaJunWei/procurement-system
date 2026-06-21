package gov.procure.audit.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Append-only, hash-chained audit record. {@code recordHash = H(prevHash + canonical fields)} so any
 * tampering breaks the chain — tamper-evidence for a 10-year government compliance trail. Maps to
 * {@code audit.audit_record} (V1 migration). UPDATE/DELETE are revoked at the DB level.
 */
@Entity
@Table(name = "audit_record", schema = "audit")
public class AuditRecordEntity {

    @Id
    @Column(name = "id")
    UUID id;

    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;

    @Column(name = "occurred_at", nullable = false)
    Instant occurredAt;

    @Column(name = "actor_id")
    UUID actorId;

    @Column(name = "action", nullable = false)
    String action;

    @Column(name = "resource_type", nullable = false)
    String resourceType;

    @Column(name = "resource_id")
    UUID resourceId;

    @Column(name = "event_type", nullable = false)
    String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    String payload;

    @Column(name = "prev_hash")
    String prevHash;

    @Column(name = "record_hash", nullable = false)
    String recordHash;

    protected AuditRecordEntity() {}

    AuditRecordEntity(UUID id, UUID tenantId, Instant occurredAt, UUID actorId, String action,
                      String resourceType, UUID resourceId, String eventType, String payload,
                      String prevHash, String recordHash) {
        this.id = id;
        this.tenantId = tenantId;
        this.occurredAt = occurredAt;
        this.actorId = actorId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.eventType = eventType;
        this.payload = payload;
        this.prevHash = prevHash;
        this.recordHash = recordHash;
    }
}
