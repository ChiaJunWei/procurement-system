package gov.procure.shared.policy;

import java.util.List;

/** Decision returned by the PDP: an effect plus reasons and obligations. */
public record PolicyDecision(Effect effect, List<String> reasons, List<Obligation> obligations) {

    public PolicyDecision {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
        obligations = obligations == null ? List.of() : List.copyOf(obligations);
    }

    public boolean isDenied() {
        return effect == Effect.DENY;
    }

    public boolean isPermitted() {
        return effect == Effect.PERMIT;
    }

    public static PolicyDecision permit(List<Obligation> obligations) {
        return new PolicyDecision(Effect.PERMIT, List.of(), obligations);
    }

    public static PolicyDecision deny(List<String> reasons) {
        return new PolicyDecision(Effect.DENY, reasons, List.of());
    }

    public static PolicyDecision notApplicable() {
        return new PolicyDecision(Effect.NOT_APPLICABLE, List.of(), List.of());
    }
}
