package gov.procure.shared.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Holds all known workflow definitions, keyed by definition key (latest version wins for new
 * instances). Features contribute definitions by declaring a {@link WorkflowDefinition} bean — the
 * registry collects them at startup. Tenant overrides can be layered here later.
 */
@Component
public class WorkflowDefinitionRegistry {

    private final Map<String, WorkflowDefinition> latestByKey = new HashMap<>();
    private final Map<String, WorkflowDefinition> byKeyAndVersion = new HashMap<>();

    public WorkflowDefinitionRegistry(List<WorkflowDefinition> definitions) {
        for (WorkflowDefinition def : definitions) {
            byKeyAndVersion.put(versionKey(def.key(), def.version()), def);
            latestByKey.merge(def.key(), def,
                (a, b) -> a.version() >= b.version() ? a : b);
        }
    }

    public WorkflowDefinition latest(String key) {
        WorkflowDefinition def = latestByKey.get(key);
        if (def == null) {
            throw new WorkflowException("No workflow definition registered for key: " + key);
        }
        return def;
    }

    public WorkflowDefinition get(String key, int version) {
        WorkflowDefinition def = byKeyAndVersion.get(versionKey(key, version));
        if (def == null) {
            throw new WorkflowException("No workflow definition %s v%d".formatted(key, version));
        }
        return def;
    }

    private String versionKey(String key, int version) {
        return key + "::" + version;
    }
}
