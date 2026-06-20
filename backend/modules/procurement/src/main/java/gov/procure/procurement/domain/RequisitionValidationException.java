package gov.procure.procurement.domain;

import gov.procure.shared.error.DomainException;

/** Raised when a requisition invariant is violated. Mapped to HTTP 422 by the global handler. */
public class RequisitionValidationException extends DomainException {
    public RequisitionValidationException(String code, String message) {
        super(code, message);
    }
}
