package gov.procure.shared.policy;

/**
 * Policy Decision Point (PDP). Evaluates a {@link PolicyRequest} against the applicable
 * {@code PolicySet} (base + tenant overrides) and returns a {@link PolicyDecision} carrying
 * an effect (PERMIT/DENY/NOT_APPLICABLE) and any obligations. Used for both ABAC authorization
 * and declarative business rules. See docs/architecture/policy-engine.md.
 */
public interface PolicyEngine {

    PolicyDecision evaluate(PolicyRequest request);

    /** Convenience: evaluate and throw {@code PolicyViolationException} if denied. */
    default PolicyDecision enforce(PolicyRequest request) {
        PolicyDecision decision = evaluate(request);
        if (decision.isDenied()) {
            throw new PolicyViolationException(request.action(), decision.reasons());
        }
        return decision;
    }
}
