package gov.procure.procurement.domain;

import gov.procure.shared.domain.AbstractAggregateRoot;
import gov.procure.shared.domain.Money;
import gov.procure.shared.tenant.TenantId;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for a purchase requisition — the reference aggregate for the platform.
 *
 * <p>Invariants enforced here (the core; DTO-level validation happens at the API edge):
 * <ul>
 *   <li>A requisition always belongs to exactly one tenant (agency).</li>
 *   <li>{@code totalEstimatedCost} is always the sum of its line items.</li>
 *   <li>Only a DRAFT requisition may be edited.</li>
 *   <li>Status transitions are owned by the Workflow engine, not mutated directly.</li>
 * </ul>
 *
 * Lifecycle status mirrors the {@code purchase-requisition.approval} WorkflowDefinition.
 */
public class PurchaseRequisition extends AbstractAggregateRoot<PurchaseRequisitionId> {

    private final PurchaseRequisitionId id;
    private final TenantId tenantId;
    private final RequisitionNumber requisitionNumber;
    private final UserId requesterId;
    private final Currency currency;
    private String justification;
    private RequisitionStatus status;
    private final List<LineItem> lineItems;
    private Money totalEstimatedCost;
    private final Instant createdAt;
    private Instant updatedAt;

    private PurchaseRequisition(PurchaseRequisitionId id, TenantId tenantId,
                               RequisitionNumber requisitionNumber, UserId requesterId,
                               String justification, Currency currency, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.requisitionNumber = requisitionNumber;
        this.requesterId = requesterId;
        this.justification = justification;
        this.currency = currency;
        this.status = RequisitionStatus.DRAFT;
        this.lineItems = new ArrayList<>();
        this.totalEstimatedCost = Money.zero(currency);
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Factory — the only way to create a requisition. Registers the
     * {@code PurchaseRequisitionCreated} domain event.
     */
    public static PurchaseRequisition create(TenantId tenantId, RequisitionNumber number,
                                            UserId requesterId, String justification,
                                            Currency currency, List<NewLineItem> items, Clock clock) {
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(requesterId, "requesterId");
        if (justification == null || justification.isBlank()) {
            throw new RequisitionValidationException("procurement.requisition.justification-required",
                "Justification is required");
        }
        if (items == null || items.isEmpty()) {
            throw new RequisitionValidationException("procurement.requisition.no-line-items",
                "A requisition needs at least one line item");
        }
        var requisition = new PurchaseRequisition(
            new PurchaseRequisitionId(UUID.randomUUID()), tenantId, number,
            requesterId, justification.strip(), currency, clock.instant());
        items.forEach(requisition::addLineItem);
        requisition.registerEvent(PurchaseRequisitionCreated.from(requisition));
        return requisition;
    }

    private void addLineItem(NewLineItem item) {
        requireDraft();
        var lineItem = LineItem.create(item.description(), item.quantity(),
            item.unitOfMeasure(), new Money(item.estimatedUnitPrice(), currency), item.budgetReference());
        lineItems.add(lineItem);
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalEstimatedCost = lineItems.stream()
            .map(LineItem::lineTotal)
            .reduce(Money.zero(currency), Money::add);
        this.updatedAt = Instant.now();
    }

    private void requireDraft() {
        if (status != RequisitionStatus.DRAFT) {
            throw new RequisitionValidationException("procurement.requisition.not-editable",
                "Only DRAFT requisitions can be modified (current: " + status + ")");
        }
    }

    /** Applied by the Workflow engine after a successful transition. */
    public void applyStatus(RequisitionStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    // --- accessors (no public setters — state changes go through behavior) ---
    @Override public PurchaseRequisitionId id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public RequisitionNumber requisitionNumber() { return requisitionNumber; }
    public UserId requesterId() { return requesterId; }
    public String justification() { return justification; }
    public RequisitionStatus status() { return status; }
    public List<LineItem> lineItems() { return List.copyOf(lineItems); }
    public Money totalEstimatedCost() { return totalEstimatedCost; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
