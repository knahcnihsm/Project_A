package com.rgcet.admission.service;

import com.rgcet.admission.common.ResourceNotFoundException;
import com.rgcet.admission.dto.AcademicStepRequest;
import com.rgcet.admission.dto.ArchiveRequest;
import com.rgcet.admission.dto.BulkUpdateRequest;
import com.rgcet.admission.dto.BulkUpdateRequest.BulkUpdateResponse;
import com.rgcet.admission.dto.BulkUpdateRequest.UpdateRow;
import com.rgcet.admission.dto.CertificatesStepRequest;
import com.rgcet.admission.dto.CommunicationStepRequest;
import com.rgcet.admission.dto.DiplomaStepRequest;
import com.rgcet.admission.dto.FeeStepRequest;
import com.rgcet.admission.dto.HscMarksStepRequest;
import com.rgcet.admission.dto.ParentStepRequest;
import com.rgcet.admission.dto.PersonalStepRequest;
import com.rgcet.admission.dto.PgStepRequest;
import com.rgcet.admission.dto.QualifyingExamStepRequest;
import com.rgcet.admission.dto.StudentResponseDto;
import com.rgcet.admission.dto.StudentSummaryDto;
import com.rgcet.admission.dto.SubmitAdmissionRequest;
import com.rgcet.admission.entity.Address;
import com.rgcet.admission.entity.AddressType;
import com.rgcet.admission.entity.Admission;
import com.rgcet.admission.entity.AdmissionCategory;
import com.rgcet.admission.entity.Archive;
import com.rgcet.admission.entity.Caste;
import com.rgcet.admission.entity.Certificate;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.DiplomaDetails;
import com.rgcet.admission.entity.Gender;
import com.rgcet.admission.entity.HSCAcademicMark;
import com.rgcet.admission.entity.HSCVocationalMark;
import com.rgcet.admission.entity.PGQualification;
import com.rgcet.admission.entity.ParentDetails;
import com.rgcet.admission.entity.PaymentStatus;
import com.rgcet.admission.entity.Program;
import com.rgcet.admission.entity.QualifyingExam;
import com.rgcet.admission.entity.Student;
import com.rgcet.admission.entity.StudentCertificate;
import com.rgcet.admission.entity.StudentFee;
import com.rgcet.admission.entity.StudentStatus;
import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.ArchiveRepository;
import com.rgcet.admission.repository.CertificateRepository;
import com.rgcet.admission.repository.DepartmentRepository;
import com.rgcet.admission.repository.HostelRepository;
import com.rgcet.admission.repository.ProgramRepository;
import com.rgcet.admission.repository.StudentRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final AdmissionCategoryRepository categoryRepository;
    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final CertificateRepository certificateRepository;
    private final HostelRepository hostelRepository;
    private final ArchiveRepository archiveRepository;
    private final FeeService feeService;
    private final PlatformTransactionManager transactionManager;

    private static final Pattern REGISTER_PATTERN = Pattern.compile("^[A-Za-z0-9_./\\\\\\-\\s]{1,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.BASIC_ISO_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };

    // ---------- Create & steps ----------

    @Transactional
    public StudentResponseDto createStudent(PersonalStepRequest request) {
        if (studentRepository.existsByApplicationNoIgnoreCase(request.applicationNumber())) {
            throw new IllegalArgumentException("Application number already exists: " + request.applicationNumber());
        }
        Student student = new Student();
        applyPersonal(student, request);
        student.setStatus(StudentStatus.DRAFT);
        LocalDateTime now = LocalDateTime.now();
        student.setCreatedAt(now);
        student.setUpdatedAt(now);
        studentRepository.save(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updatePersonal(Long id, PersonalStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        if (!student.getApplicationNo().equalsIgnoreCase(request.applicationNumber())
                && studentRepository.existsByApplicationNoIgnoreCase(request.applicationNumber())) {
            throw new IllegalArgumentException("Application number already exists: " + request.applicationNumber());
        }
        applyPersonal(student, request);
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updateParent(Long id, ParentStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        ParentDetails parent = student.getParent();
        if (parent == null) {
            parent = new ParentDetails();
            parent.setStudent(student);
            student.setParent(parent);
        }
        parent.setFatherName(request.fatherName());
        parent.setFatherMobileNo(request.fatherMobile());
        parent.setFatherOccupation(request.fatherOccupation());
        parent.setAnnualIncome(request.annualIncome());
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updateCommunication(Long id, CommunicationStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        Address permanent = getAddress(student, AddressType.PERMANENT);
        CommunicationStepRequest.AddressRequest permReq = request.permanentAddress();
        permanent.setAddressLine(permReq.addressLine());
        permanent.setPincode(permReq.pincode());
        permanent.setPhone(permReq.phone());
        permanent.setMobile(permReq.mobile());
        permanent.setEmail(permReq.email());
        permanent.setSameAsPermanent(request.sameAsPermanent());

        Address communication = getAddress(student, AddressType.COMMUNICATION);
        if (request.sameAsPermanent()) {
            communication.setAddressLine(permanent.getAddressLine());
            communication.setPincode(permanent.getPincode());
            communication.setPhone(permanent.getPhone());
            communication.setMobile(permanent.getMobile());
            communication.setEmail(permanent.getEmail());
        } else {
            CommunicationStepRequest.AddressRequest commReq = request.communicationAddress();
            communication.setAddressLine(commReq.addressLine());
            communication.setPincode(commReq.pincode());
            communication.setPhone(commReq.phone());
            communication.setMobile(commReq.mobile());
            communication.setEmail(commReq.email());
        }
        communication.setSameAsPermanent(request.sameAsPermanent());
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updateAcademic(Long id, AcademicStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        Admission admission = student.getAdmission();
        if (admission == null) {
            admission = new Admission();
            admission.setStudent(student);
            student.setAdmission(admission);
        }
        admission.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Admission category not found: " + request.categoryId())));
        admission.setProgram(programRepository.findById(request.programId())
                .orElseThrow(() -> new ResourceNotFoundException("Program not found: " + request.programId())));
        if (request.departmentId() != null) {
            admission.setDepartment(departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.departmentId())));
        } else {
            admission.setDepartment(null);
        }
        admission.setBatch(request.batch());
        admission.setDateOfAdmission(request.dateOfAdmission());
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updateQualifyingExam(Long id, QualifyingExamStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        QualifyingExam exam = getOrCreateQualifyingExam(student);
        exam.setInstitutionName(request.institutionName());
        exam.setInstitutionPlace(request.institutionPlace());
        exam.setExamPassed(request.examPassed());
        exam.setMonthYearOfPassing(request.monthYearPassing());
        exam.setSslcPercentage(request.sslcPercentage());
        exam.setSslcRegistrationNo(request.sslcRegisterNumber());
        if (request.hscPercentage() != null) {
            exam.setHscPercentage(request.hscPercentage());
        }
        exam.setHscRegistrationNo(request.hscRegisterNumber());
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updateHscMarks(Long id, HscMarksStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        QualifyingExam exam = getOrCreateQualifyingExam(student);

        List<HSCAcademicMark> academic = new ArrayList<>();
        if (request.academicMarks() != null) {
            for (HscMarksStepRequest.SubjectMark m : request.academicMarks()) {
                HSCAcademicMark mark = new HSCAcademicMark();
                mark.setQualifyingExam(exam);
                mark.setSubjectName(m.subject());
                mark.setMonthYear(m.monthYear());
                mark.setMaximumMarks(m.maxMarks());
                mark.setMarksObtained(m.marksObtained());
                mark.setPercentage(CutoffCalculator.subjectPercentage(m.marksObtained(), m.maxMarks()));
                academic.add(mark);
            }
        }
        List<HSCVocationalMark> vocational = new ArrayList<>();
        if (request.vocationalMarks() != null) {
            for (HscMarksStepRequest.SubjectMark m : request.vocationalMarks()) {
                HSCVocationalMark mark = new HSCVocationalMark();
                mark.setQualifyingExam(exam);
                mark.setSubjectName(m.subject());
                mark.setMonthYear(m.monthYear());
                mark.setMaximumMarks(m.maxMarks());
                mark.setMarksObtained(m.marksObtained());
                mark.setPercentage(CutoffCalculator.subjectPercentage(m.marksObtained(), m.maxMarks()));
                vocational.add(mark);
            }
        }

        exam.getAcademicMarks().clear();
        exam.getAcademicMarks().addAll(academic);
        exam.getVocationalMarks().clear();
        exam.getVocationalMarks().addAll(vocational);
        BigDecimal calcOverall = CutoffCalculator.overallPercentage(academic, vocational);
        if (exam.getHscPercentage() == null
                && calcOverall != null
                && calcOverall.compareTo(BigDecimal.ZERO) > 0) {
            exam.setHscPercentage(calcOverall);
        }
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updateDiploma(Long id, DiplomaStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        DiplomaDetails diploma = student.getDiplomaDetails();
        if (diploma == null) {
            diploma = new DiplomaDetails();
            diploma.setStudent(student);
            student.setDiplomaDetails(diploma);
        }
        diploma.setDiploma(request.diplomaCourse());
        diploma.setInstitutionName(request.institutionName());
        diploma.setBoard(request.board());
        diploma.setSecondYearPercentage(request.secondYearPercentage());
        diploma.setThirdYearPercentage(request.thirdYearPercentage());
        diploma.setAggregatePercentage(resolveAggregate(request));
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updatePg(Long id, PgStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        PGQualification pg = student.getPgQualification();
        if (pg == null) {
            pg = new PGQualification();
            pg.setStudent(student);
            student.setPgQualification(pg);
        }
        pg.setUniversityName(request.universityName());
        pg.setUniversityPlace(request.universityPlace());
        pg.setInstitutionName(request.institutionName());
        pg.setInstitutionPlace(request.institutionPlace());
        pg.setExamPassed(request.examPassed());
        pg.setMonthYearOfPassing(request.monthYearPassing());
        pg.setTotalPercentage(request.totalPercentage());
        pg.setMainSubjectPercentage(request.mainSubjectPercentage());
        pg.setDegreeRegistrationNo(request.degreeRegistrationNumber());
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updateFee(Long id, FeeStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        FeeResult result = feeService.compute(student, request);

        StudentFee fee = student.getFee();
        if (fee == null) {
            fee = new StudentFee();
            fee.setStudent(student);
            student.setFee(fee);
        }
        fee.setCutOffMark(result.cutOffMark());
        fee.setFeeStructure(result.structure());
        fee.setTuitionFeePerYear(result.tuitionFeePerYear());
        fee.setCourseDurationYears(result.courseDurationYears());
        fee.setTotalTuitionFee(result.totalTuitionFee());
        fee.setBusRequired(request.busRequired());
        fee.setRoute(result.route());
        fee.setBusStop(result.busStop());
        fee.setBusFee(result.busFee());
        fee.setHostelRequired(request.hostelRequired());
        fee.setHostel(request.hostelRequired()
                ? hostelRepository.findAll().stream().findFirst().orElse(null)
                : null);
        fee.setHostelFee(result.hostelFee());
        fee.setTotalFee(result.totalFee());
        if (fee.getPaymentStatus() == null) {
            fee.setPaymentStatus(PaymentStatus.PENDING);
        }
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto updateCertificates(Long id, CertificatesStepRequest request) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        if (request.certificates() == null) {
            return StudentResponseDto.from(student);
        }
        Set<Long> requestedIds = request.certificates().stream()
                .map(CertificatesStepRequest.CertificateItem::certificateId)
                .collect(Collectors.toSet());

        List<StudentCertificate> toRemove = student.getCertificates().stream()
                .filter(sc -> sc.getCertificate() != null
                        && !requestedIds.contains(sc.getCertificate().getCertificateId()))
                .toList();
        toRemove.forEach(student.getCertificates()::remove);

        Map<Long, StudentCertificate> existing = student.getCertificates().stream()
                .filter(sc -> sc.getCertificate() != null)
                .collect(Collectors.toMap(sc -> sc.getCertificate().getCertificateId(), Function.identity()));

        LocalDateTime now = LocalDateTime.now();
        for (CertificatesStepRequest.CertificateItem item : request.certificates()) {
            StudentCertificate sc = existing.get(item.certificateId());
            if (sc == null) {
                Certificate certificate = certificateRepository.findById(item.certificateId())
                        .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + item.certificateId()));
                sc = new StudentCertificate();
                sc.setStudent(student);
                sc.setCertificate(certificate);
                student.getCertificates().add(sc);
            }
            sc.setIsSubmitted(item.submitted());
            if (item.filePath() != null && !item.filePath().isBlank()) {
                sc.setFilePath(item.filePath());
                sc.setUploadedAt(now);
            }
        }
        touch(student);
        return StudentResponseDto.from(student);
    }

    // ---------- Submit all sections in one transaction ----------

    @Transactional
    public StudentResponseDto submitAdmission(SubmitAdmissionRequest request) {
        Long id = request.studentId();
        if (id == null) {
            id = createStudent(request.personal()).id();
        } else {
            updatePersonal(id, request.personal());
        }

        if (request.parent() != null) {
            updateParent(id, request.parent());
        }
        if (request.communication() != null) {
            updateCommunication(id, request.communication());
        }
        if (request.academic() != null) {
            updateAcademic(id, request.academic());
        }
        if (request.qualifyingExam() != null) {
            updateQualifyingExam(id, request.qualifyingExam());
        }
        if (request.hscMarks() != null) {
            updateHscMarks(id, request.hscMarks());
        }
        if (request.diploma() != null) {
            updateDiploma(id, request.diploma());
        }
        if (request.pg() != null) {
            updatePg(id, request.pg());
        }
        if (request.fee() != null) {
            updateFee(id, request.fee());
        }
        if (request.certificates() != null) {
            updateCertificates(id, request.certificates());
        }

        Student student = getStudentOrThrow(id);
        student.setStatus(StudentStatus.ACTIVE);
        touch(student);
        return StudentResponseDto.from(student);
    }

    // ---------- Finalize / read / archive ----------

    @Transactional
    public StudentResponseDto finalize(Long id) {
        Student student = getStudentOrThrow(id);
        assertNotArchived(student);
        student.setStatus(StudentStatus.ACTIVE);
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional(readOnly = true)
    public StudentResponseDto getStudent(Long id) {
        return StudentResponseDto.from(getStudentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<StudentSummaryDto> search(String search, Long departmentId, Long programId,
                                          Long categoryId, String batch, StudentStatus status,
                                          Pageable pageable) {
        Specification<Student> spec = buildSpecification(search, departmentId, programId, categoryId, batch, status);
        return studentRepository.findAll(spec, pageable).map(StudentSummaryDto::from);
    }

    @Transactional(readOnly = true)
    public Page<StudentResponseDto> searchDetails(String search, Long departmentId, Long programId,
                                                  Long categoryId, String batch, StudentStatus status,
                                                  Pageable pageable) {
        Specification<Student> spec = buildSpecification(search, departmentId, programId, categoryId, batch, status);
        return studentRepository.findAll(spec, pageable).map(StudentResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<StudentSummaryDto> listArchived() {
        return studentRepository.findAll(byStatus(StudentStatus.ARCHIVED), Sort.by(Sort.Direction.DESC, "archivedAt"))
                .stream()
                .map(StudentSummaryDto::from)
                .toList();
    }

    @Transactional
    public StudentResponseDto archive(Long id, ArchiveRequest request) {
        Student student = getStudentOrThrow(id);
        if (student.getStatus() == StudentStatus.ARCHIVED) {
            throw new IllegalStateException("Student is already archived.");
        }
        LocalDateTime now = LocalDateTime.now();
        student.setStatus(StudentStatus.ARCHIVED);
        student.setArchivedAt(now);
        student.setArchiveReason(request.reason());

        Archive archive = new Archive();
        archive.setStudent(student);
        archive.setArchiveReason(request.reason());
        archive.setDescription(request.description());
        archive.setArchivedDate(now);
        archiveRepository.save(archive);
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional
    public StudentResponseDto restore(Long id) {
        Student student = getStudentOrThrow(id);
        if (student.getStatus() != StudentStatus.ARCHIVED) {
            throw new IllegalStateException("Student is not archived.");
        }
        archiveRepository.deleteByStudentStudentId(id);
        student.setStatus(StudentStatus.ACTIVE);
        student.setArchivedAt(null);
        student.setArchiveReason(null);
        touch(student);
        return StudentResponseDto.from(student);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return studentRepository.countByStatus(StudentStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long countArchived() {
        return studentRepository.countByStatus(StudentStatus.ARCHIVED);
    }

    @Transactional(readOnly = true)
    public long countDraft() {
        return studentRepository.countByStatus(StudentStatus.DRAFT);
    }

    // ---------- Bulk update ----------

    /**
     * Processes the uploaded Bulk Student Update Excel rows.
     * <p>
     * Every row is committed in its own database transaction so that one failed row
     * never rolls back the successful updates of the other rows. Only existing
     * students are updated (never inserted or deleted). Blank cells mean "No Change".
     * <p>
     * Summary semantics:
     * <ul>
     *     <li><b>Successfully Updated</b> - student found, validation passed, changes persisted.</li>
     *     <li><b>Skipped</b> - the row could not be matched to a student (missing identifier,
     *     student not found, archived student).</li>
     *     <li><b>Failed</b> - the row had an invalid field value or the database update failed.</li>
     * </ul>
     */
    public BulkUpdateResponse bulkUpdate(BulkUpdateRequest request) {
        List<UpdateRow> rows = request.rows() == null ? List.of() : request.rows();
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        List<BulkUpdateRequest.RowError> errors = new ArrayList<>();
        TransactionTemplate rowTransaction = new TransactionTemplate(transactionManager);

        for (int i = 0; i < rows.size(); i++) {
            UpdateRow row = rows.get(i);
            int rowNumber = row.rowNumber() != null ? row.rowNumber() : i + 2;
            String registerNo = trimToNull(row.registerNumber());
            String applicationNo = trimToNull(row.applicationNumber());

            if (registerNo == null && applicationNo == null) {
                skipped++;
                errors.add(new BulkUpdateRequest.RowError(rowNumber, "", "", "Missing Identifier"));
                continue;
            }

            try {
                rowTransaction.execute(status -> {
                    processBulkRow(row);
                    return null;
                });
                updated++;
            } catch (BulkRowException e) {
                if (e.isFailure()) {
                    failed++;
                } else {
                    skipped++;
                }
                errors.add(new BulkUpdateRequest.RowError(rowNumber,
                        orEmpty(registerNo), orEmpty(applicationNo), e.getReason()));
            } catch (RuntimeException e) {
                failed++;
                errors.add(new BulkUpdateRequest.RowError(rowNumber,
                        orEmpty(registerNo), orEmpty(applicationNo), "Database Update Failed"));
            }
        }
        return new BulkUpdateResponse(rows.size(), updated, skipped, failed, errors);
    }

    /**
     * Processes a single row inside its own transaction.
     */
    private void processBulkRow(UpdateRow row) {
        RowData data = validateBulkRow(row);
        Student student = findByIdentifier(row.applicationNumber(), row.registerNumber());
        if (student == null) {
            throw new BulkRowException("Student Not Found", false);
        }
        if (student.getStatus() == StudentStatus.ARCHIVED) {
            throw new BulkRowException("Student is Archived", false);
        }
        applyBulkRow(student, row, data);
        touch(student);
        studentRepository.save(student);
    }

    /**
     * Validates every column that contains a value and resolves master data
     * references (Program, Department, Admission Category). Throws on the first
     * invalid field so the row is reported as failed.
     */
    private RowData validateBulkRow(UpdateRow row) {
        LocalDate dob = null;
        if (isNotBlank(row.dateOfBirth())) {
            dob = parseDate(row.dateOfBirth());
            if (dob == null) {
                throw new BulkRowException("Invalid Date of Birth", true);
            }
        }

        Gender gender = null;
        if (isNotBlank(row.gender())) {
            gender = parseGender(row.gender());
            if (gender == null) {
                throw new BulkRowException("Invalid Gender", true);
            }
        }

        Caste caste = null;
        if (isNotBlank(row.caste())) {
            caste = parseCaste(row.caste());
            if (caste == null) {
                throw new BulkRowException("Invalid Category", true);
            }
        }

        String registerNo = trimToNull(row.registerNumber());
        if (registerNo != null && !REGISTER_PATTERN.matcher(registerNo).matches()) {
            throw new BulkRowException("Invalid Register Number", true);
        }

        String applicationNo = trimToNull(row.applicationNumber());
        if (applicationNo != null && !REGISTER_PATTERN.matcher(applicationNo).matches()) {
            throw new BulkRowException("Invalid Application Number", true);
        }

        String aadhaar = trimToNull(row.aadhaarNumber());
        if (aadhaar != null && !aadhaar.matches("\\d{12}")) {
            throw new BulkRowException("Invalid Aadhaar Number", true);
        }

        String fatherMobile = trimToNull(row.fatherMobile());
        if (fatherMobile != null && !fatherMobile.matches("\\d{10}")) {
            throw new BulkRowException("Invalid Mobile Number", true);
        }

        String mobileNumber = trimToNull(row.mobileNumber());
        if (mobileNumber != null && !mobileNumber.matches("\\d{10}")) {
            throw new BulkRowException("Invalid Mobile Number", true);
        }

        String email = trimToNull(row.email());
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BulkRowException("Invalid Email", true);
        }

        AdmissionCategory admissionCategory = null;
        if (isNotBlank(row.admissionCategory())) {
            admissionCategory = categoryRepository.findByCategoryNameIgnoreCase(trimToNull(row.admissionCategory()))
                    .orElseThrow(() -> new BulkRowException("Invalid Admission Category", true));
        }

        Program program = null;
        if (isNotBlank(row.program())) {
            program = programRepository.findByProgramNameIgnoreCase(trimToNull(row.program()))
                    .orElseThrow(() -> new BulkRowException("Invalid Program", true));
        }

        Department department = null;
        if (isNotBlank(row.department())) {
            department = departmentRepository.findByDepartmentNameIgnoreCase(trimToNull(row.department()))
                    .orElseThrow(() -> new BulkRowException("Invalid Department", true));
        }

        BigDecimal grandTotalFee = null;
        if (isNotBlank(row.grandTotalFee())) {
            grandTotalFee = parseAmount(row.grandTotalFee());
            if (grandTotalFee == null) {
                throw new BulkRowException("Invalid Grand Total Fee", true);
            }
        }

        StudentStatus status = null;
        if (isNotBlank(row.status())) {
            try {
                status = StudentStatus.valueOf(trimToNull(row.status()).toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BulkRowException("Invalid Status", true);
            }
        }

        return new RowData(dob, gender, caste, admissionCategory, program, department, grandTotalFee, status);
    }

    /**
     * Applies only the columns that contain a value, across all related tables.
     * Blank cells never overwrite existing database values.
     */
    private void applyBulkRow(Student student, UpdateRow row, RowData data) {
        if (isNotBlank(row.registerNumber())) {
            student.setRegisterNo(trimToNull(row.registerNumber()));
        }
        if (isNotBlank(row.applicationNumber())) {
            student.setApplicationNo(trimToNull(row.applicationNumber()));
        }
        if (isNotBlank(row.studentName())) {
            student.setStudentName(trimToNull(row.studentName()));
        }
        if (data.dateOfBirth() != null) {
            student.setDateOfBirth(data.dateOfBirth());
            student.setAge(computeAge(data.dateOfBirth()));
        }
        if (data.gender() != null) {
            student.setGender(data.gender());
        }
        if (isNotBlank(row.aadhaarNumber())) {
            student.setAadhaarNo(trimToNull(row.aadhaarNumber()));
        }
        if (isNotBlank(row.district())) {
            student.setDistrict(trimToNull(row.district()));
        }
        if (data.caste() != null) {
            student.setCaste(data.caste());
        }

        if (isNotBlank(row.fatherName()) || isNotBlank(row.fatherMobile())) {
            ParentDetails parent = student.getParent();
            if (parent == null) {
                parent = new ParentDetails();
                parent.setStudent(student);
                student.setParent(parent);
            }
            if (isNotBlank(row.fatherName())) {
                parent.setFatherName(trimToNull(row.fatherName()));
            }
            if (isNotBlank(row.fatherMobile())) {
                parent.setFatherMobileNo(trimToNull(row.fatherMobile()));
            }
        }

        if (isNotBlank(row.mobileNumber()) || isNotBlank(row.email())) {
            Address permanent = getAddress(student, AddressType.PERMANENT);
            Address communication = getAddress(student, AddressType.COMMUNICATION);
            if (isNotBlank(row.mobileNumber())) {
                permanent.setMobile(trimToNull(row.mobileNumber()));
                communication.setMobile(trimToNull(row.mobileNumber()));
            }
            if (isNotBlank(row.email())) {
                permanent.setEmail(trimToNull(row.email()));
                communication.setEmail(trimToNull(row.email()));
            }
        }

        if (data.admissionCategory() != null || data.program() != null
                || data.department() != null || isNotBlank(row.batch())) {
            Admission admission = student.getAdmission();
            if (admission == null) {
                admission = new Admission();
                admission.setStudent(student);
                student.setAdmission(admission);
            }
            if (data.admissionCategory() != null) {
                admission.setCategory(data.admissionCategory());
            }
            if (data.program() != null) {
                admission.setProgram(data.program());
            }
            if (data.department() != null) {
                admission.setDepartment(data.department());
            }
            if (isNotBlank(row.batch())) {
                admission.setBatch(trimToNull(row.batch()));
            }
        }

        if (data.grandTotalFee() != null) {
            StudentFee fee = student.getFee();
            if (fee == null) {
                fee = new StudentFee();
                fee.setStudent(student);
                student.setFee(fee);
            }
            fee.setTotalFee(data.grandTotalFee());
            if (fee.getPaymentStatus() == null) {
                fee.setPaymentStatus(PaymentStatus.PENDING);
            }
        }

        if (data.status() != null) {
            LocalDateTime now = LocalDateTime.now();
            student.setStatus(data.status());
            if (data.status() == StudentStatus.ARCHIVED) {
                student.setArchivedAt(now);
                if (isNotBlank(row.archiveReason())) {
                    student.setArchiveReason(trimToNull(row.archiveReason()));
                }
            } else {
                student.setArchivedAt(null);
                student.setArchiveReason(null);
            }
        } else if (isNotBlank(row.archiveReason())) {
            student.setArchiveReason(trimToNull(row.archiveReason()));
        }
    }

    // ---------- Private helpers ----------

    private void applyPersonal(Student student, PersonalStepRequest req) {
        student.setApplicationNo(req.applicationNumber());
        student.setRegisterNo(req.registerNumber());
        student.setStudentName(req.studentName());
        student.setDateOfBirth(req.dateOfBirth());
        student.setAge(computeAge(req.dateOfBirth()));
        student.setAadhaarNo(req.aadhaarNumber());
        student.setGender(req.gender());
        student.setDistrict(req.district());
        student.setNationality(req.nationality());
        student.setCaste(req.caste());
    }

    private Student findByIdentifier(String applicationNumber, String registerNumber) {
        if (isNotBlank(registerNumber)) {
            Student byReg = studentRepository.findByRegisterNoIgnoreCase(registerNumber).orElse(null);
            if (byReg != null) {
                return byReg;
            }
        }
        if (isNotBlank(applicationNumber)) {
            return studentRepository.findByApplicationNoIgnoreCase(applicationNumber).orElse(null);
        }
        return null;
    }

    private LocalDate parseDate(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }

    private Gender parseGender(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        return switch (value.toUpperCase()) {
            case "MALE", "M" -> Gender.MALE;
            case "FEMALE", "F" -> Gender.FEMALE;
            case "TRANSGENDER", "T" -> Gender.TRANSGENDER;
            default -> null;
        };
    }

    private Caste parseCaste(String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        String upper = value.toUpperCase();
        if ("OBC".equals(upper)) {
            return Caste.BC;
        }
        try {
            return Caste.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replace(",", "");
        cleaned = cleaned.replaceAll("(?i)^\\s*(rs\\.?|inr|rupees|₹|\\$|€|£)\\s*", "");
        cleaned = cleaned.replaceAll("\\s+", "");
        if (cleaned.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private record RowData(LocalDate dateOfBirth, Gender gender, Caste caste,
                           AdmissionCategory admissionCategory, Program program, Department department,
                           BigDecimal grandTotalFee, StudentStatus status) {
    }

    private static class BulkRowException extends RuntimeException {
        private final String reason;
        private final boolean failure;

        BulkRowException(String reason, boolean failure) {
            this.reason = reason;
            this.failure = failure;
        }

        String getReason() {
            return reason;
        }

        boolean isFailure() {
            return failure;
        }
    }

    private Integer computeAge(java.time.LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
    }

    private Address getAddress(Student student, AddressType type) {
        for (Address address : student.getAddresses()) {
            if (address.getAddressType() == type) {
                return address;
            }
        }
        Address address = new Address();
        address.setStudent(student);
        address.setAddressType(type);
        student.getAddresses().add(address);
        return address;
    }

    private QualifyingExam getOrCreateQualifyingExam(Student student) {
        QualifyingExam exam = student.getQualifyingExam();
        if (exam == null) {
            exam = new QualifyingExam();
            exam.setStudent(student);
            student.setQualifyingExam(exam);
        }
        return exam;
    }

    private BigDecimal resolveAggregate(DiplomaStepRequest request) {
        if (request.aggregatePercentage() != null) {
            return request.aggregatePercentage();
        }
        if (request.secondYearPercentage() != null && request.thirdYearPercentage() != null) {
            return request.secondYearPercentage().add(request.thirdYearPercentage())
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private void touch(Student student) {
        student.setUpdatedAt(LocalDateTime.now());
    }

    private void assertNotArchived(Student student) {
        if (student.getStatus() == StudentStatus.ARCHIVED) {
            throw new IllegalStateException("Archived students cannot be modified. Restore the student first.");
        }
    }

    private Student getStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    @Transactional
    public List<StudentResponseDto> seed10FakeStudents() {
        AdmissionCategory centac = categoryRepository.findByCategoryNameIgnoreCase("CENTAC")
                .orElseGet(() -> {
                    AdmissionCategory c = new AdmissionCategory();
                    c.setCategoryName("CENTAC");
                    return categoryRepository.save(c);
                });
        AdmissionCategory management = categoryRepository.findByCategoryNameIgnoreCase("Management")
                .orElseGet(() -> {
                    AdmissionCategory m = new AdmissionCategory();
                    m.setCategoryName("Management");
                    return categoryRepository.save(m);
                });
        Program btechProgram = programRepository.findByProgramNameIgnoreCase("First Year B.Tech")
                .orElseGet(() -> {
                    Program p = new Program();
                    p.setProgramName("First Year B.Tech");
                    p.setDurationYears(4);
                    return programRepository.save(p);
                });

        Map<String, Department> depts = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getDepartmentName, d -> d, (a, b) -> a));

        List<Student> seededStudents = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        String[][] fakeData = {
            {"RGCET/2026/2001", "26BTECH001", "Aarav Sharma", "2008-05-14", "987654321001", "MALE", "Puducherry", "Indian", "BC", "Rajesh Sharma", "9840123451", "Computer Science & Engineering (CSE)", "CENTAC", "75000", "123 MG Road, Puducherry", "605001", "aarav.sharma@example.com"},
            {"RGCET/2026/2002", "26BTECH002", "Ananya Ramakrishnan", "2008-08-20", "987654321002", "FEMALE", "Chennai", "Indian", "OC", "Ramakrishnan V", "9840123452", "Artificial Intelligence and Data Science (AI&DS)", "Management", "90000", "45 Anna Nagar, Chennai", "600040", "ananya.r@example.com"},
            {"RGCET/2026/2003", "26BTECH003", "Rahul Varma", "2008-02-11", "987654321003", "MALE", "Cuddalore", "Indian", "MBC", "Suresh Varma", "9840123453", "Information Technology (IT)", "CENTAC", "75000", "88 Beach Road, Cuddalore", "607001", "rahul.varma@example.com"},
            {"RGCET/2026/2004", "26BTECH004", "Kavya Subramanian", "2008-11-05", "987654321004", "FEMALE", "Karaikal", "Indian", "SC", "Subramanian K", "9840123454", "Electronics & Communication Engineering (ECE)", "CENTAC", "75000", "12 Church Street, Karaikal", "609602", "kavya.subu@example.com"},
            {"RGCET/2026/2005", "26BTECH005", "Dhruv Patel", "2008-04-18", "987654321005", "MALE", "Puducherry", "Indian", "OC", "Vikram Patel", "9840123455", "Artificial Intelligence and Machine Learning (AI&ML)", "Management", "90000", "67 Heritage Town, Puducherry", "605001", "dhruv.patel@example.com"},
            {"RGCET/2026/2006", "26BTECH006", "Priya Sundaram", "2008-09-30", "987654321006", "FEMALE", "Villupuram", "Indian", "BC", "Sundaram M", "9840123456", "Biomedical Engineering (BME)", "CENTAC", "75000", "34 Main Road, Villupuram", "605602", "priya.sundaram@example.com"},
            {"RGCET/2026/2007", "26BTECH007", "Vikramaditya Reddy", "2008-01-25", "987654321007", "MALE", "Chidambaram", "Indian", "OC", "Raghunath Reddy", "9840123457", "Computer Science & Engineering (CSE)", "Management", "100000", "90 Temple Street, Chidambaram", "608001", "vikram.reddy@example.com"},
            {"RGCET/2026/2008", "26BTECH008", "Sneha Venkatesh", "2008-07-12", "987654321008", "FEMALE", "Neyveli", "Indian", "MBC", "Venkatesh N", "9840123458", "Information Technology (IT)", "Management", "80000", "15 Township Block 4, Neyveli", "607801", "sneha.v@example.com"},
            {"RGCET/2026/2009", "26BTECH009", "Karthik Nair", "2008-10-02", "987654321009", "MALE", "Puducherry", "Indian", "BC", "Narayanan Nair", "9840123459", "Electronics & Communication Engineering (ECE)", "CENTAC", "75000", "22 ECR Road, Lawspet, Puducherry", "605008", "karthik.nair@example.com"},
            {"RGCET/2026/2010", "26BTECH010", "Divya Iyer", "2008-06-19", "987654321010", "FEMALE", "Puducherry", "Indian", "OC", "Sankar Iyer", "9840123460", "Artificial Intelligence and Data Science (AI&DS)", "CENTAC", "75000", "59 VIP Avenue, Puducherry", "605011", "divya.iyer@example.com"}
        };

        for (String[] data : fakeData) {
            String appNo = data[0];
            if (studentRepository.existsByApplicationNoIgnoreCase(appNo)) {
                continue;
            }
            Student s = new Student();
            s.setApplicationNo(appNo);
            s.setRegisterNo(data[1]);
            s.setStudentName(data[2]);
            s.setDateOfBirth(LocalDate.parse(data[3]));
            s.setAadhaarNo(data[4]);
            s.setGender(Gender.valueOf(data[5]));
            s.setDistrict(data[6]);
            s.setNationality(data[7]);
            s.setCaste(Caste.valueOf(data[8]));
            s.setStatus(StudentStatus.ACTIVE);
            s.setCreatedAt(now);
            s.setUpdatedAt(now);

            ParentDetails p = new ParentDetails();
            p.setStudent(s);
            p.setFatherName(data[9]);
            p.setFatherMobileNo(data[10]);
            p.setFatherOccupation("Private Service / Business");
            p.setAnnualIncome(BigDecimal.valueOf(500000));
            s.setParent(p);

            Address perm = new Address();
            perm.setStudent(s);
            perm.setAddressType(AddressType.PERMANENT);
            perm.setMobile(data[10]);
            perm.setEmail(data[16]);
            perm.setAddressLine(data[14]);
            perm.setPincode(data[15]);
            s.getAddresses().add(perm);

            Address comm = new Address();
            comm.setStudent(s);
            comm.setAddressType(AddressType.COMMUNICATION);
            comm.setMobile(data[10]);
            comm.setEmail(data[16]);
            comm.setAddressLine(data[14]);
            comm.setPincode(data[15]);
            s.getAddresses().add(comm);

            Admission adm = new Admission();
            adm.setStudent(s);
            adm.setProgram(btechProgram);
            adm.setDepartment(depts.getOrDefault(data[11], depts.values().isEmpty() ? null : depts.values().iterator().next()));
            adm.setCategory("CENTAC".equalsIgnoreCase(data[12]) ? centac : management);
            adm.setBatch("2026-2030");
            adm.setDateOfAdmission(LocalDate.of(2026, 6, 1));
            s.setAdmission(adm);

            StudentFee fee = new StudentFee();
            fee.setStudent(s);
            fee.setTotalFee(new BigDecimal(data[13]));
            s.setFee(fee);

            studentRepository.save(s);
            seededStudents.add(s);
        }

        return seededStudents.stream().map(StudentResponseDto::from).collect(Collectors.toList());
    }

    private Specification<Student> byStatus(StudentStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<Student> buildSpecification(String search, Long departmentId, Long programId,
                                                      Long categoryId, String batch, StudentStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                predicates.add(cb.notEqual(root.get("status"), StudentStatus.ARCHIVED));
            }
            if (isNotBlank(search)) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("studentName")), like),
                        cb.like(cb.lower(root.get("applicationNo")), like),
                        cb.like(cb.lower(root.get("registerNo")), like)));
            }
            if (departmentId != null || programId != null || categoryId != null || isNotBlank(batch)) {
                Join<Object, Object> admission = root.join("admission", JoinType.LEFT);
                if (departmentId != null) {
                    predicates.add(cb.equal(admission.get("department").get("departmentId"), departmentId));
                }
                if (programId != null) {
                    predicates.add(cb.equal(admission.get("program").get("programId"), programId));
                }
                if (categoryId != null) {
                    predicates.add(cb.equal(admission.get("category").get("categoryId"), categoryId));
                }
                if (isNotBlank(batch)) {
                    predicates.add(cb.equal(admission.get("batch"), batch));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
