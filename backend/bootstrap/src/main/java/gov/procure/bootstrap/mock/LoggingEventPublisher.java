package gov.procure.bootstrap.mock;

import gov.procure.shared.event.EventPublisher;
import gov.procure.shared.event.OutboxEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * MOCK event publisher: logs instead of producing to Kafka, so the transactional outbox + relay
 * flow runs fully in-process under the {@code mock} profile. Replaces {@code KafkaEventPublisher}.
 * Downstream consumers (Audit, Reporting) do NOT receive these events in mock mode. See MOCKS.md.
 */
@Component
@Profile("mock")
public class LoggingEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    @Override
    public void publish(String topic, OutboxEventEntity event) {
        log.info("[MOCK-KAFKA] topic={} type={} aggregate={} tenant={} payload={}",
            topic, event.eventType(), event.aggregateId(), event.tenantId(), event.payload());
    }
}
