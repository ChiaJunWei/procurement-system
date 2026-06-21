package gov.procure.shared.workflow;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstanceEntity, UUID> {
    Optional<WorkflowInstanceEntity> findByAggregateId(UUID aggregateId);
}
