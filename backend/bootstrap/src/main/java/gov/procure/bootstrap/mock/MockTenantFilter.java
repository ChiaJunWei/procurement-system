package gov.procure.bootstrap.mock;

import gov.procure.shared.event.CorrelationId;
import gov.procure.shared.tenant.TenantContext;
import gov.procure.shared.tenant.TenantId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * MOCK tenant resolution: sets the tenant from an optional {@code X-Tenant-Id} header, defaulting to
 * the dev agency. Replaces {@code TenantContextFilter} under the {@code mock} profile (no JWT to read
 * the claim from). See MOCKS.md.
 */
@Component
@Order(1)
@Profile("mock")
public class MockTenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("X-Tenant-Id");
            TenantId tenantId = new TenantId(
                header != null ? UUID.fromString(header) : MockIdentity.AGENCY_ID);
            TenantContext.set(tenantId);
            MDC.put("tenantId", tenantId.value().toString());
            CorrelationId.set(UUID.randomUUID());
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            MDC.remove("tenantId");
            CorrelationId.clear();
        }
    }
}
