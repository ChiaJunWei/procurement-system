package gov.procure.shared.event;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Publishes committed outbox rows to Kafka and marks them dispatched. Polls on a fixed interval;
 * a {@code LISTEN/NOTIFY} trigger can wake it sooner for low latency. Rows are claimed with
 * {@code SELECT ... FOR UPDATE SKIP LOCKED} so multiple app pods share the work safely.
 *
 * <p>At-least-once: a crash after publish but before commit re-publishes the row → consumers MUST
 * be idempotent (see {@link IdempotentConsumer}).
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final int BATCH_SIZE = 100;

    private final OutboxRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public OutboxRelay(OutboxRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = repository;
        this.publisher = publisher;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${platform.outbox.poll-interval-ms:1000}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatchBatch() {
        List<OutboxEventEntity> batch = repository.findUndispatched(Limit.of(BATCH_SIZE));
        for (OutboxEventEntity event : batch) {
            try {
                publisher.publish(event.topic(), event);
                repository.markDispatched(event, clock.instant());
            } catch (RuntimeException ex) {
                // Leave undispatched; next poll retries. Alert on repeated failures upstream.
                log.warn("Failed to publish outbox event {} ({}): {}",
                    event.id(), event.eventType(), ex.getMessage());
            }
        }
    }
}
