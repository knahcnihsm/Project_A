package com.rgcet.admission.service;

import com.rgcet.admission.entity.BusRoute;
import com.rgcet.admission.entity.BusStop;
import com.rgcet.admission.entity.TuitionFeeStructure;

import java.math.BigDecimal;

public record FeeResult(
        BigDecimal cutOffMark,
        BigDecimal meritPercent,
        TuitionFeeStructure structure,
        BigDecimal tuitionFeePerYear,
        Integer courseDurationYears,
        BigDecimal totalTuitionFee,
        BusRoute route,
        BusStop busStop,
        BigDecimal busFee,
        boolean hostelRequired,
        BigDecimal hostelFee,
        BigDecimal totalFee
) {
}
