package com.rgcet.admission.dto;

import java.math.BigDecimal;

public record QualifyingExamStepRequest(
        String institutionName,
        String institutionPlace,
        String examPassed,
        String monthYearPassing,
        BigDecimal sslcPercentage,
        String sslcRegisterNumber,
        BigDecimal hscPercentage,
        String hscRegisterNumber
) {
}
