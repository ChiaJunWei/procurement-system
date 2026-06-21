package gov.procure.shared.event;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

/**
 * The wire format published to Kafka and parsed by consumers. Self-describing (type + version)
 * so consumers can route and evolve tolerantly. Payload is the serialized {@link IntegrationEvent}.
 */
public record EventEnvelope(
    UUID eventId,
    String eventType,
    int eventVersion,
    UUID aggregateId,
    UUID tenantId,
    Instant occurredAt,
    UUID correlationId,
    JsonNode payload
) {}
