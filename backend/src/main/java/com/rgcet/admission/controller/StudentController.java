package com.rgcet.admission.controller;

import com.rgcet.admission.dto.AcademicStepRequest;
import com.rgcet.admission.dto.ArchiveRequest;
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
import com.rgcet.admission.entity.StudentStatus;
import com.rgcet.admission.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponseDto> create(@Valid @RequestBody PersonalStepRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @PutMapping("/{id}/personal")
    public StudentResponseDto updatePersonal(@PathVariable Long id, @Valid @RequestBody PersonalStepRequest request) {
        return studentService.updatePersonal(id, request);
    }

    @PutMapping("/{id}/parent")
    public StudentResponseDto updateParent(@PathVariable Long id, @Valid @RequestBody ParentStepRequest request) {
        return studentService.updateParent(id, request);
    }

    @PutMapping("/{id}/communication")
    public StudentResponseDto updateCommunication(@PathVariable Long id, @Valid @RequestBody CommunicationStepRequest request) {
        return studentService.updateCommunication(id, request);
    }

    @PutMapping("/{id}/academic")
    public StudentResponseDto updateAcademic(@PathVariable Long id, @Valid @RequestBody AcademicStepRequest request) {
        return studentService.updateAcademic(id, request);
    }

    @PutMapping("/{id}/qualifying-exam")
    public StudentResponseDto updateQualifyingExam(@PathVariable Long id, @RequestBody QualifyingExamStepRequest request) {
        return studentService.updateQualifyingExam(id, request);
    }

    @PutMapping("/{id}/hsc-marks")
    public StudentResponseDto updateHscMarks(@PathVariable Long id, @Valid @RequestBody HscMarksStepRequest request) {
        return studentService.updateHscMarks(id, request);
    }

    @PutMapping("/{id}/diploma")
    public StudentResponseDto updateDiploma(@PathVariable Long id, @RequestBody DiplomaStepRequest request) {
        return studentService.updateDiploma(id, request);
    }

    @PutMapping("/{id}/pg")
    public StudentResponseDto updatePg(@PathVariable Long id, @RequestBody PgStepRequest request) {
        return studentService.updatePg(id, request);
    }

    @PutMapping("/{id}/fee")
    public StudentResponseDto updateFee(@PathVariable Long id, @RequestBody FeeStepRequest request) {
        return studentService.updateFee(id, request);
    }

    @PutMapping("/{id}/certificates")
    public StudentResponseDto updateCertificates(@PathVariable Long id, @Valid @RequestBody CertificatesStepRequest request) {
        return studentService.updateCertificates(id, request);
    }

    @PostMapping("/submit")
    public StudentResponseDto submitAdmission(@Valid @RequestBody SubmitAdmissionRequest request) {
        return studentService.submitAdmission(request);
    }

    @PostMapping("/{id}/finalize")
    public StudentResponseDto finalize(@PathVariable Long id) {
        return studentService.finalize(id);
    }

    @GetMapping("/{id}")
    public StudentResponseDto getStudent(@PathVariable Long id) {
        return studentService.getStudent(id);
    }

    @GetMapping
    public Page<StudentSummaryDto> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String batch,
            @RequestParam(required = false) StudentStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return studentService.search(search, departmentId, programId, categoryId, batch, status, pageable);
    }

    @GetMapping("/archived")
    public List<StudentSummaryDto> listArchived() {
        return studentService.listArchived();
    }

    @GetMapping("/list")
    public Page<StudentResponseDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String batch,
            @RequestParam(required = false) StudentStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return studentService.searchDetails(search, departmentId, programId, categoryId, batch, status, pageable);
    }

    @PostMapping("/{id}/archive")
    public StudentResponseDto archive(@PathVariable Long id, @Valid @RequestBody ArchiveRequest request) {
        return studentService.archive(id, request);
    }

    @PostMapping("/{id}/restore")
    public StudentResponseDto restore(@PathVariable Long id) {
        return studentService.restore(id);
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
                "active", studentService.countActive(),
                "archived", studentService.countArchived(),
                "draft", studentService.countDraft());
    }

    @PostMapping("/seed-fake-data")
    public List<StudentResponseDto> seedFakeData() {
        return studentService.seed10FakeStudents();
    }
}
