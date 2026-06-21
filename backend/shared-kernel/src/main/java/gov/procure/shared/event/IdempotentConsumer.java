package gov.procure.shared.event;

import gov.procure.shared.tenant.TenantContext;
import gov.procure.shared.tenant.TenantId;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helper every Kafka consumer uses to get exactly-once *effects* on top of at-least-once delivery.
 * Records (event_id, consumer) and skips re-processing. Also re-establishes the tenant context from
 * the message so RLS works inside the consumer. Wrap your handler in {@link #process}.
 *
 * <pre>
 *   idempotentConsumer.process("audit", envelope, env -&gt; auditService.record(env));
 * </pre>
 */
@Component
public class IdempotentConsumer {

    private final ProcessedEventRepository processedEvents;
    private final Clock clock;

    public IdempotentConsumer(ProcessedEventRepository processedEvents, Clock clock) {
        this.processedEvents = processedEvents;
        this.clock = clock;
    }

    @Transactional
    public void process(String consumerName, EventEnvelope envelope, Consumer<EventEnvelope> handler) {
        UUID eventId = envelope.eventId();
        var key = new ProcessedEventEntity.Key(eventId, consumerName);
        if (processedEvents.existsById(key)) {
            return; // already handled — no-op
        }
        TenantId tenantId = new TenantId(envelope.tenantId());
        TenantContext.runAs(tenantId, () -> handler.accept(envelope));
        processedEvents.save(new ProcessedEventEntity(eventId, consumerName, clock.instant()));
    }
}
