package gov.procure.shared.policy;

import java.util.ArrayList;
import java.util.List;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Service;

/**
 * Reference {@link PolicyEngine} (the PDP). Evaluates each rule's SpEL condition against the
 * request's subject/resource/environment maps using a {@link SimpleEvaluationContext} (read-only,
 * no method invocation beyond property access — safe for data-driven rules), then combines the
 * effects per the policy set's {@link CombiningAlgorithm}.
 *
 * <p>Conditions reference attributes like {@code subject['agencyId']} or {@code resource['amount']}.
 */
@Service
public class DefaultPolicyEngine implements PolicyEngine {

    private final PolicySetRegistry registry;
    private final ExpressionParser parser = new SpelExpressionParser();

    public DefaultPolicyEngine(PolicySetRegistry registry) {
        this.registry = registry;
    }

    @Override
    public PolicyDecision evaluate(PolicyRequest request) {
        PolicySet policySet = registry.forAction(request.action());
        if (policySet == null) {
            return PolicyDecision.notApplicable();
        }

        var ctx = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        ctx.setVariable("subject", request.subject());
        ctx.setVariable("resource", request.resource());
        ctx.setVariable("environment", request.environment());

        boolean anyPermit = false;
        boolean anyDeny = false;
        List<String> denyReasons = new ArrayList<>();
        List<Obligation> obligations = new ArrayList<>();

        for (Rule rule : policySet.rules()) {
            if (!matches(rule, ctx)) {
                continue;
            }
            switch (rule.effect()) {
                case DENY -> {
                    anyDeny = true;
                    denyReasons.add(rule.id());
                    if (policySet.algorithm() == CombiningAlgorithm.DENY_OVERRIDES
                        || policySet.algorithm() == CombiningAlgorithm.FIRST_APPLICABLE) {
                        return PolicyDecision.deny(denyReasons);
                    }
                }
                case PERMIT -> {
                    anyPermit = true;
                    obligations.addAll(rule.obligations());
                    if (policySet.algorithm() == CombiningAlgorithm.PERMIT_OVERRIDES
                        || policySet.algorithm() == CombiningAlgorithm.FIRST_APPLICABLE) {
                        return PolicyDecision.permit(obligations);
                    }
                }
                case NOT_APPLICABLE -> { /* skip */ }
            }
        }

        if (policySet.algorithm() == CombiningAlgorithm.DENY_OVERRIDES) {
            return anyPermit ? PolicyDecision.permit(obligations) : PolicyDecision.notApplicable();
        }
        if (policySet.algorithm() == CombiningAlgorithm.PERMIT_OVERRIDES) {
            return anyDeny ? PolicyDecision.deny(denyReasons) : PolicyDecision.notApplicable();
        }
        return PolicyDecision.notApplicable();
    }

    private boolean matches(Rule rule, org.springframework.expression.EvaluationContext ctx) {
        if (rule.condition() == null || rule.condition().isBlank()) {
            return true; // unconditional rule
        }
        try {
            Expression expression = parser.parseExpression(rule.condition());
            return Boolean.TRUE.equals(expression.getValue(ctx, Boolean.class));
        } catch (RuntimeException ex) {
            throw new PolicyEvaluationException(
                "Failed to evaluate rule '%s': %s".formatted(rule.id(), ex.getMessage()), ex);
        }
    }
}
