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
import com.rgcet.admission.entity.ScholarshipStructure;
import com.rgcet.admission.entity.Student;
import com.rgcet.admission.entity.TuitionFeeStructure;
import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.BusRouteRepository;
import com.rgcet.admission.repository.BusStopRepository;
import com.rgcet.admission.repository.HostelRepository;
import com.rgcet.admission.repository.ScholarshipStructureRepository;
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
    private final ScholarshipStructureRepository scholarshipStructureRepository;
    private final AdmissionCategoryRepository categoryRepository;
    private final BusRouteRepository busRouteRepository;
    private final BusStopRepository busStopRepository;
    private final HostelRepository hostelRepository;

    public FeeResult compute(Student student, FeeStepRequest request) {
        Admission admission = student.getAdmission();
        if (admission == null || admission.getProgram() == null) {
            throw new IllegalStateException("Academic details (program) must be saved before fee calculation.");
        }

        Program program = admission.getProgram();
        Department department = admission.getDepartment();
        AdmissionCategory category = admission.getCategory();

        BigDecimal cutOff = request.cutOffMark();
        MeritInfo meritInfo = meritInfo(student, program, cutOff);
        cutOff = meritInfo.cutOff();
        BigDecimal meritPercent = meritInfo.meritPercent();

        TuitionFeeStructure structure = lookup(program, department, category, meritPercent);
        BigDecimal basePerYear = structure != null ? structure.getTuitionFee() : BigDecimal.ZERO;
        BigDecimal scholarshipAmount = lookupScholarship(program, department, category, meritPercent);
        BigDecimal tuitionPerYear = basePerYear.subtract(scholarshipAmount).max(BigDecimal.ZERO);

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

        return new FeeResult(cutOff, meritPercent, structure, basePerYear, scholarshipAmount, tuitionPerYear,
                duration, totalTuition, route, busStop, busFee, request.hostelRequired(), hostelFee, totalFee);
    }

    /**
     * Resolves the cut-off mark and the merit percentage used for fee/scholarship slab
     * lookup. The scale is decided by the admission program: PG and lateral-entry
     * candidates use their entered percentage (as-is), while first-year candidates use the
     * engineering cut-off derived from HSC marks (or the entered cut-off) scaled to 300.
     * Falls back to the overall HSC percentage when no cut-off is available.
     */
    private MeritInfo meritInfo(Student student, Program program, BigDecimal cutOff) {
        BigDecimal meritPercent = null;
        if (isPgOrLateral(program)) {
            meritPercent = cutOff;
        } else {
            QualifyingExam qualifyingExam = student.getQualifyingExam();
            BigDecimal engineeringCutOff = qualifyingExam != null
                    ? CutoffCalculator.engineeringCutOff(qualifyingExam.getAcademicMarks())
                    : null;
            if (engineeringCutOff != null) {
                cutOff = engineeringCutOff;
                meritPercent = CutoffCalculator.meritPercent(engineeringCutOff);
            } else if (cutOff != null) {
                meritPercent = CutoffCalculator.meritPercent(cutOff);
            }
        }
        if (meritPercent == null && student.getQualifyingExam() != null) {
            meritPercent = CutoffCalculator.overallPercentage(
                    student.getQualifyingExam().getAcademicMarks(),
                    student.getQualifyingExam().getVocationalMarks());
        }
        return new MeritInfo(cutOff, meritPercent);
    }

    /**
     * PG and lateral-entry candidates express their merit as a percentage that is used
     * as-is for slab lookup, so they are matched by program name. Any other program
     * (including first-year B.Tech) is treated as a /300 cut-off score.
     */
    private static boolean isPgOrLateral(Program program) {
        if (program == null || program.getProgramName() == null) {
            return false;
        }
        String name = program.getProgramName().trim().toLowerCase();
        return name.equals("pg") || name.contains("lateral");
    }

    private record MeritInfo(BigDecimal cutOff, BigDecimal meritPercent) {
    }

    /**
     * Finds the applicable Tuition_Fee_Structure (original/base fee) for the given
     * program/department/category. A flat row (null percentage range) is preferred;
     * otherwise the [min, max) percentage band that contains the merit score is used.
     * As a final fallback the Management default rate is returned.
     */
    public TuitionFeeStructure lookup(Program program, Department department,
                                      AdmissionCategory category, BigDecimal meritPercent) {
        if (program == null || department == null || category == null) {
            return null;
        }
        List<TuitionFeeStructure> rows =
                feeStructureRepository.findByProgramAndDepartmentAndCategory(program, department, category);
        TuitionFeeStructure flat = rows.stream()
                .filter(r -> r.getMinimumPercentage() == null && r.getMaximumPercentage() == null)
                .findFirst()
                .orElse(null);
        if (flat != null) {
            return flat;
        }
        TuitionFeeStructure match = lookupBand(rows, meritPercent);
        if (match != null) {
            return match;
        }
        AdmissionCategory management = categoryRepository.findByCategoryNameIgnoreCase("Management").orElse(null);
        if (management != null) {
            List<TuitionFeeStructure> defaults =
                    feeStructureRepository.findByProgramAndDepartmentAndCategory(program, department, management);
            TuitionFeeStructure flatDefault = defaults.stream()
                    .filter(r -> r.getMinimumPercentage() == null && r.getMaximumPercentage() == null)
                    .findFirst()
                    .orElse(null);
            return flatDefault != null ? flatDefault : lookupBand(defaults, meritPercent);
        }
        return null;
    }

    /**
     * Looks up the merit-based scholarship amount for the given program/department/category.
     * Returns zero when no scholarship configuration exists (non-eligible programs) or no
     * band matches the merit score.
     */
    public BigDecimal lookupScholarship(Program program, Department department,
                                        AdmissionCategory category, BigDecimal meritPercent) {
        if (program == null || department == null || category == null || meritPercent == null) {
            return BigDecimal.ZERO;
        }
        List<ScholarshipStructure> rows =
                scholarshipStructureRepository.findByProgramAndDepartmentAndCategory(program, department, category);
        if (rows.isEmpty()) {
            return BigDecimal.ZERO;
        }
        ScholarshipStructure match = lookupScholarshipBand(rows, meritPercent);
        return match != null && match.getScholarshipAmount() != null
                ? match.getScholarshipAmount()
                : BigDecimal.ZERO;
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

    private static ScholarshipStructure lookupScholarshipBand(List<ScholarshipStructure> rows, BigDecimal merit) {
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
