package com.rgcet.admission;

import com.rgcet.admission.dto.bulk.BulkCommitResponse;
import com.rgcet.admission.dto.bulk.BulkIssue;
import com.rgcet.admission.dto.bulk.BulkValidationResponse;
import com.rgcet.admission.dto.bulk.BulkWorkbookRequest;
import com.rgcet.admission.entity.Address;
import com.rgcet.admission.entity.AddressType;
import com.rgcet.admission.entity.Admission;
import com.rgcet.admission.entity.AdmissionCategory;
import com.rgcet.admission.entity.BulkUploadLog;
import com.rgcet.admission.entity.BulkUploadStatus;
import com.rgcet.admission.entity.Caste;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.Gender;
import com.rgcet.admission.entity.ParentDetails;
import com.rgcet.admission.entity.Program;
import com.rgcet.admission.entity.Student;
import com.rgcet.admission.entity.StudentFee;
import com.rgcet.admission.entity.StudentStatus;
import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.BulkUploadLogRepository;
import com.rgcet.admission.repository.DepartmentRepository;
import com.rgcet.admission.repository.ProgramRepository;
import com.rgcet.admission.repository.StudentRepository;
import com.rgcet.admission.service.BulkStudentUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BulkStudentUpdateServiceTest {

    @Autowired
    private BulkStudentUpdateService bulkService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ProgramRepository programRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private AdmissionCategoryRepository categoryRepository;
    @Autowired
    private BulkUploadLogRepository bulkUploadLogRepository;

    @Test
    void validWorkbookCommitsEverythingAndPersistsChanges() {
        Student student = newStudent("RGCET/2026/9001", "26BTECH901", "Original Student", StudentStatus.ACTIVE);

        BulkWorkbookRequest request = request("bulk-valid.xlsx",
                List.of(new BulkWorkbookRequest.PersonalRow(2, "RGCET/2026/9101", "26BTECH901",
                        "Updated Student Name", "2007-05-10", "FEMALE", "987654321002",
                        "Indian", "Karaikal", "OC")),
                List.of(new BulkWorkbookRequest.ParentRow(2, null, "26BTECH901",
                        "New Father Name", "9012345678", "Farmer", "450000")),
                List.of(new BulkWorkbookRequest.CommunicationRow(2, null, "26BTECH901",
                        "New Comm Address", "605010", "9443300101", "9550100202", "new.comm@example.com")),
                List.of(new BulkWorkbookRequest.PermanentRow(2, null, "26BTECH901",
                        "New Perm Address", "605020", null, null, null)),
                List.of(new BulkWorkbookRequest.AcademicRow(2, null, "26BTECH901",
                        "Management", "First Year B.Tech", "Information Technology (IT)",
                        "2026-2031", "2026-07-01")),
                List.of(new BulkWorkbookRequest.QualifyingExamRow(2, null, "26BTECH901",
                        "New School", "Puducherry", "HSC", "May 2025",
                        "85.5", "SSLC901", null, "HSC901")),
                List.of(hscRow("26BTECH901")),
                List.of(new BulkWorkbookRequest.DiplomaRow(2, null, "26BTECH901",
                        "Mechanical", "Govt Polytechnic", "DOTE", "78", "82", null)),
                List.of(new BulkWorkbookRequest.PgRow(2, null, "26BTECH901",
                        "Pondicherry University", "Puducherry", "PU", "Puducherry",
                        "MCA", "June 2024", "88", "90", "PU901")));

        BulkValidationResponse validation = bulkService.validateAndPreview(request);
        assertTrue(validation.valid());
        assertEquals(0, validation.errorCount());
        assertEquals(0, validation.warningCount());
        assertEquals(1, validation.matchedStudents());
        assertEquals(9, validation.totalRows());
        assertEquals(1, validation.preview().size());
        assertFalse(validation.preview().get(0).changes().isEmpty());

        BulkCommitResponse commit = bulkService.commit(request);
        assertEquals("SUCCESS", commit.status());
        assertEquals(9, commit.totalRows());
        assertEquals(9, commit.validRows());
        assertEquals(1, commit.updatedStudents());
        assertEquals(0, commit.noChangeRows());
        assertEquals(0, commit.failedRows());

        Student updated = studentRepository.findByApplicationNoIgnoreCase("RGCET/2026/9101").orElseThrow();
        assertEquals("Updated Student Name", updated.getStudentName());
        assertEquals("26BTECH901", updated.getRegisterNo());
        assertEquals(Gender.FEMALE, updated.getGender());
        assertEquals(Caste.OC, updated.getCaste());
        assertEquals("987654321002", updated.getAadhaarNo());
        assertEquals("Karaikal", updated.getDistrict());
        assertEquals("Indian", updated.getNationality());
        assertEquals(LocalDate.of(2007, 5, 10), updated.getDateOfBirth());
        assertEquals(Period.between(LocalDate.of(2007, 5, 10), LocalDate.now()).getYears(), updated.getAge());
        assertNotNull(updated.getUpdatedAt());

        assertEquals("New Father Name", updated.getParent().getFatherName());
        assertEquals("9012345678", updated.getParent().getFatherMobileNo());
        assertEquals("Farmer", updated.getParent().getFatherOccupation());
        assertEquals(0, new BigDecimal("450000").compareTo(updated.getParent().getAnnualIncome()));

        Address comm = address(updated, AddressType.COMMUNICATION);
        assertEquals("New Comm Address", comm.getAddressLine());
        assertEquals("605010", comm.getPincode());
        assertEquals("9443300101", comm.getPhone());
        assertEquals("9550100202", comm.getMobile());
        assertEquals("new.comm@example.com", comm.getEmail());

        Address perm = address(updated, AddressType.PERMANENT);
        assertEquals("New Perm Address", perm.getAddressLine());
        assertEquals("605020", perm.getPincode());
        assertNull(perm.getPhone());

        assertEquals("Management", updated.getAdmission().getCategory().getCategoryName());
        assertEquals("Information Technology (IT)", updated.getAdmission().getDepartment().getDepartmentName());
        assertEquals("First Year B.Tech", updated.getAdmission().getProgram().getProgramName());
        assertEquals("2026-2031", updated.getAdmission().getBatch());
        assertEquals(LocalDate.of(2026, 7, 1), updated.getAdmission().getDateOfAdmission());

        assertEquals("New School", updated.getQualifyingExam().getInstitutionName());
        assertEquals("SSLC901", updated.getQualifyingExam().getSslcRegistrationNo());
        assertEquals("HSC901", updated.getQualifyingExam().getHscRegistrationNo());
        assertEquals(0, new BigDecimal("85.5").compareTo(updated.getQualifyingExam().getSslcPercentage()));
        assertEquals(4, updated.getQualifyingExam().getAcademicMarks().size());
        assertEquals(3, updated.getQualifyingExam().getVocationalMarks().size());
        assertEquals(0, new BigDecimal("83.29").compareTo(updated.getQualifyingExam().getHscPercentage()));

        assertEquals("Mechanical", updated.getDiplomaDetails().getDiploma());
        assertEquals(0, new BigDecimal("80.00").compareTo(updated.getDiplomaDetails().getAggregatePercentage()));

        assertEquals("Pondicherry University", updated.getPgQualification().getUniversityName());
        assertEquals(0, new BigDecimal("88").compareTo(updated.getPgQualification().getTotalPercentage()));
        assertEquals(0, new BigDecimal("90").compareTo(updated.getPgQualification().getMainSubjectPercentage()));

        assertEquals(0, new BigDecimal("320000").compareTo(updated.getFee().getTotalFee()));

        List<BulkUploadLog> logs = bulkUploadLogRepository.findByFileName("bulk-valid.xlsx");
        assertEquals(1, logs.size());
        assertEquals(BulkUploadStatus.SUCCESS, logs.get(0).getStatus());
        assertEquals(Integer.valueOf(9), logs.get(0).getTotalRows());
        assertEquals(Integer.valueOf(1), logs.get(0).getUpdatedStudents());
        assertEquals(Integer.valueOf(0), logs.get(0).getFailedRows());
    }

    @Test
    void hardErrorsBlockCommitAndNothingIsApplied() {
        Student student = newStudent("RGCET/2026/9002", "26BTECH902", "Error Student", StudentStatus.ACTIVE);

        BulkWorkbookRequest request = request("bulk-errors.xlsx",
                List.of(new BulkWorkbookRequest.PersonalRow(2, null, "26BTECH902",
                        null, null, "X", "12345", null, null, null)),
                List.of(),
                List.of(),
                List.of(),
                List.of(new BulkWorkbookRequest.AcademicRow(2, null, "26BTECH902",
                        "Nonsense", null, null, null, null)),
                List.of(),
                List.of(),
                List.of(),
                List.of());

        BulkValidationResponse validation = bulkService.validateAndPreview(request);
        assertFalse(validation.valid());
        assertTrue(validation.errorCount() >= 3);
        assertTrue(validation.issues().stream().anyMatch(BulkIssue::isError));

        BulkCommitResponse commit = bulkService.commit(request);
        assertEquals("FAILED", commit.status());
        assertEquals(0, commit.updatedStudents());
        assertEquals(2, commit.failedRows());

        Student untouched = studentRepository.findByApplicationNoIgnoreCase("RGCET/2026/9002").orElseThrow();
        assertEquals("Error Student", untouched.getStudentName());
        assertEquals(Gender.MALE, untouched.getGender());
        assertEquals("987654321001", untouched.getAadhaarNo());
        assertEquals("CENTAC", untouched.getAdmission().getCategory().getCategoryName());

        List<BulkUploadLog> logs = bulkUploadLogRepository.findByFileName("bulk-errors.xlsx");
        assertEquals(1, logs.size());
        assertEquals(BulkUploadStatus.FAILED, logs.get(0).getStatus());
        assertEquals(Integer.valueOf(2), logs.get(0).getFailedRows());
    }

    @Test
    void archivedAndUnknownStudentsAreSkippedAsErrors() {
        Student archived = newStudent("RGCET/2026/9003", "26BTECH903", "Archived Student", StudentStatus.ARCHIVED);

        BulkWorkbookRequest request = request("bulk-archived.xlsx",
                List.of(new BulkWorkbookRequest.PersonalRow(2, null, "26BTECH903",
                        "Touched", null, null, null, null, null, null)),
                List.of(new BulkWorkbookRequest.ParentRow(2, null, "9999UNKNOWN",
                        "No One", null, null, null)),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());

        BulkValidationResponse validation = bulkService.validateAndPreview(request);
        assertFalse(validation.valid());
        assertEquals(2, validation.unmatchedRows());
        assertTrue(validation.issues().stream().anyMatch(i ->
                i.message().toLowerCase().contains("archived")));
        assertTrue(validation.issues().stream().anyMatch(i ->
                i.message().toLowerCase().contains("student not found")));

        BulkCommitResponse commit = bulkService.commit(request);
        assertEquals("FAILED", commit.status());
        assertEquals(0, commit.updatedStudents());
        assertEquals(2, commit.skippedRows());
        assertEquals(2, commit.failedRows());

        Student untouched = studentRepository.findByApplicationNoIgnoreCase("RGCET/2026/9003").orElseThrow();
        assertEquals("Archived Student", untouched.getStudentName());
        assertEquals(StudentStatus.ARCHIVED, untouched.getStatus());
    }

    @Test
    void blankCellsMeanNoChange() {
        Student student = newStudent("RGCET/2026/9004", "26BTECH904", "No Change Student", StudentStatus.ACTIVE);

        BulkWorkbookRequest request = request("bulk-no-change.xlsx",
                List.of(new BulkWorkbookRequest.PersonalRow(2, null, "26BTECH904",
                        null, null, null, null, null, null, null)),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());

        BulkCommitResponse commit = bulkService.commit(request);
        assertEquals("SUCCESS", commit.status());
        assertEquals(0, commit.updatedStudents());
        assertEquals(1, commit.noChangeRows());

        List<BulkUploadLog> logs = bulkUploadLogRepository.findByFileName("bulk-no-change.xlsx");
        assertEquals(1, logs.size());
        assertEquals(Integer.valueOf(0), logs.get(0).getUpdatedStudents());
    }

    @Test
    void warningsNeverBlockCommit() {
        Student student = newStudent("RGCET/2026/9005", "26BTECH905", "Warn Student", StudentStatus.ACTIVE);

        BulkWorkbookRequest request = request("bulk-warnings.xlsx",
                List.of(new BulkWorkbookRequest.PersonalRow(2, null, "26BTECH905",
                        null, null, null, null, "Atlantis", null, null)),
                List.of(),
                List.of(),
                List.of(),
                List.of(new BulkWorkbookRequest.AcademicRow(2, null, "26BTECH905",
                        null, null, null, "SomeBatch", null)),
                List.of(new BulkWorkbookRequest.QualifyingExamRow(2, null, "26BTECH905",
                        null, null, null, "stuff", null, null, null, null)),
                List.of(),
                List.of(),
                List.of());

        BulkValidationResponse validation = bulkService.validateAndPreview(request);
        assertTrue(validation.valid());
        assertEquals(0, validation.errorCount());
        assertTrue(validation.warningCount() >= 3);

        BulkCommitResponse commit = bulkService.commit(request);
        assertEquals("SUCCESS", commit.status());
        assertEquals(1, commit.updatedStudents());
        assertEquals(0, commit.failedRows());
        assertTrue(commit.warningCount() >= 3);

        List<BulkUploadLog> logs = bulkUploadLogRepository.findByFileName("bulk-warnings.xlsx");
        assertEquals(1, logs.size());
        assertEquals(BulkUploadStatus.SUCCESS, logs.get(0).getStatus());
    }

    // ---------- Fixtures ----------

    private BulkWorkbookRequest request(String fileName,
                                        List<BulkWorkbookRequest.PersonalRow> personal,
                                        List<BulkWorkbookRequest.ParentRow> parent,
                                        List<BulkWorkbookRequest.CommunicationRow> communication,
                                        List<BulkWorkbookRequest.PermanentRow> permanent,
                                        List<BulkWorkbookRequest.AcademicRow> academic,
                                        List<BulkWorkbookRequest.QualifyingExamRow> qualifyingExam,
                                        List<BulkWorkbookRequest.HscMarksRow> hscMarks,
                                        List<BulkWorkbookRequest.DiplomaRow> diploma,
                                        List<BulkWorkbookRequest.PgRow> pg) {
        return new BulkWorkbookRequest(fileName, "TestAdmin", personal, parent,
                communication, permanent, academic, qualifyingExam, hscMarks, diploma, pg);
    }

    private BulkWorkbookRequest.HscMarksRow hscRow(String registerNo) {
        return new BulkWorkbookRequest.HscMarksRow(2, null, registerNo, "Science",
                List.of(
                        subject("Maths", 100, 95),
                        subject("Physics", 100, 90),
                        subject("Chemistry", 100, 85),
                        subject("Computer Science", 100, 88)),
                List.of(
                        subject("Vocational Subject", 100, 80),
                        subject("Related Sub I", 100, 75),
                        subject("Related Sub II", 100, 70)));
    }

    private BulkWorkbookRequest.SubjectRow subject(String name, int max, int obtained) {
        return new BulkWorkbookRequest.SubjectRow(name, "May 2025",
                String.valueOf(max), String.valueOf(obtained));
    }

    private Student newStudent(String appNo, String regNo, String name, StudentStatus status) {
        Student s = new Student();
        s.setApplicationNo(appNo);
        s.setRegisterNo(regNo);
        s.setStudentName(name);
        s.setDateOfBirth(LocalDate.of(2008, 1, 15));
        s.setGender(Gender.MALE);
        s.setAadhaarNo("987654321001");
        s.setDistrict("Puducherry");
        s.setNationality("Indian");
        s.setCaste(Caste.BC);
        s.setStatus(status);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());

        ParentDetails parent = new ParentDetails();
        parent.setStudent(s);
        parent.setFatherName("Old Father");
        parent.setFatherMobileNo("9000000001");
        parent.setFatherOccupation("Clerk");
        parent.setAnnualIncome(new BigDecimal("300000"));
        s.setParent(parent);

        Address perm = new Address();
        perm.setStudent(s);
        perm.setAddressType(AddressType.PERMANENT);
        perm.setAddressLine("123 Old Street");
        perm.setPincode("605001");
        perm.setMobile("9500000001");
        perm.setEmail("old@example.com");
        s.getAddresses().add(perm);

        Address comm = new Address();
        comm.setStudent(s);
        comm.setAddressType(AddressType.COMMUNICATION);
        comm.setAddressLine("123 Old Street");
        comm.setPincode("605001");
        comm.setMobile("9500000001");
        comm.setEmail("old@example.com");
        s.getAddresses().add(comm);

        Admission admission = new Admission();
        admission.setStudent(s);
        admission.setProgram(program("First Year B.Tech"));
        admission.setDepartment(department("Computer Science & Engineering (CSE)"));
        admission.setCategory(category("CENTAC"));
        admission.setBatch("2026-2030");
        admission.setDateOfAdmission(LocalDate.of(2026, 6, 1));
        s.setAdmission(admission);

        StudentFee fee = new StudentFee();
        fee.setStudent(s);
        fee.setTotalFee(new BigDecimal("75000"));
        s.setFee(fee);

        return studentRepository.save(s);
    }

    private Program program(String name) {
        return programRepository.findByProgramNameIgnoreCase(name).orElseThrow();
    }

    private Department department(String name) {
        return departmentRepository.findByDepartmentNameIgnoreCase(name).orElseThrow();
    }

    private AdmissionCategory category(String name) {
        return categoryRepository.findByCategoryNameIgnoreCase(name).orElseThrow();
    }

    private Address address(Student student, AddressType type) {
        return student.getAddresses().stream()
                .filter(a -> a.getAddressType() == type)
                .findFirst().orElseThrow();
    }
}
