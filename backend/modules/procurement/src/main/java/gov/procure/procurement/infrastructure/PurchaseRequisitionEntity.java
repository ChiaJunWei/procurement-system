package gov.procure.procurement.infrastructure;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence model — lives ONLY in infrastructure so the domain stays persistence-agnostic.
 * Mapped to/from the {@code PurchaseRequisition} aggregate by {@code PurchaseRequisitionMapper}.
 * No business logic here.
 */
@Entity
@Table(name = "purchase_requisition", schema = "procurement")
public class PurchaseRequisitionEntity {

    @Id
    @Column(name = "id")
    UUID id;

    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;

    @Column(name = "requisition_number", nullable = false)
    String requisitionNumber;

    @Column(name = "requester_id", nullable = false)
    UUID requesterId;

    @Column(name = "justification", nullable = false)
    String justification;

    @Column(name = "status", nullable = false)
    String status;

    @Column(name = "currency", nullable = false, length = 3)
    String currency;

    @Column(name = "total_estimated_amount", nullable = false)
    BigDecimal totalEstimatedAmount;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true,
        fetch = FetchType.EAGER)
    List<LineItemEntity> lineItems = new ArrayList<>();

    protected PurchaseRequisitionEntity() {}
}
