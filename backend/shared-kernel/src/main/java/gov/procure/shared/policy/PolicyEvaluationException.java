package gov.procure.shared.policy;

import gov.procure.shared.error.DomainException;

/** Raised when a policy condition cannot be evaluated (malformed rule). Mapped to HTTP 422/500. */
public class PolicyEvaluationException extends DomainException {
    public PolicyEvaluationException(String message, Throwable cause) {
        super("policy.evaluation-error", message);
        initCause(cause);
    }
}
