package gov.procure.procurement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gov.procure.procurement.domain.BudgetReference;
import gov.procure.procurement.domain.NewLineItem;
import gov.procure.procurement.domain.PurchaseRequisition;
import gov.procure.procurement.domain.PurchaseRequisitionCreated;
import gov.procure.procurement.domain.RequisitionNumber;
import gov.procure.procurement.domain.RequisitionStatus;
import gov.procure.procurement.domain.RequisitionValidationException;
import gov.procure.procurement.domain.UserId;
import gov.procure.shared.tenant.TenantId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Pure domain unit test — no Spring, no DB. This is the reference test pattern for aggregates:
 * one test per invariant + one for the emitted event.
 */
class PurchaseRequisitionTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-20T00:00:00Z"), ZoneOffset.UTC);
    private static final Currency USD = Currency.getInstance("USD");

    private NewLineItem line(String price, String qty) {
        return new NewLineItem("Laptop", new BigDecimal(qty), "EA", new BigDecimal(price),
            new BudgetReference(UUID.randomUUID(), "ACCT-001"));
    }

    private PurchaseRequisition create(List<NewLineItem> lines) {
        return PurchaseRequisition.create(
            new TenantId(UUID.randomUUID()),
            RequisitionNumber.forYearAndSeq(2026, 1),
            new UserId(UUID.randomUUID()),
            "Annual hardware refresh", USD, lines, CLOCK);
    }

    @Test
    void newRequisitionStartsInDraftAndTotalsLineItems() {
        var pr = create(List.of(line("1000.00", "2"), line("50.00", "3")));

        assertThat(pr.status()).isEqualTo(RequisitionStatus.DRAFT);
        assertThat(pr.totalEstimatedCost().amount()).isEqualByComparingTo("2150.00");
    }

    @Test
    void creatingWithoutLineItemsIsRejected() {
        assertThatThrownBy(() -> create(List.of()))
            .isInstanceOf(RequisitionValidationException.class)
            .hasMessageContaining("at least one line item");
    }

    @Test
    void creationEmitsIntegrationEvent() {
        var pr = create(List.of(line("1000.00", "1")));

        assertThat(pr.domainEvents())
            .hasSize(1)
            .first()
            .isInstanceOfSatisfying(PurchaseRequisitionCreated.class, e -> {
                assertThat(e.eventType()).isEqualTo("PurchaseRequisitionCreated");
                assertThat(e.totalAmount()).isEqualByComparingTo("1000.00");
            });
    }
}
