package gov.procure.shared.security;

import java.util.Set;
import java.util.UUID;

/**
 * The authenticated principal, distilled from the Keycloak JWT. Used by PEPs to build policy
 * requests and by services that need the acting user. Kept minimal and framework-free so the
 * domain/application layers depend on this, not on Spring Security types.
 */
public record CurrentUser(UUID id, UUID agencyId, Set<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
