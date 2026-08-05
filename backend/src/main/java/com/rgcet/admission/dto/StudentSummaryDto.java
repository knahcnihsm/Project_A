package com.rgcet.admission.dto;

import com.rgcet.admission.entity.Student;
import com.rgcet.admission.entity.StudentStatus;

import java.time.LocalDateTime;

public record StudentSummaryDto(
        Long id,
        String applicationNumber,
        String registerNumber,
        String studentName,
        String program,
        String department,
        String category,
        String batch,
        StudentStatus status,
        LocalDateTime createdAt,
        LocalDateTime archivedAt,
        String archiveReason
) {
    public static StudentSummaryDto from(Student s) {
        return new StudentSummaryDto(
                s.getStudentId(),
                s.getApplicationNo(),
                s.getRegisterNo(),
                s.getStudentName(),
                s.getAdmission() == null || s.getAdmission().getProgram() == null ? null : s.getAdmission().getProgram().getProgramName(),
                s.getAdmission() == null || s.getAdmission().getDepartment() == null ? null : s.getAdmission().getDepartment().getDepartmentName(),
                s.getAdmission() == null || s.getAdmission().getCategory() == null ? null : s.getAdmission().getCategory().getCategoryName(),
                s.getAdmission() == null ? null : s.getAdmission().getBatch(),
                s.getStatus(),
                s.getCreatedAt(),
                s.getArchivedAt(),
                s.getArchiveReason());
    }
}
