package gov.procure.shared.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

/** Spring Data access for outbox rows. Internal to the event framework. */
interface OutboxRepository extends JpaRepository<OutboxEventEntity, UUID> {

    /**
     * Fetch a batch of undispatched events oldest-first, skip-locking rows already claimed by
     * another relay instance so multiple app pods can publish concurrently without double-sending.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")) // SKIP LOCKED
    @Query("""
        select e from OutboxEventEntity e
        where e.dispatchedAt is null
        order by e.occurredAt asc
        """)
    List<OutboxEventEntity> findUndispatched(Limit limit);

    default void markDispatched(OutboxEventEntity entity, Instant when) {
        entity.markDispatched(when);
        save(entity);
    }
}
