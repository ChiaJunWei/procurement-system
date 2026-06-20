package gov.procure.procurement.domain;

import java.util.Objects;
import java.util.UUID;

/** Reference to a user owned by the Identity context (by ID only — no object reference). */
public record UserId(UUID value) {
    public UserId {
        Objects.requireNonNull(value, "user id");
    }
    public static UserId of(String value) {
        return new UserId(UUID.fromString(value));
    }
}
