package gov.procure.shared.policy;

/** How a PolicySet combines its rules' effects into a single decision. */
public enum CombiningAlgorithm {
    /** Any DENY wins; else PERMIT if at least one permits; else NOT_APPLICABLE. */
    DENY_OVERRIDES,
    /** Any PERMIT wins; else DENY if at least one denies; else NOT_APPLICABLE. */
    PERMIT_OVERRIDES,
    /** The first rule that applies (non NOT_APPLICABLE) decides. */
    FIRST_APPLICABLE
}
