package gov.procure.procurement.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Use-case input. Decoupled from API DTOs and from the domain — the service translates it into
 * domain value objects. CQRS-lite: this is a write command.
 */
public record CreatePurchaseRequisitionCommand(
    UUID requesterId,
    String justification,
    String currency,
    List<Line> lineItems
) {
    public record Line(
        String description,
        BigDecimal quantity,
        String unitOfMeasure,
        BigDecimal estimatedUnitPrice,
        UUID budgetId,
        String accountingCode
    ) {}
}
