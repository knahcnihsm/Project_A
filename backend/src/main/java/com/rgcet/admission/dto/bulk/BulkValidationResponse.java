package com.rgcet.admission.dto.bulk;

import java.util.List;

/**
 * Read-only result of {@code POST /api/bulk/validate}. Nothing is written to the
 * database. The caller inspects {@code errorCount} / {@code issues} and the
 * {@code preview} before calling commit.
 */
public record BulkValidationResponse(
        String fileName,
        int totalRows,
        int matchedStudents,
        int unmatchedRows,
        int errorCount,
        int warningCount,
        boolean valid,
        List<BulkIssue> issues,
        List<BulkPreviewRow> preview
) {
}
