package gov.procure.shared.event;

/**
 * Appends an {@link IntegrationEvent} to the transactional outbox within the CURRENT
 * database transaction. The {@code OutboxRelay} later publishes committed rows to Kafka
 * and marks them dispatched, giving atomic state+event semantics without 2PC.
 *
 * <p>Usage (inside a {@code @Transactional} use-case service):
 * <pre>
 *   repository.save(aggregate);
 *   outboxAppender.append(PurchaseRequisitionCreated.from(aggregate));
 * </pre>
 */
public interface OutboxAppender {

    void append(IntegrationEvent event);
}
