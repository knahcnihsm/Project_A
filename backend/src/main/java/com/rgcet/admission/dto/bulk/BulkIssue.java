package com.rgcet.admission.dto.bulk;

/**
 * A single validation result for one workbook cell.
 * <p>
 * {@code ERROR} issues block the commit (strict, all-or-nothing). {@code WARNING}
 * issues are format / soft checks that never block the update.
 */
public record BulkIssue(
        String sheet,
        int rowNumber,
        String field,
        String message,
        Severity severity
) {

    public enum Severity {
        ERROR, WARNING
    }

    public boolean isError() {
        return severity == Severity.ERROR;
    }
}
