package com.rgcet.admission.dto;

import java.math.BigDecimal;

public record FeeStepRequest(
        BigDecimal cutOffMark,
        boolean busRequired,
        Long routeId,
        Long busStopId,
        boolean hostelRequired
) {
}
