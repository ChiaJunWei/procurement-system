package gov.procure.procurement.domain;

import java.util.regex.Pattern;

/**
 * Human-readable requisition identifier, e.g. {@code PR-2026-000123}. Generated per-tenant by
 * a sequence; uniqueness is scoped to the tenant.
 */
public record RequisitionNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("PR-\\d{4}-\\d{6}");

    public RequisitionNumber {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid requisition number: " + value);
        }
    }

    public static RequisitionNumber forYearAndSeq(int year, long sequence) {
        return new RequisitionNumber("PR-%04d-%06d".formatted(year, sequence));
    }
}
