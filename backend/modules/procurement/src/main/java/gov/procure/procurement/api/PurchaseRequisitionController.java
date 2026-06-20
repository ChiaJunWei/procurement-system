package gov.procure.procurement.api;

import gov.procure.procurement.application.CreatePurchaseRequisitionCommand;
import gov.procure.procurement.application.CreatePurchaseRequisitionService;
import gov.procure.procurement.domain.PurchaseRequisitionId;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the Procurement context. The controller is thin: it validates input (Bean
 * Validation), maps DTO → command, delegates to the use-case service, and maps the result to HTTP.
 * No business logic here. This is the reference controller — copy this shape.
 *
 * <p>Authorization: coarse-grained at the method (Keycloak authority); fine-grained ABAC happens
 * inside the service via the Policy engine. Tenant is resolved from the JWT by TenantContextFilter.
 */
@RestController
@RequestMapping("/api/v1/procurement/requisitions")
public class PurchaseRequisitionController {

    private final CreatePurchaseRequisitionService createService;

    public PurchaseRequisitionController(CreatePurchaseRequisitionService createService) {
        this.createService = createService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('procurement:requisition:create')")
    public ResponseEntity<CreateRequisitionResponse> create(
            @Valid @RequestBody CreateRequisitionRequest request) {

        CreatePurchaseRequisitionCommand command = request.toCommand();
        PurchaseRequisitionId id = createService.handle(command);

        return ResponseEntity
            .created(URI.create("/api/v1/procurement/requisitions/" + id.value()))
            .body(new CreateRequisitionResponse(id.value().toString()));
    }
}
