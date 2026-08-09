package com.rgcet.admission.dto;

import java.util.List;
import java.util.Map;

public final class BulkUpdateDtos {

    private BulkUpdateDtos() {
    }

    public record BulkUpdateSchemaDto(
            String lookupKey,
            String updatedBy,
            List<TableDto> tables,
            Map<String, List<String>> masterData
    ) {
    }

    public record TableDto(String tableName, List<ColumnDto> columns) {
    }

    public record ColumnDto(
            String name,
            String type,
            boolean required,
            List<String> enumValues,
            String fkReference,
            boolean isKey
    ) {
    }

    public record BulkUpdateRequest(List<SheetDto> sheets) {
    }

    public record SheetDto(String tableName, List<Map<String, String>> rows) {
    }

    public record BulkUpdatePreviewResponse(SummaryDto summary, List<RecordPreviewDto> records) {
    }

    public record RecordPreviewDto(
            String applicationNo,
            String studentName,
            boolean valid,
            List<String> errors,
            List<ChangeDto> changes
    ) {
    }

    public record ChangeDto(String tableName, String fieldName, String oldValue, String newValue) {
    }

    public record SummaryDto(
            int totalRecords,
            int validRecords,
            int invalidRecords,
            int changedRecords,
            int unchangedRecords
    ) {
    }

    public record BulkUpdateApplyResponse(ApplySummaryDto summary, List<RecordResultDto> results) {
    }

    public record ApplySummaryDto(
            int totalRecords,
            int updatedRecords,
            int skippedRecords,
            int failedRecords
    ) {
    }

    public record RecordResultDto(String applicationNo, String studentName, String status, List<String> errors) {
    }
}
