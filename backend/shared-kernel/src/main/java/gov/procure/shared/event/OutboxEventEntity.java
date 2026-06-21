package gov.procure.shared.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Outbox row written in the same transaction as the aggregate state change. The {@link OutboxRelay}
 * later publishes undispatched rows to Kafka and stamps {@code dispatchedAt}. Maps to
 * {@code integration.outbox_event} (see V1 migration).
 */
@Entity
@Table(name = "outbox_event", schema = "integration")
public class OutboxEventEntity {

    @Id
    @Column(name = "id")
    UUID id;

    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;

    @Column(name = "aggregate_id", nullable = false)
    UUID aggregateId;

    @Column(name = "event_type", nullable = false)
    String eventType;

    @Column(name = "event_version", nullable = false)
    int eventVersion;

    @Column(name = "topic", nullable = false)
    String topic;

    // JdbcTypeCode maps to the dialect's JSON type (jsonb on Postgres, json on H2) — kept portable
    // so the in-memory mock profile works without Postgres. The Postgres migration declares jsonb.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    String payload;

    @Column(name = "correlation_id")
    UUID correlationId;

    @Column(name = "occurred_at", nullable = false)
    Instant occurredAt;

    @Column(name = "dispatched_at")
    Instant dispatchedAt;

    protected OutboxEventEntity() {}

    OutboxEventEntity(UUID id, UUID tenantId, UUID aggregateId, String eventType, int eventVersion,
                      String topic, String payload, UUID correlationId, Instant occurredAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.topic = topic;
        this.payload = payload;
        this.correlationId = correlationId;
        this.occurredAt = occurredAt;
    }

    void markDispatched(Instant when) {
        this.dispatchedAt = when;
    }

    // Accessors used by the relay / publisher (same-package framework code).
    public UUID id() { return id; }
    public UUID tenantId() { return tenantId; }
    public UUID aggregateId() { return aggregateId; }
    public String eventType() { return eventType; }
    public int eventVersion() { return eventVersion; }
    public String topic() { return topic; }
    public String payload() { return payload; }
    public UUID correlationId() { return correlationId; }
    public Instant occurredAt() { return occurredAt; }
    public Instant dispatchedAt() { return dispatchedAt; }
}
