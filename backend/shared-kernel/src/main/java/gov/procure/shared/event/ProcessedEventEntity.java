package gov.procure.shared.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Dedupe record: (event_id, consumer) marks an event already handled by a given consumer. */
@Entity
@Table(name = "processed_event", schema = "integration")
@IdClass(ProcessedEventEntity.Key.class)
public class ProcessedEventEntity {

    @Id
    @Column(name = "event_id")
    UUID eventId;

    @Id
    @Column(name = "consumer")
    String consumer;

    @Column(name = "processed_at", nullable = false)
    Instant processedAt;

    protected ProcessedEventEntity() {}

    ProcessedEventEntity(UUID eventId, String consumer, Instant processedAt) {
        this.eventId = eventId;
        this.consumer = consumer;
        this.processedAt = processedAt;
    }

    /** Composite-key class for {@code @IdClass}: public fields + no-arg ctor as JPA requires. */
    public static class Key implements Serializable {
        public UUID eventId;
        public String consumer;

        public Key() {}

        public Key(UUID eventId, String consumer) {
            this.eventId = eventId;
            this.consumer = consumer;
        }

        @Override public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(eventId, k.eventId)
                && Objects.equals(consumer, k.consumer);
        }

        @Override public int hashCode() {
            return Objects.hash(eventId, consumer);
        }
    }
}
