package gov.procure.procurement.domain;

import gov.procure.shared.domain.DomainEvent;
import gov.procure.shared.event.IntegrationEvent;
import gov.procure.shared.tenant.TenantId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when a requisition is created. Doubles as a domain event (in-process) and an
 * integration event (published to Kafka via the outbox). Consumed by Audit, Reporting, and
 * the Workflow engine. Evolve additively only.
 */
public record PurchaseRequisitionCreated(
    UUID eventId,
    Instant occurredAt,
    UUID requisitionId,
    TenantId tenantId,
    String requisitionNumber,
    UUID requesterId,
    BigDecimal totalAmount,
    String currency
) implements DomainEvent, IntegrationEvent {

    public static final String TOPIC = "procurement.purchase-requisition.events.v1";

    public static PurchaseRequisitionCreated from(PurchaseRequisition pr) {
        return new PurchaseRequisitionCreated(
            UUID.randomUUID(),
            Instant.now(),
            pr.id().value(),
            pr.tenantId(),
            pr.requisitionNumber().value(),
            pr.requesterId().value(),
            pr.totalEstimatedCost().amount(),
            pr.totalEstimatedCost().currency().getCurrencyCode());
    }

    @Override public String eventType() { return "PurchaseRequisitionCreated"; }
    @Override public int eventVersion() { return 1; }
    @Override public UUID aggregateId() { return requisitionId; }
    @Override public String topic() { return TOPIC; }
}
