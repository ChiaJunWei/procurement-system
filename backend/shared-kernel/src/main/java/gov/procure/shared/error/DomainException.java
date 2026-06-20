package gov.procure.shared.error;

/**
 * Base class for all business-rule violations. Mapped to RFC-7807 ProblemDetail by the
 * global exception handler. Carries a stable machine-readable {@code code} for clients.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** Stable error code, e.g. {@code procurement.requisition.no-line-items}. */
    public String code() {
        return code;
    }
}
