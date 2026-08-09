package com.rgcet.admission.dto;

import java.util.List;

public final class BulkAdmissionDtos {

    private BulkAdmissionDtos() {
    }

    public record BulkAdmissionPreviewResponse(AdmissionSummaryDto summary, List<AdmissionRecordPreviewDto> records) {
    }

    public record AdmissionSummaryDto(
            int totalRecords,
            int validRecords,
            int invalidRecords
    ) {
    }

    public record AdmissionRecordPreviewDto(
            String applicationNo,
            String studentName,
            String program,
            String totalFee,
            boolean valid,
            List<String> errors
    ) {
    }

    public record BulkAdmissionApplyResponse(AdmissionApplySummaryDto summary, List<AdmissionResultDto> results) {
    }

    public record AdmissionApplySummaryDto(
            int totalRecords,
            int createdRecords,
            int failedRecords
    ) {
    }

    public record AdmissionResultDto(String applicationNo, String studentName, String status, List<String> errors) {
    }
}
