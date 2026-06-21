package gov.procure.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import gov.procure.procurement.application.CreatePurchaseRequisitionCommand;
import gov.procure.procurement.application.CreatePurchaseRequisitionService;
import gov.procure.procurement.domain.PurchaseRequisitionId;
import gov.procure.shared.security.CurrentUser;
import gov.procure.shared.security.CurrentUserProvider;
import gov.procure.shared.tenant.TenantContext;
import gov.procure.shared.tenant.TenantId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full-stack integration test for the reference vertical slice. Boots the real application context
 * against a Testcontainers PostgreSQL (Flyway migrations + RLS applied), invokes the use-case
 * service, and asserts the transactional outcome: aggregate row + workflow instance + outbox event
 * all committed atomically. This is the reference pattern for infrastructure/integration tests.
 *
 * <p>Gated on Docker availability so {@code ./gradlew check} stays green on machines without Docker;
 * it runs fully in CI. Kafka listeners are disabled and the JWT decoder / current user are stubbed
 * because this test drives the service directly rather than over HTTP.
 */
@SpringBootTest
@Testcontainers
@EnabledIf("dockerAvailable") // skips cleanly (incl. container startup) when Docker is absent
class CreatePurchaseRequisitionIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("procurement");

    static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (RuntimeException e) {
            return false;
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.listener.auto-startup", () -> "false");
        // Avoid eager OIDC discovery against a real Keycloak during context startup.
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "");
    }

    // Stubbed so the resource-server autoconfig backs off (no network call to Keycloak).
    @MockBean
    JwtDecoder jwtDecoder;

    // The service reads the acting user from here; supply one matching the tenant so policy permits.
    @MockBean
    CurrentUserProvider currentUserProvider;

    @Autowired
    CreatePurchaseRequisitionService service;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final TenantId tenant = new TenantId(UUID.randomUUID());

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void createsRequisitionWithWorkflowInstanceAndOutboxEvent() {
        UUID requester = UUID.randomUUID();
        when(currentUserProvider.require())
            .thenReturn(new CurrentUser(requester, tenant.value(), Set.of("REQUESTER")));

        TenantContext.set(tenant);

        var command = new CreatePurchaseRequisitionCommand(
            requester, "Annual hardware refresh", "USD",
            List.of(new CreatePurchaseRequisitionCommand.Line(
                "Laptop", new BigDecimal("2"), "EA", new BigDecimal("1000.00"),
                UUID.randomUUID(), "ACCT-001")));

        PurchaseRequisitionId id = service.handle(command);

        Integer prCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM procurement.purchase_requisition WHERE id = ?",
            Integer.class, id.value());
        assertThat(prCount).isEqualTo(1);

        Integer wfCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM workflow.workflow_instance WHERE aggregate_id = ?",
            Integer.class, id.value());
        assertThat(wfCount).isEqualTo(1);

        Integer outboxCount = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM integration.outbox_event WHERE aggregate_id = ? AND event_type = ?",
            Integer.class, id.value(), "PurchaseRequisitionCreated");
        assertThat(outboxCount).isEqualTo(1);
    }
}
