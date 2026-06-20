package gov.procure.shared.tenant;

import java.util.Objects;
import java.util.UUID;

/** A tenant is an agency. Strongly-typed to avoid mixing up bare UUIDs. */
public record TenantId(UUID value) {
    public TenantId {
        Objects.requireNonNull(value, "tenant id value");
    }

    public static TenantId of(String value) {
        return new TenantId(UUID.fromString(value));
    }
}
