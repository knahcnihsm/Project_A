package com.rgcet.admission.service;

import com.rgcet.admission.common.ResourceNotFoundException;
import com.rgcet.admission.dto.MasterDataDtos.BusRouteDto;
import com.rgcet.admission.dto.MasterDataDtos.BusStopDto;
import com.rgcet.admission.dto.MasterDataDtos.CategoryDto;
import com.rgcet.admission.dto.MasterDataDtos.CertificateDto;
import com.rgcet.admission.dto.MasterDataDtos.DepartmentDto;
import com.rgcet.admission.dto.MasterDataDtos.FeeStructureDto;
import com.rgcet.admission.dto.MasterDataDtos.HostelDto;
import com.rgcet.admission.dto.MasterDataDtos.ProgramDto;
import com.rgcet.admission.dto.MasterDataDtos.ScholarshipStructureDto;
import com.rgcet.admission.entity.BusRoute;
import com.rgcet.admission.entity.BusStop;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.Program;
import com.rgcet.admission.entity.ScholarshipStructure;
import com.rgcet.admission.entity.TuitionFeeStructure;
import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.BusRouteRepository;
import com.rgcet.admission.repository.CertificateRepository;
import com.rgcet.admission.repository.DepartmentRepository;
import com.rgcet.admission.repository.HostelRepository;
import com.rgcet.admission.repository.ProgramRepository;
import com.rgcet.admission.repository.ScholarshipStructureRepository;
import com.rgcet.admission.repository.TuitionFeeStructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MasterDataService {

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final AdmissionCategoryRepository categoryRepository;
    private final CertificateRepository certificateRepository;
    private final HostelRepository hostelRepository;
    private final BusRouteRepository busRouteRepository;
    private final TuitionFeeStructureRepository feeStructureRepository;
    private final ScholarshipStructureRepository scholarshipStructureRepository;

    public List<ProgramDto> getPrograms() {
        return programRepository.findAll().stream()
                .map(p -> new ProgramDto(p.getProgramId(), p.getProgramName(), p.getDurationYears()))
                .toList();
    }

    public List<DepartmentDto> getDepartments() {
        return departmentRepository.findAll().stream()
                .map(d -> new DepartmentDto(d.getDepartmentId(), d.getDepartmentName()))
                .toList();
    }

    public List<DepartmentDto> getDepartmentsByProgram(Long programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found: " + programId));
        Set<Department> departments = new LinkedHashSet<>();
        feeStructureRepository.findByProgram(program).stream()
                .map(TuitionFeeStructure::getDepartment)
                .filter(d -> d != null)
                .forEach(departments::add);
        if (departments.isEmpty()) {
            departments.addAll(departmentRepository.findAll());
        }
        return departments.stream()
                .map(d -> new DepartmentDto(d.getDepartmentId(), d.getDepartmentName()))
                .toList();
    }

    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryDto(c.getCategoryId(), c.getCategoryName()))
                .toList();
    }

    public List<CertificateDto> getCertificates() {
        return certificateRepository.findAll().stream()
                .map(c -> new CertificateDto(c.getCertificateId(), c.getCertificateName()))
                .toList();
    }

    public List<HostelDto> getHostels() {
        return hostelRepository.findAll().stream()
                .map(h -> new HostelDto(h.getHostelId(), h.getHostelFee()))
                .toList();
    }

    public List<BusRouteDto> getBusRoutes() {
        return busRouteRepository.findAll().stream()
                .map(this::toRouteDto)
                .toList();
    }

    public List<FeeStructureDto> getFeeStructures() {
        return feeStructureRepository.findAll().stream()
                .map(f -> new FeeStructureDto(
                        f.getFeeStructureId(),
                        f.getProgram() == null ? null : f.getProgram().getProgramName(),
                        f.getDepartment() == null ? null : f.getDepartment().getDepartmentName(),
                        f.getCategory() == null ? null : f.getCategory().getCategoryName(),
                        f.getMinimumPercentage(), f.getMaximumPercentage(), f.getTuitionFee()))
                .toList();
    }

    public List<ScholarshipStructureDto> getScholarshipStructures() {
        return scholarshipStructureRepository.findAll().stream()
                .map(f -> new ScholarshipStructureDto(
                        f.getScholarshipStructureId(),
                        f.getProgram() == null ? null : f.getProgram().getProgramName(),
                        f.getDepartment() == null ? null : f.getDepartment().getDepartmentName(),
                        f.getCategory() == null ? null : f.getCategory().getCategoryName(),
                        f.getMinimumPercentage(), f.getMaximumPercentage(), f.getScholarshipAmount()))
                .toList();
    }

    private BusRouteDto toRouteDto(BusRoute route) {
        List<BusStopDto> stops = route.getStops().stream()
                .sorted(Comparator.comparing(BusStop::getStopOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(s -> new BusStopDto(s.getBusStopId(), s.getStopOrder(), s.getStopName(), s.getTransportFee()))
                .toList();
        return new BusRouteDto(route.getRouteId(), route.getRouteName(), route.getBusFee(), stops);
    }
}
