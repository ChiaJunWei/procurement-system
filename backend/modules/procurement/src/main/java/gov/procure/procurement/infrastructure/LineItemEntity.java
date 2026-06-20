package gov.procure.procurement.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "requisition_line_item", schema = "procurement")
public class LineItemEntity {

    @Id
    @Column(name = "id")
    UUID id;

    @ManyToOne
    @JoinColumn(name = "requisition_id", nullable = false)
    PurchaseRequisitionEntity requisition;

    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "quantity", nullable = false)
    BigDecimal quantity;

    @Column(name = "unit_of_measure")
    String unitOfMeasure;

    @Column(name = "estimated_unit_price", nullable = false)
    BigDecimal estimatedUnitPrice;

    @Column(name = "budget_id", nullable = false)
    UUID budgetId;

    @Column(name = "accounting_code", nullable = false)
    String accountingCode;

    protected LineItemEntity() {}
}
