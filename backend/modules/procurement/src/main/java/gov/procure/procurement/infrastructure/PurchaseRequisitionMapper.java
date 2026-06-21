package gov.procure.procurement.infrastructure;

import gov.procure.procurement.domain.BudgetReference;
import gov.procure.procurement.domain.LineItem;
import gov.procure.procurement.domain.PurchaseRequisition;
import gov.procure.procurement.domain.PurchaseRequisitionId;
import gov.procure.procurement.domain.RequisitionNumber;
import gov.procure.procurement.domain.RequisitionStatus;
import gov.procure.procurement.domain.UserId;
import gov.procure.shared.domain.Money;
import gov.procure.shared.tenant.TenantId;
import java.util.Currency;
import java.util.List;
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

    /** Rehydrate the aggregate from its persistence model via the domain's rehydrate factory. */
    public PurchaseRequisition toDomain(PurchaseRequisitionEntity entity) {
        Currency currency = Currency.getInstance(entity.currency);
        List<LineItem> lineItems = entity.lineItems.stream()
            .map(le -> LineItem.rehydrate(
                le.id, le.description, le.quantity, le.unitOfMeasure,
                new Money(le.estimatedUnitPrice, currency),
                new BudgetReference(le.budgetId, le.accountingCode)))
            .toList();
        return PurchaseRequisition.rehydrate(
            new PurchaseRequisitionId(entity.id),
            new TenantId(entity.tenantId),
            new RequisitionNumber(entity.requisitionNumber),
            new UserId(entity.requesterId),
            entity.justification,
            RequisitionStatus.valueOf(entity.status),
            currency,
            lineItems,
            new Money(entity.totalEstimatedAmount, currency),
            entity.createdAt,
            entity.updatedAt);
    }
}
