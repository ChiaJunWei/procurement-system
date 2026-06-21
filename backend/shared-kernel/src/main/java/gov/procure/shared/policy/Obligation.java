package gov.procure.shared.policy;

import java.util.Map;

/** An obligation the PEP must fulfil when permitting (e.g. REQUIRE_ADDITIONAL_APPROVAL). */
public record Obligation(String type, Map<String, Object> parameters) {
    public static Obligation of(String type, Map<String, Object> parameters) {
        return new Obligation(type, parameters == null ? Map.of() : Map.copyOf(parameters));
    }
}
