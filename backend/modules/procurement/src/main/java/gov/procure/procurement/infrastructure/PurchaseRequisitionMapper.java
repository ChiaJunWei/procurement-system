package gov.procure.procurement.infrastructure;

import gov.procure.procurement.domain.PurchaseRequisition;
import org.springframework.stereotype.Component;

/**
 * Maps between the {@code PurchaseRequisition} aggregate and its JPA entity. Kept explicit (not
 * reflection-based) so the boundary is obvious and refactor-safe. Reconstitution of the aggregate
 * from the entity uses a package-private rehydration constructor or factory in a fuller build;
 * sketched here to show where mapping responsibility lives.
 */
@Component
public class PurchaseRequisitionMapper {

    public PurchaseRequisitionEntity toEntity(PurchaseRequisition pr) {
        var entity = new PurchaseRequisitionEntity();
        entity.id = pr.id().value();
        entity.tenantId = pr.tenantId().value();
        entity.requisitionNumber = pr.requisitionNumber().value();
        entity.requesterId = pr.requesterId().value();
        entity.justification = pr.justification();
        entity.status = pr.status().name();
        entity.currency = pr.totalEstimatedCost().currency().getCurrencyCode();
        entity.totalEstimatedAmount = pr.totalEstimatedCost().amount();
        entity.createdAt = pr.createdAt();
        entity.updatedAt = pr.updatedAt();
        entity.lineItems.clear();
        pr.lineItems().forEach(li -> {
            var le = new LineItemEntity();
            le.id = li.id();
            le.requisition = entity;
            le.tenantId = pr.tenantId().value();
            le.description = li.description();
            le.quantity = li.quantity();
            le.unitOfMeasure = li.unitOfMeasure();
            le.estimatedUnitPrice = li.estimatedUnitPrice().amount();
            le.budgetId = li.budgetReference().budgetId();
            le.accountingCode = li.budgetReference().accountingCode();
            entity.lineItems.add(le);
        });
        return entity;
    }

    /**
     * Rehydrate the aggregate from persistence. A production implementation adds a
     * package-private {@code PurchaseRequisition.rehydrate(...)} factory that bypasses the
     * creation event. Stubbed to mark the responsibility boundary.
     */
    public PurchaseRequisition toDomain(PurchaseRequisitionEntity entity) {
        throw new UnsupportedOperationException(
            "Add PurchaseRequisition.rehydrate(...) and map here — see coding-standards.md");
    }
}
