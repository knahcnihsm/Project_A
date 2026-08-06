package com.rgcet.admission.dto.bulk;

import java.util.List;

/**
 * One student in the preview step: identifier plus every field that will change
 * (blank workbook cells mean "no change" and never appear here).
 */
public record BulkPreviewRow(
        long studentId,
        String applicationNo,
        String registerNo,
        String studentName,
        List<FieldChange> changes
) {
}
