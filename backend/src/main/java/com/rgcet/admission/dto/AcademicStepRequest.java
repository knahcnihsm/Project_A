package com.rgcet.admission.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AcademicStepRequest(
        @NotNull(message = "Admission category is required") Long categoryId,
        @NotNull(message = "Program is required") Long programId,
        Long departmentId,
        String batch,
        LocalDate dateOfAdmission
) {
}
