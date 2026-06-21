package gov.procure.shared.policy;

import java.util.List;

/**
 * A named, ordered collection of rules combined by {@code algorithm}. Bound to an action namespace
 * (e.g. {@code procurement.requisition.submit}). Declared as a bean or loaded from data; tenant
 * overrides can be merged in later.
 */
public record PolicySet(
    String id,
    CombiningAlgorithm algorithm,
    List<Rule> rules
) {
    public PolicySet {
        rules = List.copyOf(rules);
    }
}
