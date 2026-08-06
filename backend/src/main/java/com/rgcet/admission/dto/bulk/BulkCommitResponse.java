package com.rgcet.admission.dto.bulk;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Result of {@code POST /api/bulk/commit}. The whole workbook commits in a single
 * all-or-nothing transaction, so on {@code SUCCESS} every valid row was applied and
 * {@code failedRows} is zero by design. On {@code FAILED} nothing was applied and
 * {@code issues} carries every blocking error.
 */
public record BulkCommitResponse(
        String fileName,
        String uploadedBy,
        LocalDateTime uploadedAt,
        int totalRows,
        int validRows,
        int updatedStudents,
        int noChangeRows,
        int skippedRows,
        int failedRows,
        int warningCount,
        long durationMs,
        String status,
        List<BulkIssue> issues
) {
}
