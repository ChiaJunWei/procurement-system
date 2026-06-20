package gov.procure.procurement.domain;

import java.util.Objects;
import java.util.UUID;

public record PurchaseRequisitionId(UUID value) {
    public PurchaseRequisitionId {
        Objects.requireNonNull(value, "purchase requisition id");
    }
    public static PurchaseRequisitionId of(String value) {
        return new PurchaseRequisitionId(UUID.fromString(value));
    }
}
