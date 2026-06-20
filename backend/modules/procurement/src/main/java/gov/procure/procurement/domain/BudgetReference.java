package gov.procure.procurement.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * ID-reference into the Agency context's Budget aggregate. We reference by ID only — never hold
 * a Budget object — to keep contexts decoupled and extraction-ready.
 */
public record BudgetReference(UUID budgetId, String accountingCode) {
    public BudgetReference {
        Objects.requireNonNull(budgetId, "budgetId");
        Objects.requireNonNull(accountingCode, "accountingCode");
    }
}
