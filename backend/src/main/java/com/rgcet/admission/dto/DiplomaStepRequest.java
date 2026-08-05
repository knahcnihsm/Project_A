package com.rgcet.admission.dto;

import java.math.BigDecimal;

public record DiplomaStepRequest(
        String diplomaCourse,
        String institutionName,
        String board,
        BigDecimal secondYearPercentage,
        BigDecimal thirdYearPercentage,
        BigDecimal aggregatePercentage
) {
}
