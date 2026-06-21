package gov.procure.shared.policy;

import java.util.List;

/**
 * A single policy rule. {@code condition} is a SpEL expression evaluated over the request's
 * {@code subject}, {@code resource}, and {@code environment} attribute maps. When it evaluates true
 * the rule yields {@code effect} and contributes its {@code obligations}.
 */
public record Rule(
    String id,
    Effect effect,
    String condition,
    List<Obligation> obligations
) {
    public Rule {
        obligations = obligations == null ? List.of() : List.copyOf(obligations);
    }

    public static Rule permit(String id, String condition, Obligation... obligations) {
        return new Rule(id, Effect.PERMIT, condition, List.of(obligations));
    }

    public static Rule deny(String id, String condition) {
        return new Rule(id, Effect.DENY, condition, List.of());
    }
}
