package gov.procure.shared.policy;

import gov.procure.shared.error.DomainException;
import java.util.List;

/** Thrown when {@link PolicyEngine#enforce} denies a request. Mapped to HTTP 403 by the handler. */
public class PolicyViolationException extends DomainException {

    private final transient List<String> reasons;

    public PolicyViolationException(String action, List<String> reasons) {
        super("policy.denied", "Policy denied action '" + action + "': " + String.join("; ", reasons));
        this.reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public List<String> reasons() {
        return reasons;
    }
}
