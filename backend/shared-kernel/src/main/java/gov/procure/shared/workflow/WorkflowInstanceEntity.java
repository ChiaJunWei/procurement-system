package gov.procure.shared.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

/**
 * Runtime state of one aggregate moving through a {@link WorkflowDefinition}. Persisted in the
 * {@code workflow} schema. Pins the definition version it started on. Optimistic-locked so
 * concurrent transitions don't clobber each other.
 */
@Entity
@Table(name = "workflow_instance", schema = "workflow")
public class WorkflowInstanceEntity {

    @Id
    @Column(name = "id")
    UUID id;

    @Column(name = "tenant_id", nullable = false)
    UUID tenantId;

    @Column(name = "definition_key", nullable = false)
    String definitionKey;

    @Column(name = "definition_version", nullable = false)
    int definitionVersion;

    @Column(name = "aggregate_id", nullable = false)
    UUID aggregateId;

    @Column(name = "current_state", nullable = false)
    String currentState;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Version
    @Column(name = "lock_version", nullable = false)
    long lockVersion;

    protected WorkflowInstanceEntity() {}

    WorkflowInstanceEntity(UUID id, UUID tenantId, String definitionKey, int definitionVersion,
                           UUID aggregateId, String currentState, Instant now) {
        this.id = id;
        this.tenantId = tenantId;
        this.definitionKey = definitionKey;
        this.definitionVersion = definitionVersion;
        this.aggregateId = aggregateId;
        this.currentState = currentState;
        this.createdAt = now;
        this.updatedAt = now;
    }

    void moveTo(String state, Instant when) {
        this.currentState = state;
        this.updatedAt = when;
    }

    UUID id() { return id; }
    UUID tenantId() { return tenantId; }
    String definitionKey() { return definitionKey; }
    int definitionVersion() { return definitionVersion; }
    UUID aggregateId() { return aggregateId; }
    String currentState() { return currentState; }
}
