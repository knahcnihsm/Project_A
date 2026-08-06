package com.rgcet.admission.service;

import com.rgcet.admission.dto.bulk.BulkCommitResponse;
import com.rgcet.admission.dto.bulk.BulkIssue;
import com.rgcet.admission.dto.bulk.BulkPreviewRow;
import com.rgcet.admission.dto.bulk.BulkValidationResponse;
import com.rgcet.admission.dto.bulk.BulkWorkbookRequest;
import com.rgcet.admission.dto.bulk.FieldChange;
import com.rgcet.admission.entity.Address;
import com.rgcet.admission.entity.AddressType;
import com.rgcet.admission.entity.Admission;
import com.rgcet.admission.entity.AdmissionCategory;
import com.rgcet.admission.entity.BulkUploadStatus;
import com.rgcet.admission.entity.Caste;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.DiplomaDetails;
import com.rgcet.admission.entity.Gender;
import com.rgcet.admission.entity.HSCAcademicMark;
import com.rgcet.admission.entity.HSCVocationalMark;
import com.rgcet.admission.entity.PGQualification;
import com.rgcet.admission.entity.ParentDetails;
import com.rgcet.admission.entity.Program;
import com.rgcet.admission.entity.QualifyingExam;
import com.rgcet.admission.entity.Student;
import com.rgcet.admission.entity.StudentStatus;
import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.DepartmentRepository;
import com.rgcet.admission.repository.ProgramRepository;
import com.rgcet.admission.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implements the multi-sheet "Bulk Student Details Update" feature.
 * <p>
 * The workbook is sent as typed JSON ({@link BulkWorkbookRequest}). Validation is strict
 * and all-or-nothing: enum and master-data lookups produce hard errors, while free-text
 * fields (Nationality, Board, District, Batch, institution names, month-year) only warn.
 * Blank cells always mean "no change".
 * <p>
 * {@link #validateAndPreview} is read-only and never writes. {@link #commit} applies every
 * valid row in a single transaction, so either the whole workbook is applied or nothing is
 * (failed rows = 0 by design). Audit rows are written to {@code bulk_upload_log}.
 */
@Service
@RequiredArgsConstructor
public class BulkStudentUpdateService {

    private static final String SHEET_PERSONAL = "Student Personal Details";
    private static final String SHEET_PARENT = "Parent / Guardian Details";
    private static final String SHEET_COMMUNICATION = "Communication Address";
    private static final String SHEET_PERMANENT = "Permanent Address";
    private static final String SHEET_ACADEMIC = "Academic Admission Details";
    private static final String SHEET_QUALIFYING = "Qualifying Examination (HSC / CBSE)";
    private static final String SHEET_HSC = "HSC Marks";
    private static final String SHEET_DIPLOMA = "Diploma Qualification Details";
    private static final String SHEET_PG = "PG Qualifying Degree Details";

    private static final Pattern REGISTER_PATTERN = Pattern.compile("^[A-Za-z0-9_./\\\\\\-\\s]{1,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern MONTH_TOKEN = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)");
    private static final Pattern YEAR_TOKEN = Pattern.compile("(19|20)\\d{2}");
    private static final Pattern BATCH_PATTERN = Pattern.compile("\\d{4}-\\d{4}");
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
    private static final Set<String> KNOWN_NATIONALITIES = Set.of(
            "Indian", "Indian (NRI)", "American", "British", "Canadian", "Australian",
            "Singaporean", "Malaysian", "Sri Lankan", "Nepali", "Bangladeshi", "Pakistani",
            "Afghan", "Chinese", "Japanese", "South Korean", "German", "French", "Other");

    private final StudentRepository studentRepository;
    private final AdmissionCategoryRepository categoryRepository;
    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final BulkUploadLogService bulkUploadLogService;
    private final StudentService studentService;

    // ---------- Public API ----------

    @Transactional(readOnly = true)
    public BulkValidationResponse validateAndPreview(BulkWorkbookRequest request) {
        ParsedWorkbook parsed = parseAndValidate(request);
        return new BulkValidationResponse(
                request.fileName(),
                parsed.totalRows,
                parsed.matchedStudentIds.size(),
                parsed.unmatchedRows,
                parsed.errorCount,
                parsed.warningCount,
                parsed.errorCount == 0,
                parsed.issues,
                buildPreview(parsed));
    }

    @Transactional
    public BulkCommitResponse commit(BulkWorkbookRequest request) {
        long start = System.currentTimeMillis();
        ParsedWorkbook parsed = parseAndValidate(request);
        long duration = System.currentTimeMillis() - start;

        if (parsed.errorCount > 0) {
            bulkUploadLogService.record(request.fileName(), request.uploadedBy(),
                    parsed.totalRows, parsed.totalRows - parsed.errorRows,
                    0, 0, parsed.unmatchedRows, parsed.errorRows,
                    duration, BulkUploadStatus.FAILED);
            return new BulkCommitResponse(request.fileName(), uploadedBy(request),
                    LocalDateTime.now(), parsed.totalRows, parsed.totalRows - parsed.errorRows,
                    0, 0, parsed.unmatchedRows, parsed.errorRows,
                    parsed.warningCount, duration, "FAILED", parsed.issues);
        }

        ApplyResult result = applyAll(parsed);
        duration = System.currentTimeMillis() - start;
        bulkUploadLogService.record(request.fileName(), request.uploadedBy(),
                parsed.totalRows, parsed.totalRows,
                result.updatedStudents, result.noChangeRows,
                parsed.unmatchedRows, 0, duration, BulkUploadStatus.SUCCESS);
        return new BulkCommitResponse(request.fileName(), uploadedBy(request),
                LocalDateTime.now(), parsed.totalRows, parsed.totalRows,
                result.updatedStudents, result.noChangeRows,
                parsed.unmatchedRows, 0, parsed.warningCount, duration, "SUCCESS", warnings(parsed.issues));
    }

    // ---------- Parse & validate ----------

    private ParsedWorkbook parseAndValidate(BulkWorkbookRequest request) {
        ParsedWorkbook parsed = new ParsedWorkbook();
        StudentLookup lookup = buildStudentLookup(request);

        for (BulkWorkbookRequest.PersonalRow r : safe(request.personal())) {
            parsed.totalRows++;
            int errorsBefore = parsed.errorCount;
            PersonalApply apply = validatePersonal(r, lookup, parsed);
            if (apply != null) {
                parsed.personal.add(apply);
                parsed.matchedStudentIds.add(apply.student.getStudentId());
            }
            countRowResult(parsed, errorsBefore);
        }
        for (BulkWorkbookRequest.ParentRow r : safe(request.parent())) {
            parsed.totalRows++;
            int errorsBefore = parsed.errorCount;
            ParentApply apply = validateParent(r, lookup, parsed);
            if (apply != null) {
                parsed.parent.add(apply);
                parsed.matchedStudentIds.add(apply.student.getStudentId());
            }
            countRowResult(parsed, errorsBefore);
        }
        for (BulkWorkbookRequest.CommunicationRow r : safe(request.communication())) {
            parsed.totalRows++;
            int errorsBefore = parsed.errorCount;
            AddressApply apply = validateAddress(r.rowNumber(), r.applicationNo(), r.registerNo(),
                    r.addressLine(), r.pincode(), r.phone(), r.mobile(), r.email(),
                    AddressType.COMMUNICATION, SHEET_COMMUNICATION, lookup, parsed);
            if (apply != null) {
                parsed.communication.add(apply);
                parsed.matchedStudentIds.add(apply.student.getStudentId());
            }
            countRowResult(parsed, errorsBefore);
        }
        for (BulkWorkbookRequest.PermanentRow r : safe(request.permanent())) {
            parsed.totalRows++;
            int errorsBefore = parsed.errorCount;
            AddressApply apply = validateAddress(r.rowNumber(), r.applicationNo(), r.registerNo(),
                    r.addressLine(), r.pincode(), r.phone(), r.mobile(), r.email(),
                    AddressType.PERMANENT, SHEET_PERMANENT, lookup, parsed);
            if (apply != null) {
                parsed.permanent.add(apply);
                parsed.matchedStudentIds.add(apply.student.getStudentId());
            }
            countRowResult(parsed, errorsBefore);
        }
        for (BulkWorkbookRequest.AcademicRow r : safe(request.academic())) {
            parsed.totalRows++;
            int errorsBefore = parsed.errorCount;
            AcademicApply apply = validateAcademic(r, lookup, parsed);
            if (apply != null) {
                parsed.academic.add(apply);
                parsed.matchedStudentIds.add(apply.student.getStudentId());
            }
            countRowResult(parsed, errorsBefore);
        }
        for (BulkWorkbookRequest.QualifyingExamRow r : safe(request.qualifyingExam())) {
            parsed.totalRows++;
            int errorsBefore = parsed.errorCount;
            QualifyingExamApply apply = validateQualifyingExam(r, lookup, parsed);
            if (apply != null) {
                parsed.qualifyingExam.add(apply);
                parsed.matchedStudentIds.add(apply.student.getStudentId());
            }
            countRowResult(parsed, errorsBefore);
        }
        for (BulkWorkbookRequest.HscMarksRow r : safe(request.hscMarks())) {
            parsed.totalRows++;
            int errorsBefore = parsed.errorCount;
            HscMarksApply apply = validateHscMarks(r, lookup, parsed);
            if (apply != null) {
                parsed.hscMarks.add(apply);
                parsed.matchedStudentIds.add(apply.student.getStudentId());
            }
            countRowResult(parsed, errorsBefore);
        }
        for (BulkWorkbookRequest.DiplomaRow r : safe(request.diploma())) {
            parsed.totalRows++;
            int errorsBefore = parsed.errorCount;
            DiplomaApply apply = validateDiploma(r, lookup, parsed);
            if (apply != null) {
                parsed.diploma.add(apply);
                parsed.matchedStudentIds.add(apply.student.getStudentId());
            }
            countRowResult(parsed, errorsBefore);
        }
        for (BulkWorkbookRequest.PgRow r : safe(request.pg())) {
            parsed.totalRows++;
            int errorsBefore = parsed.errorCount;
            PgApply apply = validatePg(r, lookup, parsed);
            if (apply != null) {
                parsed.pg.add(apply);
                parsed.matchedStudentIds.add(apply.student.getStudentId());
            }
            countRowResult(parsed, errorsBefore);
        }
        return parsed;
    }

    private void countRowResult(ParsedWorkbook parsed, int errorsBefore) {
        if (parsed.errorCount > errorsBefore) {
            parsed.errorRows++;
        }
    }

    private StudentLookup buildStudentLookup(BulkWorkbookRequest request) {
        Set<String> apps = new HashSet<>();
        Set<String> regs = new HashSet<>();
        for (BulkWorkbookRequest.PersonalRow r : safe(request.personal())) {
            collectIdentifiers(r.applicationNo(), r.registerNo(), apps, regs);
        }
        for (BulkWorkbookRequest.ParentRow r : safe(request.parent())) {
            collectIdentifiers(r.applicationNo(), r.registerNo(), apps, regs);
        }
        for (BulkWorkbookRequest.CommunicationRow r : safe(request.communication())) {
            collectIdentifiers(r.applicationNo(), r.registerNo(), apps, regs);
        }
        for (BulkWorkbookRequest.PermanentRow r : safe(request.permanent())) {
            collectIdentifiers(r.applicationNo(), r.registerNo(), apps, regs);
        }
        for (BulkWorkbookRequest.AcademicRow r : safe(request.academic())) {
            collectIdentifiers(r.applicationNo(), r.registerNo(), apps, regs);
        }
        for (BulkWorkbookRequest.QualifyingExamRow r : safe(request.qualifyingExam())) {
            collectIdentifiers(r.applicationNo(), r.registerNo(), apps, regs);
        }
        for (BulkWorkbookRequest.HscMarksRow r : safe(request.hscMarks())) {
            collectIdentifiers(r.applicationNo(), r.registerNo(), apps, regs);
        }
        for (BulkWorkbookRequest.DiplomaRow r : safe(request.diploma())) {
            collectIdentifiers(r.applicationNo(), r.registerNo(), apps, regs);
        }
        for (BulkWorkbookRequest.PgRow r : safe(request.pg())) {
            collectIdentifiers(r.applicationNo(), r.registerNo(), apps, regs);
        }

        Map<String, Student> byApp = new HashMap<>();
        Map<String, Student> byReg = new HashMap<>();
        if (!apps.isEmpty()) {
            byApp.putAll(studentRepository.findByApplicationNoInIgnoreCase(apps).stream()
                    .collect(Collectors.toMap(s -> s.getApplicationNo().toLowerCase(Locale.ROOT),
                            s -> s, (a, b) -> a)));
        }
        if (!regs.isEmpty()) {
            byReg.putAll(studentRepository.findByRegisterNoInIgnoreCase(regs).stream()
                    .collect(Collectors.toMap(s -> s.getRegisterNo().toLowerCase(Locale.ROOT),
                            s -> s, (a, b) -> a)));
        }
        return new StudentLookup(byApp, byReg);
    }

    private void collectIdentifiers(String applicationNo, String registerNo, Set<String> apps, Set<String> regs) {
        String app = trimToNull(applicationNo);
        String reg = trimToNull(registerNo);
        if (app != null) {
            apps.add(app.toLowerCase(Locale.ROOT));
        }
        if (reg != null) {
            regs.add(reg.toLowerCase(Locale.ROOT));
        }
    }

    private Student resolveStudent(String applicationNo, String registerNo, String sheet,
                                   int rowNumber, StudentLookup lookup, ParsedWorkbook parsed) {
        String reg = trimToNull(registerNo);
        String app = trimToNull(applicationNo);
        if (reg == null && app == null) {
            addError(parsed, sheet, rowNumber, "Identifier",
                    "Missing identifier: provide Register No or Application No");
            parsed.unmatchedRows++;
            return null;
        }
        if (reg != null && !REGISTER_PATTERN.matcher(reg).matches()) {
            addError(parsed, sheet, rowNumber, "Register No", "Invalid Register No format");
            parsed.unmatchedRows++;
            return null;
        }
        if (app != null && !REGISTER_PATTERN.matcher(app).matches()) {
            addError(parsed, sheet, rowNumber, "Application No", "Invalid Application No format");
            parsed.unmatchedRows++;
            return null;
        }
        Student student = null;
        if (reg != null) {
            student = lookup.byReg.get(reg.toLowerCase(Locale.ROOT));
        }
        if (student == null && app != null) {
            student = lookup.byApp.get(app.toLowerCase(Locale.ROOT));
        }
        if (student == null) {
            addError(parsed, sheet, rowNumber, "Identifier",
                    "Student not found for the given Register No / Application No");
            parsed.unmatchedRows++;
            return null;
        }
        if (student.getStatus() == StudentStatus.ARCHIVED) {
            addError(parsed, sheet, rowNumber, "Identifier",
                    "Student is archived and cannot be updated");
            parsed.unmatchedRows++;
            return null;
        }
        return student;
    }

    // ---------- Per-sheet validation ----------

    private PersonalApply validatePersonal(BulkWorkbookRequest.PersonalRow r, StudentLookup lookup, ParsedWorkbook parsed) {
        Student student = resolveStudent(r.applicationNo(), r.registerNo(), SHEET_PERSONAL, r.rowNumber(), lookup, parsed);
        if (student == null) {
            return null;
        }
        String newApp = trimToNull(r.applicationNo());
        if (newApp != null && !student.getApplicationNo().equalsIgnoreCase(newApp)
                && studentRepository.existsByApplicationNoIgnoreCase(newApp)) {
            addError(parsed, SHEET_PERSONAL, r.rowNumber(), "Application No",
                    "Application number already exists for another student");
        }
        LocalDate dob = parseDate(r.dateOfBirth(), SHEET_PERSONAL, r.rowNumber(), "Date of Birth", parsed);
        Gender gender = parseGender(r.gender(), SHEET_PERSONAL, r.rowNumber(), "Gender", parsed);
        Caste caste = parseCaste(r.caste(), SHEET_PERSONAL, r.rowNumber(), "Caste", parsed);
        String aadhaar = parseDigits(r.aadhaarNumber(), 12, SHEET_PERSONAL, r.rowNumber(), "Aadhaar No", parsed);
        warnFreeText(r.nationality(), SHEET_PERSONAL, r.rowNumber(), "Nationality", parsed);
        warnFreeText(r.district(), SHEET_PERSONAL, r.rowNumber(), "District", parsed);
        if (isNotBlank(r.nationality())
                && !KNOWN_NATIONALITIES.contains(trimToNull(r.nationality()))) {
            addWarning(parsed, SHEET_PERSONAL, r.rowNumber(), "Nationality",
                    "Unrecognized nationality (accepted as-is)");
        }
        return new PersonalApply(student, r.rowNumber(),
                trimToNull(r.registerNo()), newApp, trimToNull(r.studentName()),
                dob, gender, aadhaar, trimToNull(r.nationality()), trimToNull(r.district()), caste);
    }

    private ParentApply validateParent(BulkWorkbookRequest.ParentRow r, StudentLookup lookup, ParsedWorkbook parsed) {
        Student student = resolveStudent(r.applicationNo(), r.registerNo(), SHEET_PARENT, r.rowNumber(), lookup, parsed);
        if (student == null) {
            return null;
        }
        String fatherMobile = parseDigits(r.fatherMobile(), 10, SHEET_PARENT, r.rowNumber(), "Father Mobile", parsed);
        BigDecimal annualIncome = parseAmount(r.annualIncome(), SHEET_PARENT, r.rowNumber(), "Annual Family Income", parsed);
        warnFreeText(r.fatherName(), SHEET_PARENT, r.rowNumber(), "Father Name", parsed);
        warnFreeText(r.fatherOccupation(), SHEET_PARENT, r.rowNumber(), "Father Occupation", parsed);
        return new ParentApply(student, r.rowNumber(), trimToNull(r.fatherName()), fatherMobile,
                trimToNull(r.fatherOccupation()), annualIncome);
    }

    private AddressApply validateAddress(int rowNumber, String applicationNo, String registerNo,
                                         String addressLine, String pincode, String phone, String mobile,
                                         String email, AddressType type, String sheet,
                                         StudentLookup lookup, ParsedWorkbook parsed) {
        Student student = resolveStudent(applicationNo, registerNo, sheet, rowNumber, lookup, parsed);
        if (student == null) {
            return null;
        }
        String pin = parseDigits(pincode, 6, sheet, rowNumber, "PIN Code", parsed);
        String ph = parseDigits(phone, 10, sheet, rowNumber, "Phone No", parsed);
        String mob = parseDigits(mobile, 10, sheet, rowNumber, "Mobile No", parsed);
        String mail = parseEmail(email, sheet, rowNumber, "Email ID", parsed);
        warnFreeText(addressLine, sheet, rowNumber, "Address Line", parsed);
        return new AddressApply(student, rowNumber, type,
                trimToNull(addressLine), pin, ph, mob, mail);
    }

    private AcademicApply validateAcademic(BulkWorkbookRequest.AcademicRow r, StudentLookup lookup, ParsedWorkbook parsed) {
        Student student = resolveStudent(r.applicationNo(), r.registerNo(), SHEET_ACADEMIC, r.rowNumber(), lookup, parsed);
        if (student == null) {
            return null;
        }
        AdmissionCategory category = null;
        if (isNotBlank(r.admissionCategory())) {
            category = categoryRepository.findByCategoryNameIgnoreCase(trimToNull(r.admissionCategory()))
                    .orElse(null);
            if (category == null) {
                addError(parsed, SHEET_ACADEMIC, r.rowNumber(), "Admission Category",
                        "Unknown admission category: " + r.admissionCategory());
            }
        }
        Program program = null;
        if (isNotBlank(r.program())) {
            program = programRepository.findByProgramNameIgnoreCase(trimToNull(r.program()))
                    .orElse(null);
            if (program == null) {
                addError(parsed, SHEET_ACADEMIC, r.rowNumber(), "Program",
                        "Unknown program: " + r.program());
            }
        }
        Department department = null;
        if (isNotBlank(r.department())) {
            department = departmentRepository.findByDepartmentNameIgnoreCase(trimToNull(r.department()))
                    .orElse(null);
            if (department == null) {
                addError(parsed, SHEET_ACADEMIC, r.rowNumber(), "Department",
                        "Unknown department: " + r.department());
            }
        }
        LocalDate dateOfAdmission = parseDate(r.dateOfAdmission(), SHEET_ACADEMIC, r.rowNumber(),
                "Date of Admission", parsed);
        warnFreeText(r.batch(), SHEET_ACADEMIC, r.rowNumber(), "Batch", parsed);
        if (isNotBlank(r.batch()) && !BATCH_PATTERN.matcher(trimToNull(r.batch())).matches()) {
            addWarning(parsed, SHEET_ACADEMIC, r.rowNumber(), "Batch",
                    "Batch does not match the expected YYYY-YYYY format (accepted as-is)");
        }
        return new AcademicApply(student, r.rowNumber(), category, program, department,
                trimToNull(r.batch()), dateOfAdmission);
    }

    private QualifyingExamApply validateQualifyingExam(BulkWorkbookRequest.QualifyingExamRow r,
                                                       StudentLookup lookup, ParsedWorkbook parsed) {
        Student student = resolveStudent(r.applicationNo(), r.registerNo(), SHEET_QUALIFYING, r.rowNumber(), lookup, parsed);
        if (student == null) {
            return null;
        }
        BigDecimal sslc = parsePercentage(r.sslcPercentage(), SHEET_QUALIFYING, r.rowNumber(), "SSLC Percentage", parsed);
        BigDecimal hsc = parsePercentage(r.hscPercentage(), SHEET_QUALIFYING, r.rowNumber(), "HSC Percentage", parsed);
        warnFreeText(r.institutionName(), SHEET_QUALIFYING, r.rowNumber(), "Institution Name", parsed);
        warnFreeText(r.institutionPlace(), SHEET_QUALIFYING, r.rowNumber(), "Institution Place", parsed);
        warnFreeText(r.examPassed(), SHEET_QUALIFYING, r.rowNumber(), "Exam Passed", parsed);
        warnMonthYear(r.monthYearPassing(), SHEET_QUALIFYING, r.rowNumber(), "Month & Year of Passing", parsed);
        return new QualifyingExamApply(student, r.rowNumber(),
                trimToNull(r.institutionName()), trimToNull(r.institutionPlace()),
                trimToNull(r.examPassed()), trimToNull(r.monthYearPassing()),
                sslc, trimToNull(r.sslcRegisterNumber()), hsc, trimToNull(r.hscRegisterNumber()));
    }

    private HscMarksApply validateHscMarks(BulkWorkbookRequest.HscMarksRow r, StudentLookup lookup, ParsedWorkbook parsed) {
        Student student = resolveStudent(r.applicationNo(), r.registerNo(), SHEET_HSC, r.rowNumber(), lookup, parsed);
        if (student == null) {
            return null;
        }
        List<HSCAcademicMark> academic = new ArrayList<>();
        for (BulkWorkbookRequest.SubjectRow s : safe(r.academicMarks())) {
            HSCAcademicMark mark = validateSubject(s, SHEET_HSC, r.rowNumber(), parsed);
            if (mark != null) {
                academic.add(mark);
            }
        }
        List<HSCVocationalMark> vocational = new ArrayList<>();
        for (BulkWorkbookRequest.SubjectRow s : safe(r.vocationalMarks())) {
            HSCVocationalMark mark = validateVocationalSubject(s, SHEET_HSC, r.rowNumber(), parsed);
            if (mark != null) {
                vocational.add(mark);
            }
        }
        return new HscMarksApply(student, r.rowNumber(), academic, vocational);
    }

    private HSCAcademicMark validateSubject(BulkWorkbookRequest.SubjectRow s, String sheet, int rowNumber, ParsedWorkbook parsed) {
        if (isBlank(s.subject()) && isBlank(s.maxMarks()) && isBlank(s.marksObtained())) {
            return null;
        }
        BigDecimal max = parseMark(s.maxMarks(), sheet, rowNumber, "Maximum Marks", parsed);
        BigDecimal obtained = parseMark(s.marksObtained(), sheet, rowNumber, "Marks Obtained", parsed);
        if (max != null && obtained != null && obtained.compareTo(max) > 0) {
            addError(parsed, sheet, rowNumber, "Marks Obtained",
                    "Marks obtained cannot exceed maximum marks");
        }
        if (max == null || obtained == null) {
            return null;
        }
        String name = trimToNull(s.subject());
        if (name == null) {
            addError(parsed, sheet, rowNumber, "Subject Name", "Subject name is required");
            return null;
        }
        warnMonthYear(s.monthYear(), sheet, rowNumber, "Month & Year", parsed);
        HSCAcademicMark mark = new HSCAcademicMark();
        mark.setSubjectName(name);
        mark.setMonthYear(trimToNull(s.monthYear()));
        mark.setMaximumMarks(max);
        mark.setMarksObtained(obtained);
        mark.setPercentage(CutoffCalculator.subjectPercentage(obtained, max));
        return mark;
    }

    private HSCVocationalMark validateVocationalSubject(BulkWorkbookRequest.SubjectRow s, String sheet, int rowNumber, ParsedWorkbook parsed) {
        if (isBlank(s.subject()) && isBlank(s.maxMarks()) && isBlank(s.marksObtained())) {
            return null;
        }
        BigDecimal max = parseMark(s.maxMarks(), sheet, rowNumber, "Maximum Marks", parsed);
        BigDecimal obtained = parseMark(s.marksObtained(), sheet, rowNumber, "Marks Obtained", parsed);
        if (max != null && obtained != null && obtained.compareTo(max) > 0) {
            addError(parsed, sheet, rowNumber, "Marks Obtained",
                    "Marks obtained cannot exceed maximum marks");
        }
        if (max == null || obtained == null) {
            return null;
        }
        String name = trimToNull(s.subject());
        if (name == null) {
            addError(parsed, sheet, rowNumber, "Subject Name", "Subject name is required");
            return null;
        }
        warnMonthYear(s.monthYear(), sheet, rowNumber, "Month & Year", parsed);
        HSCVocationalMark mark = new HSCVocationalMark();
        mark.setSubjectName(name);
        mark.setMonthYear(trimToNull(s.monthYear()));
        mark.setMaximumMarks(max);
        mark.setMarksObtained(obtained);
        mark.setPercentage(CutoffCalculator.subjectPercentage(obtained, max));
        return mark;
    }

    private DiplomaApply validateDiploma(BulkWorkbookRequest.DiplomaRow r, StudentLookup lookup, ParsedWorkbook parsed) {
        Student student = resolveStudent(r.applicationNo(), r.registerNo(), SHEET_DIPLOMA, r.rowNumber(), lookup, parsed);
        if (student == null) {
            return null;
        }
        BigDecimal secondYear = parsePercentage(r.secondYearPercentage(), SHEET_DIPLOMA, r.rowNumber(),
                "Second Year Percentage", parsed);
        BigDecimal thirdYear = parsePercentage(r.thirdYearPercentage(), SHEET_DIPLOMA, r.rowNumber(),
                "Third Year Percentage", parsed);
        BigDecimal aggregate = parsePercentage(r.aggregatePercentage(), SHEET_DIPLOMA, r.rowNumber(),
                "Aggregate Percentage", parsed);
        warnFreeText(r.diplomaCourse(), SHEET_DIPLOMA, r.rowNumber(), "Diploma Course", parsed);
        warnFreeText(r.institutionName(), SHEET_DIPLOMA, r.rowNumber(), "Institution Name", parsed);
        warnFreeText(r.board(), SHEET_DIPLOMA, r.rowNumber(), "Board", parsed);
        return new DiplomaApply(student, r.rowNumber(), trimToNull(r.diplomaCourse()),
                trimToNull(r.institutionName()), trimToNull(r.board()), secondYear, thirdYear, aggregate);
    }

    private PgApply validatePg(BulkWorkbookRequest.PgRow r, StudentLookup lookup, ParsedWorkbook parsed) {
        Student student = resolveStudent(r.applicationNo(), r.registerNo(), SHEET_PG, r.rowNumber(), lookup, parsed);
        if (student == null) {
            return null;
        }
        BigDecimal total = parsePercentage(r.totalPercentage(), SHEET_PG, r.rowNumber(), "Total Percentage", parsed);
        BigDecimal mainSubject = parsePercentage(r.mainSubjectPercentage(), SHEET_PG, r.rowNumber(),
                "Main Subject Percentage", parsed);
        warnFreeText(r.universityName(), SHEET_PG, r.rowNumber(), "University Name", parsed);
        warnFreeText(r.universityPlace(), SHEET_PG, r.rowNumber(), "University Place", parsed);
        warnFreeText(r.institutionName(), SHEET_PG, r.rowNumber(), "Institution Name", parsed);
        warnFreeText(r.institutionPlace(), SHEET_PG, r.rowNumber(), "Institution Place", parsed);
        warnFreeText(r.examPassed(), SHEET_PG, r.rowNumber(), "Exam Passed", parsed);
        warnMonthYear(r.monthYearPassing(), SHEET_PG, r.rowNumber(), "Month & Year of Passing", parsed);
        return new PgApply(student, r.rowNumber(),
                trimToNull(r.universityName()), trimToNull(r.universityPlace()),
                trimToNull(r.institutionName()), trimToNull(r.institutionPlace()),
                trimToNull(r.examPassed()), trimToNull(r.monthYearPassing()),
                total, mainSubject, trimToNull(r.degreeRegistrationNumber()));
    }

    // ---------- Value parsers ----------

    private LocalDate parseDate(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
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
        addError(parsed, sheet, rowNumber, field, "Invalid date format");
        return null;
    }

    private Gender parseGender(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        Gender gender = switch (value.toUpperCase()) {
            case "MALE", "M" -> Gender.MALE;
            case "FEMALE", "F" -> Gender.FEMALE;
            case "TRANSGENDER", "T" -> Gender.TRANSGENDER;
            default -> null;
        };
        if (gender == null) {
            addError(parsed, sheet, rowNumber, field, "Invalid gender");
        }
        return gender;
    }

    private Caste parseCaste(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
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
            addError(parsed, sheet, rowNumber, field, "Invalid caste / category");
            return null;
        }
    }

    private String parseDigits(String raw, int length, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        if (!value.matches("\\d{" + length + "}")) {
            addError(parsed, sheet, rowNumber, field, "Must be exactly " + length + " digits");
            return null;
        }
        return value;
    }

    private String parseEmail(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            addError(parsed, sheet, rowNumber, field, "Invalid email address");
            return null;
        }
        return value;
    }

    private BigDecimal parsePercentage(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
        BigDecimal value = parseDecimal(raw, sheet, rowNumber, field, parsed);
        if (value == null) {
            return null;
        }
        if (value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            addError(parsed, sheet, rowNumber, field, "Percentage must be between 0 and 100");
            return null;
        }
        return value;
    }

    private BigDecimal parseMark(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
        BigDecimal value = parseDecimal(raw, sheet, rowNumber, field, parsed);
        if (value == null) {
            return null;
        }
        if (value.signum() < 0) {
            addError(parsed, sheet, rowNumber, field, "Value must not be negative");
            return null;
        }
        return value;
    }

    private BigDecimal parseAmount(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
        BigDecimal value = parseDecimal(raw, sheet, rowNumber, field, parsed);
        if (value == null) {
            return null;
        }
        if (value.signum() < 0) {
            addError(parsed, sheet, rowNumber, field, "Value must not be negative");
            return null;
        }
        return value;
    }

    private BigDecimal parseDecimal(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            addError(parsed, sheet, rowNumber, field, "Invalid numeric value");
            return null;
        }
    }

    // ---------- Soft / free-text warnings ----------

    private void warnFreeText(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
        String value = trimToNull(raw);
        if (value != null && value.length() > 100) {
            addWarning(parsed, sheet, rowNumber, field, "Value is unusually long (accepted as-is)");
        }
    }

    private void warnMonthYear(String raw, String sheet, int rowNumber, String field, ParsedWorkbook parsed) {
        String value = trimToNull(raw);
        if (value == null) {
            return;
        }
        if (!YEAR_TOKEN.matcher(value).find() || !MONTH_TOKEN.matcher(value).find()) {
            addWarning(parsed, sheet, rowNumber, field,
                    "Does not look like a month-year (accepted as-is)");
        }
    }

    // ---------- Apply ----------

    private ApplyResult applyAll(ParsedWorkbook parsed) {
        Set<Long> changed = new LinkedHashSet<>();
        int noChangeRows = 0;

        for (PersonalApply a : parsed.personal) {
            if (applyPersonal(a)) {
                changed.add(a.student.getStudentId());
            } else {
                noChangeRows++;
            }
        }
        for (ParentApply a : parsed.parent) {
            if (applyParent(a)) {
                changed.add(a.student.getStudentId());
            } else {
                noChangeRows++;
            }
        }
        for (AddressApply a : parsed.communication) {
            if (applyAddress(a)) {
                changed.add(a.student.getStudentId());
            } else {
                noChangeRows++;
            }
        }
        for (AddressApply a : parsed.permanent) {
            if (applyAddress(a)) {
                changed.add(a.student.getStudentId());
            } else {
                noChangeRows++;
            }
        }
        for (AcademicApply a : parsed.academic) {
            if (applyAcademic(a)) {
                changed.add(a.student.getStudentId());
            } else {
                noChangeRows++;
            }
        }
        for (QualifyingExamApply a : parsed.qualifyingExam) {
            if (applyQualifyingExam(a)) {
                changed.add(a.student.getStudentId());
            } else {
                noChangeRows++;
            }
        }
        for (HscMarksApply a : parsed.hscMarks) {
            if (applyHscMarks(a)) {
                changed.add(a.student.getStudentId());
            } else {
                noChangeRows++;
            }
        }
        for (DiplomaApply a : parsed.diploma) {
            if (applyDiploma(a)) {
                changed.add(a.student.getStudentId());
            } else {
                noChangeRows++;
            }
        }
        for (PgApply a : parsed.pg) {
            if (applyPg(a)) {
                changed.add(a.student.getStudentId());
            } else {
                noChangeRows++;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (Student s : parsed.allStudents()) {
            s.setUpdatedAt(now);
            studentRepository.save(s);
        }
        return new ApplyResult(changed.size(), noChangeRows);
    }

    private boolean applyPersonal(PersonalApply a) {
        boolean changed = false;
        if (a.registerNo != null
                && !a.registerNo.equalsIgnoreCase(a.student.getRegisterNo())) {
            a.student.setRegisterNo(a.registerNo);
            changed = true;
        }
        if (a.applicationNo != null
                && !a.applicationNo.equalsIgnoreCase(a.student.getApplicationNo())) {
            a.student.setApplicationNo(a.applicationNo);
            changed = true;
        }
        if (a.studentName != null) {
            a.student.setStudentName(a.studentName);
            changed = true;
        }
        if (a.dateOfBirth != null) {
            a.student.setDateOfBirth(a.dateOfBirth);
            a.student.setAge(studentService.computeAge(a.dateOfBirth));
            changed = true;
        }
        if (a.gender != null) {
            a.student.setGender(a.gender);
            changed = true;
        }
        if (a.aadhaarNumber != null) {
            a.student.setAadhaarNo(a.aadhaarNumber);
            changed = true;
        }
        if (a.nationality != null) {
            a.student.setNationality(a.nationality);
            changed = true;
        }
        if (a.district != null) {
            a.student.setDistrict(a.district);
            changed = true;
        }
        if (a.caste != null) {
            a.student.setCaste(a.caste);
            changed = true;
        }
        return changed;
    }

    private boolean applyParent(ParentApply a) {
        if (a.fatherName == null && a.fatherMobile == null
                && a.fatherOccupation == null && a.annualIncome == null) {
            return false;
        }
        ParentDetails parent = a.student.getParent();
        if (parent == null) {
            parent = new ParentDetails();
            parent.setStudent(a.student);
            a.student.setParent(parent);
        }
        if (a.fatherName != null) {
            parent.setFatherName(a.fatherName);
        }
        if (a.fatherMobile != null) {
            parent.setFatherMobileNo(a.fatherMobile);
        }
        if (a.fatherOccupation != null) {
            parent.setFatherOccupation(a.fatherOccupation);
        }
        if (a.annualIncome != null) {
            parent.setAnnualIncome(a.annualIncome);
        }
        return true;
    }

    private boolean applyAddress(AddressApply a) {
        if (a.addressLine == null && a.pincode == null && a.phone == null
                && a.mobile == null && a.email == null) {
            return false;
        }
        Address address = studentService.getAddress(a.student, a.type);
        if (a.addressLine != null) {
            address.setAddressLine(a.addressLine);
        }
        if (a.pincode != null) {
            address.setPincode(a.pincode);
        }
        if (a.phone != null) {
            address.setPhone(a.phone);
        }
        if (a.mobile != null) {
            address.setMobile(a.mobile);
        }
        if (a.email != null) {
            address.setEmail(a.email);
        }
        return true;
    }

    private boolean applyAcademic(AcademicApply a) {
        if (a.category == null && a.program == null && a.department == null
                && a.batch == null && a.dateOfAdmission == null) {
            return false;
        }
        Admission admission = a.student.getAdmission();
        if (admission == null) {
            admission = new Admission();
            admission.setStudent(a.student);
            a.student.setAdmission(admission);
        }
        if (a.category != null) {
            admission.setCategory(a.category);
        }
        if (a.program != null) {
            admission.setProgram(a.program);
        }
        if (a.department != null) {
            admission.setDepartment(a.department);
        }
        if (a.batch != null) {
            admission.setBatch(a.batch);
        }
        if (a.dateOfAdmission != null) {
            admission.setDateOfAdmission(a.dateOfAdmission);
        }
        studentService.recomputeFeeIfPresent(a.student);
        return true;
    }

    private boolean applyQualifyingExam(QualifyingExamApply a) {
        if (a.institutionName == null && a.institutionPlace == null && a.examPassed == null
                && a.monthYearPassing == null && a.sslcPercentage == null && a.sslcRegisterNumber == null
                && a.hscPercentage == null && a.hscRegisterNumber == null) {
            return false;
        }
        QualifyingExam exam = studentService.getOrCreateQualifyingExam(a.student);
        if (a.institutionName != null) {
            exam.setInstitutionName(a.institutionName);
        }
        if (a.institutionPlace != null) {
            exam.setInstitutionPlace(a.institutionPlace);
        }
        if (a.examPassed != null) {
            exam.setExamPassed(a.examPassed);
        }
        if (a.monthYearPassing != null) {
            exam.setMonthYearOfPassing(a.monthYearPassing);
        }
        if (a.sslcPercentage != null) {
            exam.setSslcPercentage(a.sslcPercentage);
        }
        if (a.sslcRegisterNumber != null) {
            exam.setSslcRegistrationNo(a.sslcRegisterNumber);
        }
        if (a.hscPercentage != null) {
            exam.setHscPercentage(a.hscPercentage);
        }
        if (a.hscRegisterNumber != null) {
            exam.setHscRegistrationNo(a.hscRegisterNumber);
        }
        studentService.recomputeFeeIfPresent(a.student);
        return true;
    }

    private boolean applyHscMarks(HscMarksApply a) {
        if (a.academicMarks.isEmpty() && a.vocationalMarks.isEmpty()) {
            return false;
        }
        QualifyingExam exam = studentService.getOrCreateQualifyingExam(a.student);
        if (!a.academicMarks.isEmpty()) {
            a.academicMarks.forEach(m -> m.setQualifyingExam(exam));
            exam.getAcademicMarks().clear();
            exam.getAcademicMarks().addAll(a.academicMarks);
        }
        if (!a.vocationalMarks.isEmpty()) {
            a.vocationalMarks.forEach(m -> m.setQualifyingExam(exam));
            exam.getVocationalMarks().clear();
            exam.getVocationalMarks().addAll(a.vocationalMarks);
        }
        BigDecimal calcOverall = CutoffCalculator.overallPercentage(
                exam.getAcademicMarks(), exam.getVocationalMarks());
        if (exam.getHscPercentage() == null && calcOverall != null
                && calcOverall.compareTo(BigDecimal.ZERO) > 0) {
            exam.setHscPercentage(calcOverall);
        }
        studentService.recomputeFeeIfPresent(a.student);
        return true;
    }

    private boolean applyDiploma(DiplomaApply a) {
        if (a.diploma == null && a.institutionName == null && a.board == null
                && a.secondYearPercentage == null && a.thirdYearPercentage == null
                && a.aggregatePercentage == null) {
            return false;
        }
        DiplomaDetails diploma = a.student.getDiplomaDetails();
        if (diploma == null) {
            diploma = new DiplomaDetails();
            diploma.setStudent(a.student);
            a.student.setDiplomaDetails(diploma);
        }
        if (a.diploma != null) {
            diploma.setDiploma(a.diploma);
        }
        if (a.institutionName != null) {
            diploma.setInstitutionName(a.institutionName);
        }
        if (a.board != null) {
            diploma.setBoard(a.board);
        }
        if (a.secondYearPercentage != null) {
            diploma.setSecondYearPercentage(a.secondYearPercentage);
        }
        if (a.thirdYearPercentage != null) {
            diploma.setThirdYearPercentage(a.thirdYearPercentage);
        }
        if (a.aggregatePercentage != null) {
            diploma.setAggregatePercentage(a.aggregatePercentage);
        } else if (a.secondYearPercentage != null && a.thirdYearPercentage != null) {
            diploma.setAggregatePercentage(a.secondYearPercentage.add(a.thirdYearPercentage)
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
        }
        studentService.recomputeFeeIfPresent(a.student);
        return true;
    }

    private boolean applyPg(PgApply a) {
        if (a.universityName == null && a.universityPlace == null && a.institutionName == null
                && a.institutionPlace == null && a.examPassed == null && a.monthYearPassing == null
                && a.totalPercentage == null && a.mainSubjectPercentage == null
                && a.degreeRegistrationNumber == null) {
            return false;
        }
        PGQualification pg = a.student.getPgQualification();
        if (pg == null) {
            pg = new PGQualification();
            pg.setStudent(a.student);
            a.student.setPgQualification(pg);
        }
        if (a.universityName != null) {
            pg.setUniversityName(a.universityName);
        }
        if (a.universityPlace != null) {
            pg.setUniversityPlace(a.universityPlace);
        }
        if (a.institutionName != null) {
            pg.setInstitutionName(a.institutionName);
        }
        if (a.institutionPlace != null) {
            pg.setInstitutionPlace(a.institutionPlace);
        }
        if (a.examPassed != null) {
            pg.setExamPassed(a.examPassed);
        }
        if (a.monthYearPassing != null) {
            pg.setMonthYearOfPassing(a.monthYearPassing);
        }
        if (a.totalPercentage != null) {
            pg.setTotalPercentage(a.totalPercentage);
        }
        if (a.mainSubjectPercentage != null) {
            pg.setMainSubjectPercentage(a.mainSubjectPercentage);
        }
        if (a.degreeRegistrationNumber != null) {
            pg.setDegreeRegistrationNo(a.degreeRegistrationNumber);
        }
        studentService.recomputeFeeIfPresent(a.student);
        return true;
    }

    // ---------- Preview ----------

    private List<BulkPreviewRow> buildPreview(ParsedWorkbook parsed) {
        LinkedHashMap<Long, BulkPreviewRow> map = new LinkedHashMap<>();
        for (PersonalApply a : parsed.personal) {
            previewPersonal(a, map);
        }
        for (ParentApply a : parsed.parent) {
            previewParent(a, map);
        }
        for (AddressApply a : parsed.communication) {
            previewAddress(a, SHEET_COMMUNICATION, map);
        }
        for (AddressApply a : parsed.permanent) {
            previewAddress(a, SHEET_PERMANENT, map);
        }
        for (AcademicApply a : parsed.academic) {
            previewAcademic(a, map);
        }
        for (QualifyingExamApply a : parsed.qualifyingExam) {
            previewQualifyingExam(a, map);
        }
        for (HscMarksApply a : parsed.hscMarks) {
            previewHscMarks(a, map);
        }
        for (DiplomaApply a : parsed.diploma) {
            previewDiploma(a, map);
        }
        for (PgApply a : parsed.pg) {
            previewPg(a, map);
        }
        return new ArrayList<>(map.values());
    }

    private List<FieldChange> changes(LinkedHashMap<Long, BulkPreviewRow> map, Student student) {
        BulkPreviewRow row = map.get(student.getStudentId());
        if (row == null) {
            row = new BulkPreviewRow(student.getStudentId(), student.getApplicationNo(),
                    student.getRegisterNo(), student.getStudentName(), new ArrayList<>());
            map.put(student.getStudentId(), row);
        }
        return row.changes();
    }

    private void previewPersonal(PersonalApply a, LinkedHashMap<Long, BulkPreviewRow> map) {
        List<FieldChange> changes = changes(map, a.student);
        diff(SHEET_PERSONAL, "Register No", a.student.getRegisterNo(), a.registerNo, changes);
        diff(SHEET_PERSONAL, "Application No", a.student.getApplicationNo(), a.applicationNo, changes);
        diff(SHEET_PERSONAL, "Student Name", a.student.getStudentName(), a.studentName, changes);
        diff(SHEET_PERSONAL, "Date of Birth", fmt(a.student.getDateOfBirth()), fmt(a.dateOfBirth), changes);
        diff(SHEET_PERSONAL, "Gender", str(a.student.getGender()), str(a.gender), changes);
        diff(SHEET_PERSONAL, "Aadhaar No", a.student.getAadhaarNo(), a.aadhaarNumber, changes);
        diff(SHEET_PERSONAL, "Nationality", a.student.getNationality(), a.nationality, changes);
        diff(SHEET_PERSONAL, "District", a.student.getDistrict(), a.district, changes);
        diff(SHEET_PERSONAL, "Caste", str(a.student.getCaste()), str(a.caste), changes);
    }

    private void previewParent(ParentApply a, LinkedHashMap<Long, BulkPreviewRow> map) {
        List<FieldChange> changes = changes(map, a.student);
        ParentDetails parent = a.student.getParent();
        diff(SHEET_PARENT, "Father Name", parent == null ? null : parent.getFatherName(), a.fatherName, changes);
        diff(SHEET_PARENT, "Father Mobile", parent == null ? null : parent.getFatherMobileNo(), a.fatherMobile, changes);
        diff(SHEET_PARENT, "Father Occupation", parent == null ? null : parent.getFatherOccupation(), a.fatherOccupation, changes);
        diff(SHEET_PARENT, "Annual Family Income",
                parent == null || parent.getAnnualIncome() == null ? null : parent.getAnnualIncome().toPlainString(),
                a.annualIncome == null ? null : a.annualIncome.toPlainString(), changes);
    }

    private void previewAddress(AddressApply a, String sheet, LinkedHashMap<Long, BulkPreviewRow> map) {
        List<FieldChange> changes = changes(map, a.student);
        Address address = a.student.getAddresses().stream()
                .filter(ad -> ad.getAddressType() == a.type)
                .findFirst()
                .orElse(null);
        diff(sheet, "Address Line", address == null ? null : address.getAddressLine(), a.addressLine, changes);
        diff(sheet, "PIN Code", address == null ? null : address.getPincode(), a.pincode, changes);
        diff(sheet, "Phone No", address == null ? null : address.getPhone(), a.phone, changes);
        diff(sheet, "Mobile No", address == null ? null : address.getMobile(), a.mobile, changes);
        diff(sheet, "Email ID", address == null ? null : address.getEmail(), a.email, changes);
    }

    private void previewAcademic(AcademicApply a, LinkedHashMap<Long, BulkPreviewRow> map) {
        List<FieldChange> changes = changes(map, a.student);
        Admission admission = a.student.getAdmission();
        diff(SHEET_ACADEMIC, "Admission Category",
                admission == null || admission.getCategory() == null ? null : admission.getCategory().getCategoryName(),
                a.category == null ? null : a.category.getCategoryName(), changes);
        diff(SHEET_ACADEMIC, "Program",
                admission == null || admission.getProgram() == null ? null : admission.getProgram().getProgramName(),
                a.program == null ? null : a.program.getProgramName(), changes);
        diff(SHEET_ACADEMIC, "Department",
                admission == null || admission.getDepartment() == null ? null : admission.getDepartment().getDepartmentName(),
                a.department == null ? null : a.department.getDepartmentName(), changes);
        diff(SHEET_ACADEMIC, "Batch", admission == null ? null : admission.getBatch(), a.batch, changes);
        diff(SHEET_ACADEMIC, "Date of Admission",
                admission == null ? null : fmt(admission.getDateOfAdmission()),
                fmt(a.dateOfAdmission), changes);
    }

    private void previewQualifyingExam(QualifyingExamApply a, LinkedHashMap<Long, BulkPreviewRow> map) {
        List<FieldChange> changes = changes(map, a.student);
        QualifyingExam exam = a.student.getQualifyingExam();
        diff(SHEET_QUALIFYING, "Institution Name", exam == null ? null : exam.getInstitutionName(), a.institutionName, changes);
        diff(SHEET_QUALIFYING, "Institution Place", exam == null ? null : exam.getInstitutionPlace(), a.institutionPlace, changes);
        diff(SHEET_QUALIFYING, "Exam Passed", exam == null ? null : exam.getExamPassed(), a.examPassed, changes);
        diff(SHEET_QUALIFYING, "Month & Year of Passing", exam == null ? null : exam.getMonthYearOfPassing(), a.monthYearPassing, changes);
        diff(SHEET_QUALIFYING, "SSLC Percentage",
                exam == null || exam.getSslcPercentage() == null ? null : exam.getSslcPercentage().toPlainString(),
                a.sslcPercentage == null ? null : a.sslcPercentage.toPlainString(), changes);
        diff(SHEET_QUALIFYING, "SSLC Register No", exam == null ? null : exam.getSslcRegistrationNo(), a.sslcRegisterNumber, changes);
        diff(SHEET_QUALIFYING, "HSC Percentage",
                exam == null || exam.getHscPercentage() == null ? null : exam.getHscPercentage().toPlainString(),
                a.hscPercentage == null ? null : a.hscPercentage.toPlainString(), changes);
        diff(SHEET_QUALIFYING, "HSC Register No", exam == null ? null : exam.getHscRegistrationNo(), a.hscRegisterNumber, changes);
    }

    private void previewHscMarks(HscMarksApply a, LinkedHashMap<Long, BulkPreviewRow> map) {
        List<FieldChange> changes = changes(map, a.student);
        QualifyingExam exam = a.student.getQualifyingExam();
        for (HSCAcademicMark m : a.academicMarks) {
            HSCAcademicMark old = findAcademic(exam, m.getSubjectName());
            String label = "Academic - " + m.getSubjectName();
            diff(SHEET_HSC, label,
                    old == null ? null : old.getMarksObtained().toPlainString() + " / " + old.getMaximumMarks().toPlainString(),
                    m.getMarksObtained().toPlainString() + " / " + m.getMaximumMarks().toPlainString(), changes);
            diff(SHEET_HSC, label + " Month & Year",
                    old == null ? null : old.getMonthYear(), m.getMonthYear(), changes);
        }
        for (HSCVocationalMark m : a.vocationalMarks) {
            HSCVocationalMark old = findVocational(exam, m.getSubjectName());
            String label = "Vocational - " + m.getSubjectName();
            diff(SHEET_HSC, label,
                    old == null ? null : old.getMarksObtained().toPlainString() + " / " + old.getMaximumMarks().toPlainString(),
                    m.getMarksObtained().toPlainString() + " / " + m.getMaximumMarks().toPlainString(), changes);
            diff(SHEET_HSC, label + " Month & Year",
                    old == null ? null : old.getMonthYear(), m.getMonthYear(), changes);
        }
    }

    private void previewDiploma(DiplomaApply a, LinkedHashMap<Long, BulkPreviewRow> map) {
        List<FieldChange> changes = changes(map, a.student);
        DiplomaDetails d = a.student.getDiplomaDetails();
        diff(SHEET_DIPLOMA, "Diploma Course", d == null ? null : d.getDiploma(), a.diploma, changes);
        diff(SHEET_DIPLOMA, "Institution Name", d == null ? null : d.getInstitutionName(), a.institutionName, changes);
        diff(SHEET_DIPLOMA, "Board", d == null ? null : d.getBoard(), a.board, changes);
        diff(SHEET_DIPLOMA, "Second Year Percentage",
                d == null || d.getSecondYearPercentage() == null ? null : d.getSecondYearPercentage().toPlainString(),
                a.secondYearPercentage == null ? null : a.secondYearPercentage.toPlainString(), changes);
        diff(SHEET_DIPLOMA, "Third Year Percentage",
                d == null || d.getThirdYearPercentage() == null ? null : d.getThirdYearPercentage().toPlainString(),
                a.thirdYearPercentage == null ? null : a.thirdYearPercentage.toPlainString(), changes);
        diff(SHEET_DIPLOMA, "Aggregate Percentage",
                d == null || d.getAggregatePercentage() == null ? null : d.getAggregatePercentage().toPlainString(),
                a.aggregatePercentage == null ? null : a.aggregatePercentage.toPlainString(), changes);
    }

    private void previewPg(PgApply a, LinkedHashMap<Long, BulkPreviewRow> map) {
        List<FieldChange> changes = changes(map, a.student);
        PGQualification p = a.student.getPgQualification();
        diff(SHEET_PG, "University Name", p == null ? null : p.getUniversityName(), a.universityName, changes);
        diff(SHEET_PG, "University Place", p == null ? null : p.getUniversityPlace(), a.universityPlace, changes);
        diff(SHEET_PG, "Institution Name", p == null ? null : p.getInstitutionName(), a.institutionName, changes);
        diff(SHEET_PG, "Institution Place", p == null ? null : p.getInstitutionPlace(), a.institutionPlace, changes);
        diff(SHEET_PG, "Exam Passed", p == null ? null : p.getExamPassed(), a.examPassed, changes);
        diff(SHEET_PG, "Month & Year of Passing", p == null ? null : p.getMonthYearOfPassing(), a.monthYearPassing, changes);
        diff(SHEET_PG, "Total Percentage",
                p == null || p.getTotalPercentage() == null ? null : p.getTotalPercentage().toPlainString(),
                a.totalPercentage == null ? null : a.totalPercentage.toPlainString(), changes);
        diff(SHEET_PG, "Main Subject Percentage",
                p == null || p.getMainSubjectPercentage() == null ? null : p.getMainSubjectPercentage().toPlainString(),
                a.mainSubjectPercentage == null ? null : a.mainSubjectPercentage.toPlainString(), changes);
        diff(SHEET_PG, "Degree Registration No",
                p == null ? null : p.getDegreeRegistrationNo(), a.degreeRegistrationNumber, changes);
    }

    private void diff(String sheet, String field, String oldValue, String newValue, List<FieldChange> changes) {
        if (newValue == null) {
            return;
        }
        if (oldValue == null || !oldValue.equals(newValue)) {
            changes.add(new FieldChange(sheet, field, oldValue == null ? "—" : oldValue, newValue));
        }
    }

    private HSCAcademicMark findAcademic(QualifyingExam exam, String subjectName) {
        if (exam == null || subjectName == null) {
            return null;
        }
        return exam.getAcademicMarks().stream()
                .filter(m -> subjectName.equalsIgnoreCase(m.getSubjectName()))
                .findFirst()
                .orElse(null);
    }

    private HSCVocationalMark findVocational(QualifyingExam exam, String subjectName) {
        if (exam == null || subjectName == null) {
            return null;
        }
        return exam.getVocationalMarks().stream()
                .filter(m -> subjectName.equalsIgnoreCase(m.getSubjectName()))
                .findFirst()
                .orElse(null);
    }

    // ---------- Issue helpers ----------

    private BulkIssue addError(ParsedWorkbook parsed, String sheet, int rowNumber, String field, String message) {
        BulkIssue issue = new BulkIssue(sheet, rowNumber, field, message, BulkIssue.Severity.ERROR);
        parsed.issues.add(issue);
        parsed.errorCount++;
        return issue;
    }

    private BulkIssue addWarning(ParsedWorkbook parsed, String sheet, int rowNumber, String field, String message) {
        BulkIssue issue = new BulkIssue(sheet, rowNumber, field, message, BulkIssue.Severity.WARNING);
        parsed.issues.add(issue);
        parsed.warningCount++;
        return issue;
    }

    private List<BulkIssue> warnings(List<BulkIssue> issues) {
        return issues.stream().filter(i -> !i.isError()).toList();
    }

    private String uploadedBy(BulkWorkbookRequest request) {
        return request.uploadedBy() == null || request.uploadedBy().isBlank()
                ? "Admin" : request.uploadedBy().trim();
    }

    // ---------- String / collection helpers ----------

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private String fmt(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private String str(Enum<?> value) {
        return value == null ? null : value.name();
    }

    // ---------- Internal models ----------

    private record StudentLookup(Map<String, Student> byApp, Map<String, Student> byReg) {
    }

    private static class ParsedWorkbook {
        final List<BulkIssue> issues = new ArrayList<>();
        final List<PersonalApply> personal = new ArrayList<>();
        final List<ParentApply> parent = new ArrayList<>();
        final List<AddressApply> communication = new ArrayList<>();
        final List<AddressApply> permanent = new ArrayList<>();
        final List<AcademicApply> academic = new ArrayList<>();
        final List<QualifyingExamApply> qualifyingExam = new ArrayList<>();
        final List<HscMarksApply> hscMarks = new ArrayList<>();
        final List<DiplomaApply> diploma = new ArrayList<>();
        final List<PgApply> pg = new ArrayList<>();
        final Set<Long> matchedStudentIds = new LinkedHashSet<>();
        int totalRows;
        int unmatchedRows;
        int errorRows;
        int errorCount;
        int warningCount;

        List<Student> allStudents() {
            LinkedHashMap<Long, Student> byId = new LinkedHashMap<>();
            for (PersonalApply a : personal) {
                byId.putIfAbsent(a.student.getStudentId(), a.student);
            }
            for (ParentApply a : parent) {
                byId.putIfAbsent(a.student.getStudentId(), a.student);
            }
            for (AddressApply a : communication) {
                byId.putIfAbsent(a.student.getStudentId(), a.student);
            }
            for (AddressApply a : permanent) {
                byId.putIfAbsent(a.student.getStudentId(), a.student);
            }
            for (AcademicApply a : academic) {
                byId.putIfAbsent(a.student.getStudentId(), a.student);
            }
            for (QualifyingExamApply a : qualifyingExam) {
                byId.putIfAbsent(a.student.getStudentId(), a.student);
            }
            for (HscMarksApply a : hscMarks) {
                byId.putIfAbsent(a.student.getStudentId(), a.student);
            }
            for (DiplomaApply a : diploma) {
                byId.putIfAbsent(a.student.getStudentId(), a.student);
            }
            for (PgApply a : pg) {
                byId.putIfAbsent(a.student.getStudentId(), a.student);
            }
            return new ArrayList<>(byId.values());
        }
    }

    private record PersonalApply(Student student, int rowNumber,
                                 String registerNo, String applicationNo, String studentName,
                                 LocalDate dateOfBirth, Gender gender, String aadhaarNumber,
                                 String nationality, String district, Caste caste) {
    }

    private record ParentApply(Student student, int rowNumber,
                               String fatherName, String fatherMobile, String fatherOccupation,
                               BigDecimal annualIncome) {
    }

    private record AddressApply(Student student, int rowNumber, AddressType type,
                                String addressLine, String pincode, String phone, String mobile, String email) {
    }

    private record AcademicApply(Student student, int rowNumber,
                                 AdmissionCategory category, Program program, Department department,
                                 String batch, LocalDate dateOfAdmission) {
    }

    private record QualifyingExamApply(Student student, int rowNumber,
                                       String institutionName, String institutionPlace,
                                       String examPassed, String monthYearPassing,
                                       BigDecimal sslcPercentage, String sslcRegisterNumber,
                                       BigDecimal hscPercentage, String hscRegisterNumber) {
    }

    private record HscMarksApply(Student student, int rowNumber,
                                 List<HSCAcademicMark> academicMarks, List<HSCVocationalMark> vocationalMarks) {
    }

    private record DiplomaApply(Student student, int rowNumber,
                                String diploma, String institutionName, String board,
                                BigDecimal secondYearPercentage, BigDecimal thirdYearPercentage,
                                BigDecimal aggregatePercentage) {
    }

    private record PgApply(Student student, int rowNumber,
                           String universityName, String universityPlace,
                           String institutionName, String institutionPlace,
                           String examPassed, String monthYearPassing,
                           BigDecimal totalPercentage, BigDecimal mainSubjectPercentage,
                           String degreeRegistrationNumber) {
    }

    private record ApplyResult(int updatedStudents, int noChangeRows) {
    }
}
