package gov.procure.shared.event;

import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka implementation of {@link EventPublisher}. Keys records by aggregate id (ordering) and sets
 * the standard headers every consumer relies on. JSON payload is published as-is from the outbox.
 */
@Component
@Profile("!mock") // replaced by LoggingEventPublisher in the runnable mock profile
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, OutboxEventEntity event) {
        var record = new ProducerRecord<>(topic, event.aggregateId().toString(), event.payload());
        record.headers()
            .add(header("event_id", event.id().toString()))
            .add(header("event_type", event.eventType()))
            .add(header("event_version", Integer.toString(event.eventVersion())))
            .add(header("tenant_id", event.tenantId().toString()))
            .add(header("occurred_at", event.occurredAt().toString()))
            .add(header("correlation_id",
                event.correlationId() != null ? event.correlationId().toString() : ""));
        // Synchronous send so the relay only marks dispatched after the broker acks.
        kafkaTemplate.send(record).join();
    }

    private RecordHeader header(String key, String value) {
        return new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8));
    }
}
