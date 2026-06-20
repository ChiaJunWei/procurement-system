package gov.procure.shared.policy;

import gov.procure.shared.error.DomainException;
import java.util.List;
import java.util.Map;

/** Policy decision returned by the PDP. */
record PolicyDecision(Effect effect, List<String> reasons, List<Obligation> obligations) {
    enum Effect { PERMIT, DENY, NOT_APPLICABLE }
    boolean isDenied() { return effect == Effect.DENY; }
}

/** An obligation the PEP must fulfil when permitting (e.g. require a second approver). */
record Obligation(String type, Map<String, Object> parameters) {}

/** A decision request: who, what action, on what resource, in what environment. */
record PolicyRequest(Map<String, Object> subject, String action,
                     Map<String, Object> resource, Map<String, Object> environment) {
    String actionName() { return action; }
}

/** Thrown when {@code PolicyEngine.enforce} denies a request. Mapped to HTTP 403. */
class PolicyViolationException extends DomainException {
    PolicyViolationException(String action, List<String> reasons) {
        super("policy.denied", "Policy denied action '" + action + "': " + String.join("; ", reasons));
    }
}
