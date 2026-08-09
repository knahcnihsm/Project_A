package com.rgcet.admission.dto;

import com.rgcet.admission.entity.Caste;
import com.rgcet.admission.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PersonalStepRequest(
        @NotBlank(message = "Application number is required") String applicationNumber,
        @NotBlank(message = "Register number is required") String registerNumber,
        @NotBlank(message = "Student name is required") String studentName,
        @NotNull(message = "Date of birth is required") LocalDate dateOfBirth,
        @Pattern(regexp = "\\d{12}", message = "Aadhaar number must be 12 digits") String aadhaarNumber,
        @NotNull(message = "Gender is required") Gender gender,
        String district,
        String nationality,
        Caste caste,
        @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits") String mobileNumber,
        @Email(message = "Email must be valid") String emailId
) {
}
