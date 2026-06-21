package gov.procure.shared.event;

import java.util.UUID;
import org.slf4j.MDC;

/**
 * Correlation id propagated across the request → outbox → Kafka → consumer chain for traceability.
 * Stored in the logging MDC by the inbound filter; falls back to a fresh id if absent.
 */
public final class CorrelationId {

    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {}

    public static UUID currentOrNew() {
        String value = MDC.get(MDC_KEY);
        if (value != null) {
            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException ignored) {
                // fall through to a new id
            }
        }
        return UUID.randomUUID();
    }

    public static void set(UUID id) {
        MDC.put(MDC_KEY, id.toString());
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
