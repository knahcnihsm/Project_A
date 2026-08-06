package com.rgcet.admission;

import com.rgcet.admission.dto.FeeStepRequest;
import com.rgcet.admission.entity.Admission;
import com.rgcet.admission.entity.AdmissionCategory;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.HSCAcademicMark;
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
import com.rgcet.admission.service.FeeResult;
import com.rgcet.admission.service.FeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeeServiceTest {

    private TuitionFeeStructureRepository feeRepo;
    private ScholarshipStructureRepository scholarshipRepo;
    private AdmissionCategoryRepository categoryRepo;
    private FeeService feeService;

    private static final BigDecimal CSE_FEE = new BigDecimal("100000");
    private static final BigDecimal IT_FEE = new BigDecimal("80000");

    @BeforeEach
    void setUp() {
        feeRepo = mock(TuitionFeeStructureRepository.class);
        scholarshipRepo = mock(ScholarshipStructureRepository.class);
        categoryRepo = mock(AdmissionCategoryRepository.class);
        feeService = new FeeService(feeRepo, scholarshipRepo, categoryRepo,
                mock(BusRouteRepository.class), mock(BusStopRepository.class), mock(HostelRepository.class));
    }

    private TuitionFeeStructure structure(BigDecimal min, BigDecimal max, int fee) {
        TuitionFeeStructure s = new TuitionFeeStructure();
        s.setMinimumPercentage(min);
        s.setMaximumPercentage(max);
        s.setTuitionFee(BigDecimal.valueOf(fee));
        return s;
    }

    private ScholarshipStructure scholarship(int min, int max, int amount) {
        ScholarshipStructure s = new ScholarshipStructure();
        s.setMinimumPercentage(BigDecimal.valueOf(min));
        s.setMaximumPercentage(BigDecimal.valueOf(max));
        s.setScholarshipAmount(BigDecimal.valueOf(amount));
        return s;
    }

    private Student studentWithProgram(Program program, Department department, AdmissionCategory category) {
        Admission admission = new Admission();
        admission.setProgram(program);
        admission.setDepartment(department);
        admission.setCategory(category);
        Student student = new Student();
        student.setAdmission(admission);
        return student;
    }

    private Program program(int duration) {
        Program program = new Program();
        program.setDurationYears(duration);
        return program;
    }

    @Test
    void lookupPicksBandContainingMerit() {
        List<TuitionFeeStructure> rows = List.of(
                structure(new BigDecimal("0"), new BigDecimal("60"), 100000),
                structure(new BigDecimal("60"), new BigDecimal("80"), 90000),
                structure(new BigDecimal("80"), new BigDecimal("100"), 80000));

        assertEquals(100000, FeeService.lookupBand(rows, new BigDecimal("20")).getTuitionFee().intValue());
        // 60 falls into the 60-80 band ([min, max))
        assertEquals(90000, FeeService.lookupBand(rows, new BigDecimal("60")).getTuitionFee().intValue());
        assertEquals(90000, FeeService.lookupBand(rows, new BigDecimal("79.99")).getTuitionFee().intValue());
        assertEquals(80000, FeeService.lookupBand(rows, new BigDecimal("80")).getTuitionFee().intValue());
        // 100 belongs to the top band
        assertEquals(80000, FeeService.lookupBand(rows, new BigDecimal("100")).getTuitionFee().intValue());
    }

    @Test
    void lookupWithNullMeritPrefersFlatRow() {
        List<TuitionFeeStructure> rows = List.of(
                structure(null, null, 100000),
                structure(new BigDecimal("80"), new BigDecimal("100"), 80000));
        assertEquals(100000, FeeService.lookupBand(rows, null).getTuitionFee().intValue());
    }

    @Test
    void lookupPrefersFlatBaseRowOverBandedRows() {
        Program program = new Program();
        Department department = new Department();
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(
                        structure(new BigDecimal("0"), new BigDecimal("60"), 100000),
                        structure(new BigDecimal("60"), new BigDecimal("80"), 90000),
                        structure(null, null, 100000),
                        structure(new BigDecimal("80"), new BigDecimal("100"), 80000)));

        assertEquals(100000, feeService.lookup(program, department, management, new BigDecimal("85"))
                .getTuitionFee().intValue());
    }

    @Test
    void lookupFallsBackToManagementDefaultWhenNoBandMatches() {
        Program program = new Program();
        Department department = new Department();
        AdmissionCategory centac = new AdmissionCategory();
        centac.setCategoryName("CENTAC");
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, centac))
                .thenReturn(List.of());
        when(categoryRepo.findByCategoryNameIgnoreCase("Management")).thenReturn(Optional.of(management));
        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, 100000)));

        TuitionFeeStructure result = feeService.lookup(program, department, centac, new BigDecimal("25"));
        assertEquals(100000, result.getTuitionFee().intValue());
    }

    @Test
    void lookupReturnsNullWhenNothingMatches() {
        Program program = new Program();
        Department department = new Department();
        AdmissionCategory centac = new AdmissionCategory();
        centac.setCategoryName("CENTAC");

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, centac))
                .thenReturn(List.of());
        when(categoryRepo.findByCategoryNameIgnoreCase("Management")).thenReturn(Optional.empty());

        assertNull(feeService.lookup(program, department, centac, new BigDecimal("25")));
    }

    @Test
    void computeAppliesScholarshipForEligibleFirstYear() {
        Program program = program(4);
        Department department = new Department();
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");
        Student student = studentWithProgram(program, department, management);

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, CSE_FEE.intValue())));
        when(scholarshipRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(scholarshipSlabs());

        // cut-off 240 / 300 -> 80% merit
        FeeResult result = feeService.compute(student, new FeeStepRequest(new BigDecimal("240"), false, null, null, false));

        assertEquals(0, new BigDecimal("80.00").compareTo(result.meritPercent()));
        assertEquals(0, CSE_FEE.compareTo(result.originalTuitionFee()));
        assertEquals(0, new BigDecimal("20000").compareTo(result.scholarshipAmount()));
        assertEquals(0, new BigDecimal("80000").compareTo(result.tuitionFeePerYear()));
        assertEquals(0, new BigDecimal("320000").compareTo(result.totalTuitionFee()));
    }

    @Test
    void computeAppliesScholarshipSlabsByMeritBand() {
        Program program = program(4);
        Department department = new Department();
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");
        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, CSE_FEE.intValue())));
        when(scholarshipRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(scholarshipSlabs());

        // 255 / 300 -> 85% -> 20,000
        FeeResult high = feeService.compute(studentWithProgram(program, department, management),
                new FeeStepRequest(new BigDecimal("255"), false, null, null, false));
        assertEquals(0, new BigDecimal("20000").compareTo(high.scholarshipAmount()));

        // 210 / 300 -> 70% -> 10,000
        FeeResult mid = feeService.compute(studentWithProgram(program, department, management),
                new FeeStepRequest(new BigDecimal("210"), false, null, null, false));
        assertEquals(0, new BigDecimal("10000").compareTo(mid.scholarshipAmount()));

        // 150 / 300 -> 50% -> no scholarship
        FeeResult low = feeService.compute(studentWithProgram(program, department, management),
                new FeeStepRequest(new BigDecimal("150"), false, null, null, false));
        assertEquals(0, BigDecimal.ZERO.compareTo(low.scholarshipAmount()));
        assertEquals(0, CSE_FEE.compareTo(low.tuitionFeePerYear()));
    }

    @Test
    void computeUsesEngineeringCutoffFromHscMarks() {
        Program program = program(4);
        Department department = new Department();
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");

        Student student = studentWithProgram(program, department, management);
        QualifyingExam exam = new QualifyingExam();
        exam.setStudent(student);
        exam.getAcademicMarks().add(academicMark("Maths", 100, 95));
        exam.getAcademicMarks().add(academicMark("Physics", 100, 90));
        exam.getAcademicMarks().add(academicMark("Chemistry", 100, 80));
        exam.getAcademicMarks().add(academicMark("Computer Science", 100, 85));
        student.setQualifyingExam(exam);

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, CSE_FEE.intValue())));
        when(scholarshipRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(scholarshipSlabs());

        // best science subject = 85 -> cut-off 95 + 90 + 85 = 270 / 300 -> 90% -> 20,000
        FeeResult result = feeService.compute(student, new FeeStepRequest(new BigDecimal("0"), false, null, null, false));

        assertEquals(0, new BigDecimal("90.00").compareTo(result.meritPercent()));
        assertEquals(0, new BigDecimal("270").compareTo(result.cutOffMark()));
        assertEquals(0, new BigDecimal("20000").compareTo(result.scholarshipAmount()));
        assertEquals(0, new BigDecimal("80000").compareTo(result.tuitionFeePerYear()));
    }

    @Test
    void computeNormalisesHscMarksToPercentagesForCutoff() {
        Program program = program(4);
        Department department = new Department();
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");

        Student student = studentWithProgram(program, department, management);
        QualifyingExam exam = new QualifyingExam();
        exam.setStudent(student);
        exam.getAcademicMarks().add(academicMark("Maths", 200, 190));
        exam.getAcademicMarks().add(academicMark("Physics", 200, 180));
        exam.getAcademicMarks().add(academicMark("Computer Science", 200, 170));
        student.setQualifyingExam(exam);

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, CSE_FEE.intValue())));
        when(scholarshipRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(scholarshipSlabs());

        // 190/200=95 + 180/200=90 + 170/200=85 -> cutoff 270 / 300 -> 90% -> 20,000
        FeeResult result = feeService.compute(student, new FeeStepRequest(new BigDecimal("0"), false, null, null, false));

        assertEquals(0, new BigDecimal("270").compareTo(result.cutOffMark()));
        assertEquals(0, new BigDecimal("90.00").compareTo(result.meritPercent()));
        assertEquals(0, new BigDecimal("20000").compareTo(result.scholarshipAmount()));
        assertEquals(0, new BigDecimal("80000").compareTo(result.tuitionFeePerYear()));
    }

    @Test
    void computeUsesMainSubjectPercentageForPg() {
        Program program = program(2);
        program.setProgramName("PG");
        Department department = new Department();
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");

        Student student = studentWithProgram(program, department, management);

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, CSE_FEE.intValue())));
        when(scholarshipRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(scholarshipSlabs());

        // PG merit is the entered percentage as-is, even without a pgQualification entity
        FeeResult result = feeService.compute(student, new FeeStepRequest(new BigDecimal("85"), false, null, null, false));

        assertEquals(0, new BigDecimal("85.00").compareTo(result.meritPercent()));
        assertEquals(0, new BigDecimal("20000").compareTo(result.scholarshipAmount()));
        assertEquals(0, new BigDecimal("80000").compareTo(result.tuitionFeePerYear()));
        assertEquals(0, new BigDecimal("160000").compareTo(result.totalTuitionFee()));
    }

    @Test
    void computeUsesEnteredPercentageForLateralEntry() {
        Program program = program(3);
        program.setProgramName("Second Year B.Tech (Lateral Entry)");
        Department department = new Department();
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");

        Student student = studentWithProgram(program, department, management);

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, CSE_FEE.intValue())));
        when(scholarshipRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(scholarshipSlabs());

        // Lateral entry: percentage is used as-is (no /300 scaling)
        FeeResult result = feeService.compute(student, new FeeStepRequest(new BigDecimal("70"), false, null, null, false));

        assertEquals(0, new BigDecimal("70.00").compareTo(result.meritPercent()));
        assertEquals(0, new BigDecimal("10000").compareTo(result.scholarshipAmount()));
        assertEquals(0, new BigDecimal("90000").compareTo(result.tuitionFeePerYear()));
        assertEquals(0, new BigDecimal("270000").compareTo(result.totalTuitionFee()));
    }

    @Test
    void computeAppliesNoScholarshipForNonEligibleProgram() {
        Program program = program(4);
        Department department = new Department();
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");

        Student student = studentWithProgram(program, department, management);

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, IT_FEE.intValue())));
        when(scholarshipRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of());

        FeeResult result = feeService.compute(student, new FeeStepRequest(new BigDecimal("240"), false, null, null, false));

        assertEquals(0, BigDecimal.ZERO.compareTo(result.scholarshipAmount()));
        assertEquals(0, IT_FEE.compareTo(result.tuitionFeePerYear()));
        assertEquals(0, new BigDecimal("320000").compareTo(result.totalTuitionFee()));
    }

    @Test
    void computeChargesFullFeeForBelowFortyMerit() {
        Program program = program(4);
        Department department = new Department();
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, CSE_FEE.intValue())));
        when(scholarshipRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(scholarshipSlabs());

        FeeResult result = feeService.compute(studentWithProgram(program, department, management),
                new FeeStepRequest(new BigDecimal("105"), false, null, null, false));

        assertEquals(0, new BigDecimal("35.00").compareTo(result.meritPercent()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.scholarshipAmount()));
        assertEquals(0, CSE_FEE.compareTo(result.tuitionFeePerYear()));
    }

    private List<ScholarshipStructure> scholarshipSlabs() {
        return List.of(
                scholarship(0, 40, 0),
                scholarship(40, 60, 0),
                scholarship(60, 80, 10000),
                scholarship(80, 100, 20000));
    }

    private HSCAcademicMark academicMark(String subject, int max, int obtained) {
        HSCAcademicMark mark = new HSCAcademicMark();
        mark.setSubjectName(subject);
        mark.setMaximumMarks(BigDecimal.valueOf(max));
        mark.setMarksObtained(BigDecimal.valueOf(obtained));
        return mark;
    }
}
