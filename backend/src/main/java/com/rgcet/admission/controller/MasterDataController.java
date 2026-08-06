package com.rgcet.admission.controller;

import com.rgcet.admission.dto.MasterDataDtos.BusRouteDto;
import com.rgcet.admission.dto.MasterDataDtos.CategoryDto;
import com.rgcet.admission.dto.MasterDataDtos.CertificateDto;
import com.rgcet.admission.dto.MasterDataDtos.DepartmentDto;
import com.rgcet.admission.dto.MasterDataDtos.FeeStructureDto;
import com.rgcet.admission.dto.MasterDataDtos.HostelDto;
import com.rgcet.admission.dto.MasterDataDtos.ProgramDto;
import com.rgcet.admission.dto.MasterDataDtos.ScholarshipStructureDto;
import com.rgcet.admission.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;

    @GetMapping("/programs")
    public List<ProgramDto> programs() {
        return masterDataService.getPrograms();
    }

    @GetMapping("/programs/{id}/departments")
    public List<DepartmentDto> departmentsByProgram(@PathVariable Long id) {
        return masterDataService.getDepartmentsByProgram(id);
    }

    @GetMapping("/departments")
    public List<DepartmentDto> departments() {
        return masterDataService.getDepartments();
    }

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return masterDataService.getCategories();
    }

    @GetMapping("/certificates")
    public List<CertificateDto> certificates() {
        return masterDataService.getCertificates();
    }

    @GetMapping("/hostels")
    public List<HostelDto> hostels() {
        return masterDataService.getHostels();
    }

    @GetMapping("/bus-routes")
    public List<BusRouteDto> busRoutes() {
        return masterDataService.getBusRoutes();
    }

    @GetMapping("/fee-structures")
    public List<FeeStructureDto> feeStructures() {
        return masterDataService.getFeeStructures();
    }

    @GetMapping("/scholarship-structures")
    public List<ScholarshipStructureDto> scholarshipStructures() {
        return masterDataService.getScholarshipStructures();
    }
}
