package com.rgcet.admission.dto;

import java.math.BigDecimal;
import java.util.List;

public record MasterDataDtos() {

    public record ProgramDto(Long id, String name, Integer durationYears) {
    }

    public record DepartmentDto(Long id, String name) {
    }

    public record CategoryDto(Long id, String name) {
    }

    public record BusStopDto(Long id, Integer order, String name, BigDecimal fee) {
    }

    public record BusRouteDto(Long id, String name, BigDecimal busFee, List<BusStopDto> stops) {
    }

    public record CertificateDto(Long id, String name) {
    }

    public record HostelDto(Long id, BigDecimal fee) {
    }

    public record FeeStructureDto(Long id, String program, String department, String category,
                                  BigDecimal min, BigDecimal max, BigDecimal fee) {
    }

    public record ScholarshipStructureDto(Long id, String program, String department, String category,
                                          BigDecimal min, BigDecimal max, BigDecimal scholarshipAmount) {
    }
}
