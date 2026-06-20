package gov.procure.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all aggregate roots. Holds and exposes uncommitted domain events.
 * Persistence-agnostic by design — no framework annotations live here.
 *
 * @param <ID> the strongly-typed identifier of the aggregate
 */
public abstract class AbstractAggregateRoot<ID> {

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    public abstract ID id();

    /** Record a domain event to be published after the aggregate is persisted. */
    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /** Read uncommitted events (immutable view). */
    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /** Called by the persistence adapter once events have been forwarded to the outbox. */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
