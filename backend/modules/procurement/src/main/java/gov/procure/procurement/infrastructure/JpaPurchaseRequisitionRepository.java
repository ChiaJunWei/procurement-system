package gov.procure.procurement.infrastructure;

import gov.procure.procurement.domain.PurchaseRequisition;
import gov.procure.procurement.domain.PurchaseRequisitionId;
import gov.procure.procurement.domain.PurchaseRequisitionRepository;
import gov.procure.procurement.domain.RequisitionNumber;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository ADAPTER implementing the domain port. Bridges aggregate ⇄ JPA entity and owns all
 * persistence concerns. RLS makes every query tenant-scoped automatically — no manual tenant
 * filtering. This is the reference adapter; copy this shape for new aggregates.
 */
@Repository
public class JpaPurchaseRequisitionRepository implements PurchaseRequisitionRepository {

    private final SpringDataPurchaseRequisitionRepository jpa;
    private final PurchaseRequisitionMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    public JpaPurchaseRequisitionRepository(SpringDataPurchaseRequisitionRepository jpa,
                                            PurchaseRequisitionMapper mapper,
                                            JdbcTemplate jdbcTemplate) {
        this.jpa = jpa;
        this.mapper = mapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PurchaseRequisition save(PurchaseRequisition requisition) {
        // Persist state only. Event lifecycle (draining to the outbox, clearing) is owned by the
        // application service — the adapter must NOT clear events or the service can't publish them.
        PurchaseRequisitionEntity entity = mapper.toEntity(requisition);
        jpa.save(entity);
        return requisition;
    }

    @Override
    public Optional<PurchaseRequisition> findById(PurchaseRequisitionId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public RequisitionNumber nextRequisitionNumber(int year) {
        // Per-tenant sequence; RLS-safe because the sequence row is tenant-scoped.
        Long seq = jdbcTemplate.queryForObject(
            "SELECT nextval('procurement.requisition_number_seq')", Long.class);
        return RequisitionNumber.forYearAndSeq(year, seq == null ? 1L : seq);
    }
}
