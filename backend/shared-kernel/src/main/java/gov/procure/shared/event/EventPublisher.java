package gov.procure.shared.event;

/** Port for publishing a serialized event envelope to the message broker. */
public interface EventPublisher {

    /**
     * Publish to the given topic. Partition key MUST be the aggregate id to preserve per-aggregate
     * ordering. Implementations set standard Kafka headers (event_id, event_type, tenant_id, ...).
     */
    void publish(String topic, OutboxEventEntity event);
}
