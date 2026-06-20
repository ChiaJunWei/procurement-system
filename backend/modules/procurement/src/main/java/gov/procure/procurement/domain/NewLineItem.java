package gov.procure.procurement.domain;

import java.math.BigDecimal;

/**
 * Input value object describing a line item to add. Keeps the aggregate factory signature clean
 * and decoupled from API DTOs.
 */
public record NewLineItem(
    String description,
    BigDecimal quantity,
    String unitOfMeasure,
    BigDecimal estimatedUnitPrice,
    BudgetReference budgetReference
) {}
