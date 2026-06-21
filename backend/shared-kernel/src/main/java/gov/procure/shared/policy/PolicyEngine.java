package gov.procure.shared.policy;

/**
 * Policy Decision Point (PDP). Evaluates a {@link PolicyRequest} against the applicable
 * {@code PolicySet} (base + tenant overrides) and returns a {@link PolicyDecision} carrying
 * an effect (PERMIT/DENY/NOT_APPLICABLE) and any obligations. Used for both ABAC authorization
 * and declarative business rules. See docs/architecture/policy-engine.md.
 */
public interface PolicyEngine {

    PolicyDecision evaluate(PolicyRequest request);

    /**
     * Enforce a request with a <strong>default-deny</strong> posture: the action is allowed only on
     * an explicit PERMIT. An explicit DENY <em>or</em> NOT_APPLICABLE (no rule matched / no policy
     * set) throws {@code PolicyViolationException}. This is the safe default for a government system —
     * forgetting to write a permit rule fails closed, not open.
     */
    default PolicyDecision enforce(PolicyRequest request) {
        PolicyDecision decision = evaluate(request);
        if (!decision.isPermitted()) {
            var reasons = decision.reasons().isEmpty()
                ? java.util.List.of("no applicable permit rule (default deny)")
                : decision.reasons();
            throw new PolicyViolationException(request.action(), reasons);
        }
        return decision;
    }
}
