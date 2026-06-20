package gov.procure.shared.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Establishes the tenant for the duration of a web request from the {@code agency_id} JWT claim,
 * then clears it in a finally block. Also pushes the tenant into the logging MDC. Runs early so
 * every downstream component (and the DB connection preparer) sees the correct tenant.
 */
@Component
@Order(1)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final String TENANT_CLAIM = "agency_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            resolveTenant().ifPresent(tenantId -> {
                TenantContext.set(tenantId);
                MDC.put("tenantId", tenantId.value().toString());
            });
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            MDC.remove("tenantId");
        }
    }

    private java.util.Optional<TenantId> resolveTenant() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String claim = jwt.getClaimAsString(TENANT_CLAIM);
            if (claim != null) {
                return java.util.Optional.of(TenantId.of(claim));
            }
        }
        return java.util.Optional.empty();
    }
}
