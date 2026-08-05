package com.rgcet.admission.dto;

import jakarta.validation.Valid;

import java.util.List;

public record BulkUpdateRequest(
        @Valid List<UpdateRow> rows
) {

    /**
     * One row of the Bulk Student Update Excel template.
     * Blank/empty fields mean "No Change" and are never applied to the database.
     */
    public record UpdateRow(
            Integer rowNumber,
            String applicationNumber,
            String registerNumber,
            String studentName,
            String dateOfBirth,
            String gender,
            String aadhaarNumber,
            String district,
            String caste,
            String admissionCategory,
            String program,
            String department,
            String batch,
            String fatherName,
            String fatherMobile,
            String mobileNumber,
            String email,
            String grandTotalFee,
            String status,
            String archiveReason
    ) {
    }

    public record BulkUpdateResponse(
            int totalRows,
            int updatedCount,
            int skippedCount,
            int failedCount,
            List<RowError> errors
    ) {
    }

    public record RowError(
            int rowNumber,
            String registerNumber,
            String applicationNumber,
            String reason
    ) {
    }
}
