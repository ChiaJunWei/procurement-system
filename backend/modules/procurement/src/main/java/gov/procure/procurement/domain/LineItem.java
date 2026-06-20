package gov.procure.procurement.domain;

import gov.procure.shared.domain.Money;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity owned by the {@link PurchaseRequisition} aggregate. Has no independent lifecycle —
 * always accessed and modified through the root.
 */
public class LineItem {

    private final UUID id;
    private final String description;
    private final BigDecimal quantity;
    private final String unitOfMeasure;
    private final Money estimatedUnitPrice;
    private final BudgetReference budgetReference;

    private LineItem(UUID id, String description, BigDecimal quantity, String unitOfMeasure,
                    Money estimatedUnitPrice, BudgetReference budgetReference) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.unitOfMeasure = unitOfMeasure;
        this.estimatedUnitPrice = estimatedUnitPrice;
        this.budgetReference = budgetReference;
    }

    static LineItem create(String description, BigDecimal quantity, String unitOfMeasure,
                           Money estimatedUnitPrice, BudgetReference budgetReference) {
        if (description == null || description.isBlank()) {
            throw new RequisitionValidationException("procurement.lineitem.description-required",
                "Line item description is required");
        }
        if (quantity == null || quantity.signum() <= 0) {
            throw new RequisitionValidationException("procurement.lineitem.invalid-quantity",
                "Line item quantity must be positive");
        }
        return new LineItem(UUID.randomUUID(), description.strip(), quantity,
            unitOfMeasure, estimatedUnitPrice, Objects.requireNonNull(budgetReference));
    }

    public Money lineTotal() {
        return estimatedUnitPrice.multiply(quantity);
    }

    public UUID id() { return id; }
    public String description() { return description; }
    public BigDecimal quantity() { return quantity; }
    public String unitOfMeasure() { return unitOfMeasure; }
    public Money estimatedUnitPrice() { return estimatedUnitPrice; }
    public BudgetReference budgetReference() { return budgetReference; }
}
