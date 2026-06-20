package gov.procure.shared.workflow;

import gov.procure.shared.tenant.TenantId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Carries the data guards/actions need to make a decision. Attributes are open-ended so features
 * can pass domain-specific values (e.g. "amount", "approverId") without changing the engine.
 */
public final class WorkflowContext {

    private final UUID aggregateId;
    private final TenantId tenantId;
    private final Map<String, Object> attributes;

    private WorkflowContext(UUID aggregateId, TenantId tenantId, Map<String, Object> attributes) {
        this.aggregateId = aggregateId;
        this.tenantId = tenantId;
        this.attributes = attributes;
    }

    public static WorkflowContext forAggregate(UUID aggregateId, TenantId tenantId) {
        return new WorkflowContext(aggregateId, tenantId, new HashMap<>());
    }

    public WorkflowContext with(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public UUID aggregateId() { return aggregateId; }
    public TenantId tenantId() { return tenantId; }
    public Optional<Object> attribute(String key) { return Optional.ofNullable(attributes.get(key)); }
}
