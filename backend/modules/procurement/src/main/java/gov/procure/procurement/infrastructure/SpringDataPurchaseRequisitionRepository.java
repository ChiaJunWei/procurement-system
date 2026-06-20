package gov.procure.procurement.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data interface — internal to infrastructure; the domain never sees it. */
interface SpringDataPurchaseRequisitionRepository
    extends JpaRepository<PurchaseRequisitionEntity, UUID> {
}
