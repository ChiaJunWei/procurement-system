package gov.procure.shared.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CurrentUser} from the Keycloak JWT in the Spring Security context. Expects
 * {@code sub} (user id), {@code agency_id} (tenant), and a {@code roles} claim. The only place that
 * knows about Spring Security token types — everything else depends on {@link CurrentUserProvider}.
 */
@Component
@Profile("!mock") // replaced by MockCurrentUserProvider in the runnable mock profile
public class JwtCurrentUserProvider implements CurrentUserProvider {

    @Override
    @SuppressWarnings("unchecked")
    public Optional<CurrentUser> current() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return Optional.empty();
        }
        Jwt jwt = jwtAuth.getToken();
        UUID userId = UUID.fromString(jwt.getSubject());
        String agency = jwt.getClaimAsString("agency_id");
        UUID agencyId = agency != null ? UUID.fromString(agency) : null;
        Object rolesClaim = jwt.getClaims().getOrDefault("roles", Set.of());
        Set<String> roles = rolesClaim instanceof java.util.Collection<?> c
            ? c.stream().map(Object::toString).collect(java.util.stream.Collectors.toSet())
            : Set.of();
        return Optional.of(new CurrentUser(userId, agencyId, roles));
    }
}
