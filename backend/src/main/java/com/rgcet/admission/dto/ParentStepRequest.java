package com.rgcet.admission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ParentStepRequest(
        @NotBlank(message = "Father name is required") String fatherName,
        @Pattern(regexp = "\\d{10}", message = "Father mobile number must be 10 digits") String fatherMobile,
        String fatherOccupation,
        BigDecimal annualIncome
) {
}
