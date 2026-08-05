package com.rgcet.admission.dto;

import java.math.BigDecimal;

public record PgStepRequest(
        String universityName,
        String universityPlace,
        String institutionName,
        String institutionPlace,
        String examPassed,
        String monthYearPassing,
        BigDecimal totalPercentage,
        BigDecimal mainSubjectPercentage,
        String degreeRegistrationNumber
) {
}
