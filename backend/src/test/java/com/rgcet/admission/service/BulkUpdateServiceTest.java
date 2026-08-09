package com.rgcet.admission.service;

import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdateApplyResponse;
import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdatePreviewResponse;
import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdateRequest;
import com.rgcet.admission.dto.BulkUpdateDtos.SheetDto;
import com.rgcet.admission.entity.Admission;
import com.rgcet.admission.entity.AdmissionCategory;
import com.rgcet.admission.entity.AuditLog;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.Gender;
import com.rgcet.admission.entity.Program;
import com.rgcet.admission.entity.Student;
import com.rgcet.admission.entity.StudentStatus;
import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.AuditLogRepository;
import com.rgcet.admission.repository.BusRouteRepository;
import com.rgcet.admission.repository.BusStopRepository;
import com.rgcet.admission.repository.CertificateRepository;
import com.rgcet.admission.repository.DepartmentRepository;
import com.rgcet.admission.repository.HostelRepository;
import com.rgcet.admission.repository.ProgramRepository;
import com.rgcet.admission.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BulkUpdateServiceTest {

    private static final String APP_NO = "RGCET/2026/2001";

    private StudentRepository studentRepository;
    private AdmissionCategoryRepository categoryRepository;
    private ProgramRepository programRepository;
    private DepartmentRepository departmentRepository;
    private CertificateRepository certificateRepository;
    private HostelRepository hostelRepository;
    private BusRouteRepository busRouteRepository;
    private BusStopRepository busStopRepository;
    private AuditLogRepository auditLogRepository;
    private BulkUpdateService bulkUpdateService;

    @BeforeEach
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        categoryRepository = mock(AdmissionCategoryRepository.class);
        programRepository = mock(ProgramRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        certificateRepository = mock(CertificateRepository.class);
        hostelRepository = mock(HostelRepository.class);
        busRouteRepository = mock(BusRouteRepository.class);
        busStopRepository = mock(BusStopRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);

        BulkUpdateSchemaService schemaService = new BulkUpdateSchemaService(
                categoryRepository, programRepository, departmentRepository,
                certificateRepository, hostelRepository, busRouteRepository);
        bulkUpdateService = new BulkUpdateService(
                studentRepository, categoryRepository, programRepository, departmentRepository,
                certificateRepository, hostelRepository, busRouteRepository, busStopRepository,
                auditLogRepository, schemaService, mock(StudentService.class));
    }

    private Student activeStudent() {
        Student student = new Student();
        student.setApplicationNo(APP_NO);
        student.setStudentName("Sample Student");
        student.setGender(Gender.MALE);
        student.setMobileNumber("9840123451");
        student.setEmailId("sample@example.com");
        student.setStatus(StudentStatus.ACTIVE);
        return student;
    }

    private static Map<String, String> row(String... kv) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }

    private static BulkUpdateRequest request(String table, List<Map<String, String>> rows) {
        return new BulkUpdateRequest(List.of(new SheetDto(table, rows)));
    }

    private void givenStudent(Student student) {
        when(studentRepository.findByApplicationNoIgnoreCase(student.getApplicationNo()))
                .thenReturn(Optional.of(student));
    }

    // ---------------------------------------------------------------- preview

    @Test
    void validateReportsChangedRecord() {
        givenStudent(activeStudent());
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "gender", "FEMALE", "mobile_number", "9840123451")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        assertEquals(1, preview.summary().totalRecords());
        assertEquals(1, preview.summary().validRecords());
        assertEquals(0, preview.summary().invalidRecords());
        assertEquals(1, preview.summary().changedRecords());
        assertEquals(0, preview.summary().unchangedRecords());
        assertEquals(1, preview.records().size());
        assertTrue(preview.records().get(0).valid());
        assertEquals(1, preview.records().get(0).changes().size());
        assertEquals("gender", preview.records().get(0).changes().get(0).fieldName());
        assertEquals("MALE", preview.records().get(0).changes().get(0).oldValue());
        assertEquals("FEMALE", preview.records().get(0).changes().get(0).newValue());
    }

    @Test
    void validateReportsUnchangedRecord() {
        Student student = activeStudent();
        givenStudent(student);
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "gender", "MALE", "mobile_number", "9840123451")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        assertEquals(1, preview.summary().unchangedRecords());
        assertTrue(preview.records().get(0).valid());
        assertTrue(preview.records().get(0).changes().isEmpty());
    }

    @Test
    void validateFlagsUnknownColumn() {
        givenStudent(activeStudent());
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "not_a_column", "value")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        assertEquals(1, preview.summary().invalidRecords());
        assertTrue(preview.records().get(0).errors().stream()
                .anyMatch(e -> e.contains("unknown column 'not_a_column'")));
    }

    @Test
    void validateFlagsInvalidEnumValue() {
        givenStudent(activeStudent());
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "gender", "ALIEN")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        assertEquals(1, preview.summary().invalidRecords());
        assertTrue(preview.records().get(0).errors().stream()
                .anyMatch(e -> e.contains("invalid value 'ALIEN'")));
    }

    @Test
    void validateFlagsInvalidMobileNumber() {
        givenStudent(activeStudent());
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "mobile_number", "123")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        assertEquals(1, preview.summary().invalidRecords());
        assertTrue(preview.records().get(0).errors().stream()
                .anyMatch(e -> e.contains("must be exactly 10 digits")));
    }

    @Test
    void validateFlagsMissingApplicationNumber() {
        BulkUpdateRequest req = request("student_details", List.of(row("gender", "FEMALE")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        assertEquals(1, preview.summary().invalidRecords());
        assertTrue(preview.records().get(0).errors().stream()
                .anyMatch(e -> e.contains("missing application_no")));
    }

    @Test
    void validateFlagsUnknownStudent() {
        when(studentRepository.findByApplicationNoIgnoreCase(APP_NO)).thenReturn(Optional.empty());
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "gender", "FEMALE")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        assertEquals(1, preview.summary().invalidRecords());
        assertTrue(preview.records().get(0).errors().stream()
                .anyMatch(e -> e.contains("Student not found")));
    }

    @Test
    void validateFlagsArchivedStudent() {
        Student student = activeStudent();
        student.setStatus(StudentStatus.ARCHIVED);
        givenStudent(student);
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "gender", "FEMALE")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        assertEquals(1, preview.summary().invalidRecords());
        assertTrue(preview.records().get(0).errors().stream()
                .anyMatch(e -> e.contains("Archived students cannot be updated")));
    }

    @Test
    void validateFlagsDuplicateRowsForSameTable() {
        givenStudent(activeStudent());
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "gender", "FEMALE"),
                row("application_no", APP_NO, "district", "Chennai")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        assertEquals(1, preview.summary().invalidRecords());
        assertTrue(preview.records().get(0).errors().stream()
                .anyMatch(e -> e.contains("multiple rows supplied for table 'student_details'")));
    }

    @Test
    void validateResolvesAliasesAndEnforcesAcademicRules() {
        givenStudent(activeStudent());

        Program firstYear = new Program();
        firstYear.setProgramName("First Year B.Tech");
        when(programRepository.findAll()).thenReturn(List.of(firstYear));

        BulkUpdateRequest req = request("admission", List.of(
                row("application_no", APP_NO, "program_id", "FIRST YEAR")));

        BulkUpdatePreviewResponse preview = bulkUpdateService.validate(req);

        // Alias "FIRST YEAR" resolves, but a bare admission row alone has no invalid data
        assertEquals(1, preview.summary().validRecords());

        // Now a diploma sheet for the same student must be rejected (HSC-only program)
        BulkUpdateRequest withDiploma = new BulkUpdateRequest(List.of(
                new SheetDto("admission", List.of(
                        row("application_no", APP_NO, "program_id", "FIRST YEAR"))),
                new SheetDto("diploma_details", List.of(
                        row("application_no", APP_NO, "diploma", "Diploma in CSE")))));
        BulkUpdatePreviewResponse withDiplomaPreview = bulkUpdateService.validate(withDiploma);

        assertEquals(1, withDiplomaPreview.summary().invalidRecords());
        assertTrue(withDiplomaPreview.records().get(0).errors().stream()
                .anyMatch(e -> e.contains("diploma_details is not applicable for program First Year B.Tech")));
    }

    // ---------------------------------------------------------------- apply

    @Test
    void applyPersistsChangesAndWritesAuditLog() {
        Student student = activeStudent();
        givenStudent(student);
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "gender", "FEMALE", "mobile_number", "9840123451")));

        BulkUpdateApplyResponse result = bulkUpdateService.apply(req);

        assertEquals(1, result.summary().totalRecords());
        assertEquals(1, result.summary().updatedRecords());
        assertEquals("UPDATED", result.results().get(0).status());
        assertEquals(Gender.FEMALE, student.getGender());
        verify(studentRepository).save(student);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void applySkipsUnchangedRecordWithoutSaving() {
        Student student = activeStudent();
        givenStudent(student);
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "gender", "MALE", "mobile_number", "9840123451")));

        BulkUpdateApplyResponse result = bulkUpdateService.apply(req);

        assertEquals(1, result.summary().skippedRecords());
        assertEquals("SKIPPED", result.results().get(0).status());
        verify(studentRepository, never()).save(any(Student.class));
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void applyFailsOnInvalidRecordAndDoesNotPersist() {
        Student student = activeStudent();
        givenStudent(student);
        BulkUpdateRequest req = request("student_details", List.of(
                row("application_no", APP_NO, "gender", "ALIEN")));

        BulkUpdateApplyResponse result = bulkUpdateService.apply(req);

        assertEquals(1, result.summary().failedRecords());
        assertEquals("FAILED", result.results().get(0).status());
        assertEquals(Gender.MALE, student.getGender());
        verify(studentRepository, never()).save(any(Student.class));
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void applyRejectsUnknownReference() {
        Student student = activeStudent();
        Admission admission = new Admission();
        admission.setStudent(student);
        student.setAdmission(admission);
        givenStudent(student);

        AdmissionCategory category = new AdmissionCategory();
        category.setCategoryId(1L);
        category.setCategoryName("CENTAC");
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        BulkUpdateRequest req = request("admission", List.of(
                row("application_no", APP_NO, "category_id", "Management")));

        BulkUpdateApplyResponse result = bulkUpdateService.apply(req);

        // Only "CENTAC" exists -> "Management" does not resolve to any master record
        assertEquals(1, result.summary().failedRecords());
        assertFalse(result.results().get(0).errors().isEmpty());
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void applyResolvesValidReferenceAndAudits() {
        Student student = activeStudent();
        Admission admission = new Admission();
        admission.setStudent(student);
        student.setAdmission(admission);
        givenStudent(student);

        AdmissionCategory category = new AdmissionCategory();
        category.setCategoryId(1L);
        category.setCategoryName("Management");
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        BulkUpdateRequest req = request("admission", List.of(
                row("application_no", APP_NO, "category_id", "MANAGEMENT")));

        BulkUpdateApplyResponse result = bulkUpdateService.apply(req);

        assertEquals(1, result.summary().updatedRecords());
        assertEquals("UPDATED", result.results().get(0).status());
        assertEquals(category, admission.getCategory());
        verify(studentRepository).save(student);
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
