package com.rgcet.admission.service;

import com.rgcet.admission.dto.FeeStepRequest;
import com.rgcet.admission.entity.Admission;
import com.rgcet.admission.entity.AdmissionCategory;
import com.rgcet.admission.entity.BusRoute;
import com.rgcet.admission.entity.BusStop;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.Hostel;
import com.rgcet.admission.entity.Program;
import com.rgcet.admission.entity.QualifyingExam;
import com.rgcet.admission.entity.Student;
import com.rgcet.admission.entity.TuitionFeeStructure;
import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.BusRouteRepository;
import com.rgcet.admission.repository.BusStopRepository;
import com.rgcet.admission.repository.HostelRepository;
import com.rgcet.admission.repository.TuitionFeeStructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final TuitionFeeStructureRepository feeStructureRepository;
    private final AdmissionCategoryRepository categoryRepository;
    private final BusRouteRepository busRouteRepository;
    private final BusStopRepository busStopRepository;
    private final HostelRepository hostelRepository;

    public FeeResult compute(Student student, FeeStepRequest request) {
        Admission admission = student.getAdmission();
        if (admission == null || admission.getProgram() == null) {
            throw new IllegalStateException("Academic details (program) must be saved before fee calculation.");
        }

        BigDecimal cutOff = request.cutOffMark();
        BigDecimal meritPercent = null;

        QualifyingExam qualifyingExam = student.getQualifyingExam();
        if (qualifyingExam != null) {
            BigDecimal engineeringCutOff = CutoffCalculator.engineeringCutOff(qualifyingExam.getAcademicMarks());
            if (engineeringCutOff != null) {
                cutOff = engineeringCutOff;
                meritPercent = CutoffCalculator.meritPercent(engineeringCutOff);
            } else if (meritPercent == null) {
                meritPercent = CutoffCalculator.overallPercentage(
                        qualifyingExam.getAcademicMarks(), qualifyingExam.getVocationalMarks());
            }
        }

        Program program = admission.getProgram();
        Department department = admission.getDepartment();
        AdmissionCategory category = admission.getCategory();
        TuitionFeeStructure structure = lookup(program, department, category, meritPercent);

        BigDecimal tuitionPerYear = structure != null ? structure.getTuitionFee() : BigDecimal.ZERO;
        Integer duration = program.getDurationYears() != null ? program.getDurationYears() : 0;
        BigDecimal totalTuition = tuitionPerYear.multiply(BigDecimal.valueOf(duration));

        BusRoute route = null;
        BusStop busStop = null;
        BigDecimal busFee = BigDecimal.ZERO;
        if (request.busRequired() && request.busStopId() != null) {
            busStop = busStopRepository.findById(request.busStopId()).orElse(null);
            route = request.routeId() != null
                    ? busRouteRepository.findById(request.routeId()).orElse(null)
                    : (busStop != null ? busStop.getRoute() : null);
            if (busStop != null) {
                busFee = busStop.getTransportFee() != null ? busStop.getTransportFee() : BigDecimal.ZERO;
            }
        }

        BigDecimal hostelFee = BigDecimal.ZERO;
        Hostel hostel = null;
        if (request.hostelRequired()) {
            hostel = hostelRepository.findAll().stream().findFirst().orElse(null);
            if (hostel != null) {
                hostelFee = hostel.getHostelFee() != null ? hostel.getHostelFee() : BigDecimal.ZERO;
            }
        }

        BigDecimal totalFee = totalTuition.add(busFee).add(hostelFee);

        return new FeeResult(cutOff, meritPercent, structure, tuitionPerYear, duration, totalTuition,
                route, busStop, busFee, request.hostelRequired(), hostelFee, totalFee);
    }

    /**
     * Finds the applicable Tuition_Fee_Structure for the given program/department/category.
     * Uses the [min, max) percentage band that contains the merit score; when no band matches,
     * falls back to the Management default rate (nullable percentage range).
     */
    public TuitionFeeStructure lookup(Program program, Department department,
                                      AdmissionCategory category, BigDecimal meritPercent) {
        if (program == null || department == null || category == null) {
            return null;
        }
        List<TuitionFeeStructure> rows =
                feeStructureRepository.findByProgramAndDepartmentAndCategory(program, department, category);
        TuitionFeeStructure match = lookupBand(rows, meritPercent);
        if (match != null) {
            return match;
        }
        AdmissionCategory management = categoryRepository.findByCategoryNameIgnoreCase("Management").orElse(null);
        if (management != null) {
            List<TuitionFeeStructure> defaults =
                    feeStructureRepository.findByProgramAndDepartmentAndCategory(program, department, management);
            return defaults.stream()
                    .filter(r -> r.getMinimumPercentage() == null && r.getMaximumPercentage() == null)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static TuitionFeeStructure lookupBand(List<TuitionFeeStructure> rows, BigDecimal merit) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        if (merit == null) {
            return rows.stream()
                    .filter(r -> r.getMinimumPercentage() == null
                            || r.getMinimumPercentage().signum() == 0)
                    .findFirst()
                    .orElse(null);
        }
        return rows.stream()
                .filter(r -> r.getMaximumPercentage() != null
                        && merit.compareTo(r.getMaximumPercentage()) < 0)
                .min(Comparator.comparing(r -> r.getMaximumPercentage(),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseGet(() -> rows.stream()
                        .filter(r -> r.getMaximumPercentage() != null
                                && merit.compareTo(r.getMaximumPercentage()) == 0)
                        .max(Comparator.comparing(r -> r.getMaximumPercentage(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElseGet(() -> rows.stream()
                                .filter(r -> r.getMaximumPercentage() == null
                                        && (r.getMinimumPercentage() == null
                                        || merit.compareTo(r.getMinimumPercentage()) >= 0))
                                .findFirst()
                                .orElse(null)));
    }
}
