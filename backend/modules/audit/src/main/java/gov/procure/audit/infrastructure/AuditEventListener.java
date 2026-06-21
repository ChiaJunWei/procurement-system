package gov.procure.audit.infrastructure;

import gov.procure.audit.application.AuditService;
import gov.procure.shared.event.EventEnvelope;
import gov.procure.shared.event.IdempotentConsumer;
import gov.procure.shared.event.KafkaEnvelopeReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Subscribes to ALL domain-event topics (pattern) and writes each event to the audit trail. Uses
 * {@link IdempotentConsumer} for exactly-once effects over at-least-once delivery, which also
 * re-establishes the tenant context so the insert is correctly scoped. Adding a new event type
 * anywhere in the platform requires no change here.
 */
@Component
@Profile("!mock") // no Kafka in the mock profile; audit consumption is disabled there (see MOCKS.md)
public class AuditEventListener {

    private static final String CONSUMER_NAME = "audit";

    private final KafkaEnvelopeReader envelopeReader;
    private final IdempotentConsumer idempotentConsumer;
    private final AuditService auditService;

    public AuditEventListener(KafkaEnvelopeReader envelopeReader,
                              IdempotentConsumer idempotentConsumer,
                              AuditService auditService) {
        this.envelopeReader = envelopeReader;
        this.idempotentConsumer = idempotentConsumer;
        this.auditService = auditService;
    }

    @KafkaListener(
        topicPattern = "${platform.audit.topic-pattern:.*\\.events\\.v[0-9]+}",
        groupId = "audit-trail")
    public void onEvent(ConsumerRecord<String, String> record) {
        EventEnvelope envelope = envelopeReader.read(record);
        idempotentConsumer.process(CONSUMER_NAME, envelope, auditService::record);
    }
}
