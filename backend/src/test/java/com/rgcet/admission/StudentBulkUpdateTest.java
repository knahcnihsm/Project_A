package com.rgcet.admission;

import com.rgcet.admission.dto.BulkUpdateRequest;
import com.rgcet.admission.dto.BulkUpdateRequest.BulkUpdateResponse;
import com.rgcet.admission.dto.BulkUpdateRequest.UpdateRow;
import com.rgcet.admission.entity.*;
import com.rgcet.admission.repository.*;
import com.rgcet.admission.service.FeeService;
import com.rgcet.admission.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StudentBulkUpdateTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AdmissionCategoryRepository categoryRepository;

    @Autowired
    private StudentService studentService;

    private Student sampleStudent1;
    private Student sampleStudent2;
    private Program programBTech;
    private Department deptCSE;
    private AdmissionCategory centacCat;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();

        programBTech = programRepository.findAll().stream().findFirst().orElseGet(() -> {
            Program p = new Program();
            p.setProgramName("First Year B.Tech");
            return programRepository.save(p);
        });

        deptCSE = departmentRepository.findAll().stream().findFirst().orElseGet(() -> {
            Department d = new Department();
            d.setDepartmentName("Computer Science & Engineering (CSE)");
            return departmentRepository.save(d);
        });

        centacCat = categoryRepository.findAll().stream().findFirst().orElseGet(() -> {
            AdmissionCategory c = new AdmissionCategory();
            c.setCategoryName("CENTAC");
            return categoryRepository.save(c);
        });

        // Create sample student 1
        sampleStudent1 = new Student();
        sampleStudent1.setApplicationNo("RGCET/2026/1001");
        sampleStudent1.setRegisterNo("24TD0001");
        sampleStudent1.setStudentName("John Doe");
        sampleStudent1.setDateOfBirth(LocalDate.of(2005, 1, 15));
        sampleStudent1.setGender(Gender.MALE);
        sampleStudent1.setAadhaarNo("123456789012");
        sampleStudent1.setDistrict("Puducherry");
        sampleStudent1.setCaste(Caste.BC);
        sampleStudent1.setStatus(StudentStatus.ACTIVE);
        
        ParentDetails parent1 = new ParentDetails();
        parent1.setStudent(sampleStudent1);
        parent1.setFatherName("Richard Doe");
        parent1.setFatherMobileNo("9876543210");
        sampleStudent1.setParent(parent1);

        Address permAddress1 = new Address();
        permAddress1.setStudent(sampleStudent1);
        permAddress1.setAddressType(AddressType.PERMANENT);
        permAddress1.setMobile("9876543210");
        permAddress1.setEmail("john@example.com");
        sampleStudent1.getAddresses().add(permAddress1);

        Admission admission1 = new Admission();
        admission1.setStudent(sampleStudent1);
        admission1.setProgram(programBTech);
        admission1.setDepartment(deptCSE);
        admission1.setCategory(centacCat);
        admission1.setBatch("2026 - 2030");
        sampleStudent1.setAdmission(admission1);

        StudentFee fee1 = new StudentFee();
        fee1.setStudent(sampleStudent1);
        fee1.setTotalFee(new BigDecimal("350000"));
        sampleStudent1.setFee(fee1);

        sampleStudent1 = studentRepository.save(sampleStudent1);

        // Create sample student 2 (no register number initially)
        sampleStudent2 = new Student();
        sampleStudent2.setApplicationNo("RGCET/2026/1002");
        sampleStudent2.setRegisterNo(null);
        sampleStudent2.setStudentName("Jane Smith");
        sampleStudent2.setDateOfBirth(LocalDate.of(2005, 5, 20));
        sampleStudent2.setGender(Gender.FEMALE);
        sampleStudent2.setStatus(StudentStatus.ACTIVE);
        sampleStudent2 = studentRepository.save(sampleStudent2);
    }

    @Test
    void testBulkUpdateByRegisterNumber() {
        UpdateRow row = new UpdateRow(
                2,
                "RGCET/2026/1001",
                "24TD0001",
                "Johnathan Doe Updated",
                "",
                "Male",
                "",
                "Chennai",
                "",
                "",
                "",
                "",
                "",
                "Richard Doe Sr.",
                "9988776655",
                "9123456789",
                "johnathan@example.com",
                "400000",
                "",
                ""
        );

        BulkUpdateRequest req = new BulkUpdateRequest(List.of(row));
        BulkUpdateResponse resp = studentService.bulkUpdate(req);

        assertEquals(1, resp.totalRows());
        assertEquals(1, resp.updatedCount());
        assertEquals(0, resp.skippedCount());
        assertEquals(0, resp.failedCount());
        assertTrue(resp.errors().isEmpty());

        Student updated = studentRepository.findById(sampleStudent1.getStudentId()).orElseThrow();
        assertEquals("Johnathan Doe Updated", updated.getStudentName());
        assertEquals("Chennai", updated.getDistrict());
        assertEquals("Richard Doe Sr.", updated.getParent().getFatherName());
        assertEquals("9988776655", updated.getParent().getFatherMobileNo());
        // Blank cells preserved: dateOfBirth, gender, caste remain unchanged
        assertEquals(LocalDate.of(2005, 1, 15), updated.getDateOfBirth());
        assertEquals(Caste.BC, updated.getCaste());
        // Check updated audit timestamp
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void testBulkUpdateByApplicationNumberWhenRegisterNumberIsEmpty() {
        UpdateRow row = new UpdateRow(
                3,
                "RGCET/2026/1002",
                "", // empty Register Number
                "Jane Smith Updated",
                "2005-05-20",
                "Female",
                "987654321099",
                "Puducherry",
                "OC",
                "",
                "",
                "",
                "",
                "",
                "",
                "9876543219",
                "jane@example.com",
                "",
                "",
                ""
        );

        BulkUpdateRequest req = new BulkUpdateRequest(List.of(row));
        BulkUpdateResponse resp = studentService.bulkUpdate(req);

        assertEquals(1, resp.totalRows());
        assertEquals(1, resp.updatedCount());
        assertEquals(0, resp.skippedCount());
        assertEquals(0, resp.failedCount());

        Student updated = studentRepository.findById(sampleStudent2.getStudentId()).orElseThrow();
        assertEquals("Jane Smith Updated", updated.getStudentName());
        assertEquals("987654321099", updated.getAadhaarNo());
        assertEquals(Caste.OC, updated.getCaste());
    }

    @Test
    void testMissingIdentifierRowIsSkipped() {
        UpdateRow row = new UpdateRow(
                4,
                "", // missing
                "", // missing
                "Some Name",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
        );

        BulkUpdateRequest req = new BulkUpdateRequest(List.of(row));
        BulkUpdateResponse resp = studentService.bulkUpdate(req);

        assertEquals(1, resp.totalRows());
        assertEquals(0, resp.updatedCount());
        assertEquals(1, resp.skippedCount());
        assertEquals(0, resp.failedCount());
        assertEquals(1, resp.errors().size());
        assertEquals("Missing Identifier", resp.errors().get(0).reason());
    }

    @Test
    void testStudentNotFoundRowIsSkipped() {
        UpdateRow row = new UpdateRow(
                5,
                "RGCET/9999/9999",
                "99TD9999",
                "Nonexistent Student",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
        );

        BulkUpdateRequest req = new BulkUpdateRequest(List.of(row));
        BulkUpdateResponse resp = studentService.bulkUpdate(req);

        assertEquals(1, resp.totalRows());
        assertEquals(0, resp.updatedCount());
        assertEquals(1, resp.skippedCount());
        assertEquals(0, resp.failedCount());
        assertEquals(1, resp.errors().size());
        assertEquals("Student Not Found", resp.errors().get(0).reason());
    }

    @Test
    void testValidationFailureInvalidEmail() {
        UpdateRow row = new UpdateRow(
                6,
                "RGCET/2026/1001",
                "24TD0001",
                "John Doe",
                "", "", "", "", "", "", "", "", "", "", "", "",
                "invalid-email-address", // Invalid email
                "", "", ""
        );

        BulkUpdateRequest req = new BulkUpdateRequest(List.of(row));
        BulkUpdateResponse resp = studentService.bulkUpdate(req);

        assertEquals(1, resp.totalRows());
        assertEquals(0, resp.updatedCount());
        assertEquals(0, resp.skippedCount());
        assertEquals(1, resp.failedCount());
        assertEquals(1, resp.errors().size());
        assertEquals("Invalid Email", resp.errors().get(0).reason());
    }

    @Test
    void testValidationFailureInvalidMobile() {
        UpdateRow row = new UpdateRow(
                7,
                "RGCET/2026/1001",
                "24TD0001",
                "John Doe",
                "", "", "", "", "", "", "", "", "", "", "",
                "12345", // Invalid mobile (less than 10 digits)
                "", "", "", ""
        );

        BulkUpdateRequest req = new BulkUpdateRequest(List.of(row));
        BulkUpdateResponse resp = studentService.bulkUpdate(req);

        assertEquals(1, resp.totalRows());
        assertEquals(0, resp.updatedCount());
        assertEquals(0, resp.skippedCount());
        assertEquals(1, resp.failedCount());
        assertEquals(1, resp.errors().size());
        assertEquals("Invalid Mobile Number", resp.errors().get(0).reason());
    }

    @Test
    void testValidationFailureInvalidDepartment() {
        UpdateRow row = new UpdateRow(
                8,
                "RGCET/2026/1001",
                "24TD0001",
                "John Doe",
                "", "", "", "", "", "", "",
                "Nonexistent Department Engineering", // Invalid Department
                "", "", "", "", "", "", "", ""
        );

        BulkUpdateRequest req = new BulkUpdateRequest(List.of(row));
        BulkUpdateResponse resp = studentService.bulkUpdate(req);

        assertEquals(1, resp.totalRows());
        assertEquals(0, resp.updatedCount());
        assertEquals(0, resp.skippedCount());
        assertEquals(1, resp.failedCount());
        assertEquals(1, resp.errors().size());
        assertEquals("Invalid Department", resp.errors().get(0).reason());
    }

    @Test
    void testIndependentTransactionsMultipleRows() {
        UpdateRow row1Valid = new UpdateRow(
                2,
                "RGCET/2026/1001",
                "24TD0001",
                "John Doe Updated Name",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
        );

        UpdateRow row2Invalid = new UpdateRow(
                3,
                "RGCET/2026/1002",
                "",
                "Jane Smith",
                "", "", "", "", "", "", "", "", "", "", "",
                "invalid-email", // invalid email causing row 2 failure
                "", "", "", ""
        );

        UpdateRow row3NotFound = new UpdateRow(
                4,
                "NON_EXISTENT",
                "NON_EXISTENT",
                "Ghost Student",
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
        );

        BulkUpdateRequest req = new BulkUpdateRequest(List.of(row1Valid, row2Invalid, row3NotFound));
        BulkUpdateResponse resp = studentService.bulkUpdate(req);

        assertEquals(3, resp.totalRows());
        assertEquals(1, resp.updatedCount());
        assertEquals(1, resp.skippedCount());
        assertEquals(1, resp.failedCount());
        assertEquals(2, resp.errors().size());

        // Row 1 should STILL be updated despite Row 2 and Row 3 failing!
        Student updated1 = studentRepository.findById(sampleStudent1.getStudentId()).orElseThrow();
        assertEquals("John Doe Updated Name", updated1.getStudentName());
    }
}
