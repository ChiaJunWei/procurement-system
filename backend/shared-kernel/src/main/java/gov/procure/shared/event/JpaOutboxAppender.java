package gov.procure.shared.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Default {@link OutboxAppender}: serializes the event and persists an outbox row in the CURRENT
 * transaction. No Kafka here — publishing is the relay's job after commit. This guarantees atomic
 * state + event without distributed transactions.
 */
@Component
public class JpaOutboxAppender implements OutboxAppender {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    public JpaOutboxAppender(OutboxRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append(IntegrationEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event " + event.eventType(), e);
        }
        var entity = new OutboxEventEntity(
            event.eventId() != null ? event.eventId() : UUID.randomUUID(),
            event.tenantId().value(),
            event.aggregateId(),
            event.eventType(),
            event.eventVersion(),
            event.topic(),
            payload,
            CorrelationId.currentOrNew(),
            event.occurredAt() != null ? event.occurredAt() : Instant.now());
        repository.save(entity);
    }
}
