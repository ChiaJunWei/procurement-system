package gov.procure.shared.workflow;

import gov.procure.shared.error.DomainException;

/** Raised for invalid workflow operations (unknown definition, illegal transition, failed guard). */
public class WorkflowException extends DomainException {
    public WorkflowException(String message) {
        super("workflow.error", message);
    }

    public WorkflowException(String code, String message) {
        super(code, message);
    }
}
