package gov.procure.shared.policy;

import java.util.HashMap;
import java.util.Map;

/**
 * A decision request: who (subject), what action, on what resource, in what environment.
 * Attributes are open maps so new attributes need no engine changes — only an AttributeResolver.
 */
public record PolicyRequest(
    Map<String, Object> subject,
    String action,
    Map<String, Object> resource,
    Map<String, Object> environment
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, Object> subject = new HashMap<>();
        private String action;
        private final Map<String, Object> resource = new HashMap<>();
        private final Map<String, Object> environment = new HashMap<>();

        public Builder subject(String key, Object value) { subject.put(key, value); return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder resource(String key, Object value) { resource.put(key, value); return this; }
        public Builder environment(String key, Object value) { environment.put(key, value); return this; }

        public PolicyRequest build() {
            return new PolicyRequest(Map.copyOf(subject), action, Map.copyOf(resource), Map.copyOf(environment));
        }
    }
}
