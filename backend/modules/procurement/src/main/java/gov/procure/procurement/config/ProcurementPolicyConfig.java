package gov.procure.procurement.config;

import gov.procure.shared.policy.CombiningAlgorithm;
import gov.procure.shared.policy.Obligation;
import gov.procure.shared.policy.PolicySet;
import gov.procure.shared.policy.Rule;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declarative authorization + business rules for the Procurement context. Reference pattern: add a
 * {@link PolicySet} bean keyed to an action namespace; the engine and PEPs need no changes.
 *
 * <p>Conditions are SpEL over {@code subject} / {@code resource} attribute maps supplied by the PEP.
 */
@Configuration
public class ProcurementPolicyConfig {

    public static final String ACTION_CREATE = "procurement.requisition.create";

    @Bean
    PolicySet requisitionCreatePolicy() {
        return new PolicySet(ACTION_CREATE, CombiningAlgorithm.DENY_OVERRIDES, List.of(
            // A requester may only create requisitions for their own agency (tenant).
            Rule.deny("requester-agency-mismatch",
                "#subject['agencyId'] != #resource['tenantId']"),
            // High-value requisitions permit, but oblige a second approver downstream.
            Rule.permit("high-value-needs-second-approval",
                "#resource['amount'] > 50000",
                Obligation.of("REQUIRE_ADDITIONAL_APPROVAL", Map.of("role", "FINANCE_DIRECTOR"))),
            // Otherwise, the requester may create their own requisition.
            Rule.permit("requester-may-create", "#subject['id'] == #resource['requesterId']")
        ));
    }
}
