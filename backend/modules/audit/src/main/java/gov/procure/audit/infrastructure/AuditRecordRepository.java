package gov.procure.audit.infrastructure;

import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface AuditRecordRepository extends JpaRepository<AuditRecordEntity, UUID> {

    /** Most recent record's hash for a tenant, to extend the hash chain. */
    @Query("""
        select a.recordHash from AuditRecordEntity a
        where a.tenantId = :tenantId
        order by a.occurredAt desc
        """)
    java.util.List<String> findLatestHash(UUID tenantId, Limit limit);
}
