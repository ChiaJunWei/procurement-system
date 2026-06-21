package gov.procure.shared.event;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProcessedEventRepository
    extends JpaRepository<ProcessedEventEntity, ProcessedEventEntity.Key> {
}
