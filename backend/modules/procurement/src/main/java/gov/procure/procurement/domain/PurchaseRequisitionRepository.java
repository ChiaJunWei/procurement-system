package gov.procure.procurement.domain;

import java.util.Optional;

/**
 * Repository PORT (declared in the domain, implemented in infrastructure). The domain depends on
 * this abstraction, never on JPA. Tenant scoping is enforced by RLS — no tenant parameter needed.
 */
public interface PurchaseRequisitionRepository {

    PurchaseRequisition save(PurchaseRequisition requisition);

    Optional<PurchaseRequisition> findById(PurchaseRequisitionId id);

    /** Allocates the next per-tenant requisition number for the given year. */
    RequisitionNumber nextRequisitionNumber(int year);
}
