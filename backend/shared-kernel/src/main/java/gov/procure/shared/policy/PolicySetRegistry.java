package gov.procure.shared.policy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Collects all {@link PolicySet} beans by id. Features add authorization/business rules by
 * declaring a PolicySet bean keyed to an action namespace — no engine changes.
 */
@Component
public class PolicySetRegistry {

    private final Map<String, PolicySet> byId = new HashMap<>();

    public PolicySetRegistry(List<PolicySet> policySets) {
        policySets.forEach(ps -> byId.put(ps.id(), ps));
    }

    /** Resolve the policy set governing an action. Returns null when none applies. */
    public PolicySet forAction(String action) {
        return byId.get(action);
    }
}
