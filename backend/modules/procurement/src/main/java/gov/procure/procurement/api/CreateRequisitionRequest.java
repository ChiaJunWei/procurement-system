package gov.procure.procurement.api;

import gov.procure.procurement.application.CreatePurchaseRequisitionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * API request DTO. Edge validation (Bean Validation) mirrors — but does not replace — the
 * aggregate invariants. DTOs are decoupled from both domain and command types on purpose.
 */
public record CreateRequisitionRequest(
    @NotNull UUID requesterId,
    @NotBlank @Size(max = 2000) String justification,
    @NotBlank @Size(min = 3, max = 3) String currency,
    @NotEmpty @Valid List<LineItemRequest> lineItems
) {
    public record LineItemRequest(
        @NotBlank String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
        String unitOfMeasure,
        @NotNull @DecimalMin("0.0") BigDecimal estimatedUnitPrice,
        @NotNull UUID budgetId,
        @NotBlank String accountingCode
    ) {}

    public CreatePurchaseRequisitionCommand toCommand() {
        var lines = lineItems.stream()
            .map(l -> new CreatePurchaseRequisitionCommand.Line(
                l.description(), l.quantity(), l.unitOfMeasure(),
                l.estimatedUnitPrice(), l.budgetId(), l.accountingCode()))
            .toList();
        return new CreatePurchaseRequisitionCommand(requesterId, justification, currency, lines);
    }
}
