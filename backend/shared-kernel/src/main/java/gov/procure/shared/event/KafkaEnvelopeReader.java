package gov.procure.shared.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.stereotype.Component;

/**
 * Reconstructs an {@link EventEnvelope} from a Kafka record: standard headers (written by
 * {@link KafkaEventPublisher}) plus the JSON body as the payload. Consumers use this so they all
 * parse events the same way and stay tolerant of additive payload changes.
 */
@Component
public class KafkaEnvelopeReader {

    private final ObjectMapper objectMapper;

    public KafkaEnvelopeReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EventEnvelope read(ConsumerRecord<String, String> record) {
        try {
            JsonNode payload = objectMapper.readTree(record.value());
            return new EventEnvelope(
                uuid(record, "event_id"),
                header(record, "event_type"),
                intHeader(record, "event_version"),
                UUID.fromString(record.key()),
                uuid(record, "tenant_id"),
                Instant.parse(header(record, "occurred_at")),
                optionalUuid(record, "correlation_id"),
                payload);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read event envelope from record", e);
        }
    }

    private String header(ConsumerRecord<String, String> record, String key) {
        Header h = record.headers().lastHeader(key);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }

    private int intHeader(ConsumerRecord<String, String> record, String key) {
        String v = header(record, key);
        return v == null ? 1 : Integer.parseInt(v);
    }

    private UUID uuid(ConsumerRecord<String, String> record, String key) {
        return UUID.fromString(header(record, key));
    }

    private UUID optionalUuid(ConsumerRecord<String, String> record, String key) {
        String v = header(record, key);
        return v == null || v.isBlank() ? null : UUID.fromString(v);
    }
}
