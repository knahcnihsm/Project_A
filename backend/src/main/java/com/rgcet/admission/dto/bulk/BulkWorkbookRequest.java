package com.rgcet.admission.dto.bulk;

import java.util.List;

/**
 * Typed representation of the parsed 10-sheet Bulk Student Update workbook.
 * Sheet 10 (Instructions) is documentation only and carries no data.
 */
public record BulkWorkbookRequest(
        String fileName,
        String uploadedBy,
        List<PersonalRow> personal,
        List<ParentRow> parent,
        List<CommunicationRow> communication,
        List<PermanentRow> permanent,
        List<AcademicRow> academic,
        List<QualifyingExamRow> qualifyingExam,
        List<HscMarksRow> hscMarks,
        List<DiplomaRow> diploma,
        List<PgRow> pg
) {

    public record PersonalRow(
            int rowNumber,
            String applicationNo,
            String registerNo,
            String studentName,
            String dateOfBirth,
            String gender,
            String aadhaarNumber,
            String nationality,
            String district,
            String caste
    ) {
    }

    public record ParentRow(
            int rowNumber,
            String applicationNo,
            String registerNo,
            String fatherName,
            String fatherMobile,
            String fatherOccupation,
            String annualIncome
    ) {
    }

    public record CommunicationRow(
            int rowNumber,
            String applicationNo,
            String registerNo,
            String addressLine,
            String pincode,
            String phone,
            String mobile,
            String email
    ) {
    }

    public record PermanentRow(
            int rowNumber,
            String applicationNo,
            String registerNo,
            String addressLine,
            String pincode,
            String phone,
            String mobile,
            String email
    ) {
    }

    public record AcademicRow(
            int rowNumber,
            String applicationNo,
            String registerNo,
            String admissionCategory,
            String program,
            String department,
            String batch,
            String dateOfAdmission
    ) {
    }

    public record QualifyingExamRow(
            int rowNumber,
            String applicationNo,
            String registerNo,
            String institutionName,
            String institutionPlace,
            String examPassed,
            String monthYearPassing,
            String sslcPercentage,
            String sslcRegisterNumber,
            String hscPercentage,
            String hscRegisterNumber
    ) {
    }

    public record HscMarksRow(
            int rowNumber,
            String applicationNo,
            String registerNo,
            String stream,
            List<SubjectRow> academicMarks,
            List<SubjectRow> vocationalMarks
    ) {
    }

    public record SubjectRow(
            String subject,
            String monthYear,
            String maxMarks,
            String marksObtained
    ) {
    }

    public record DiplomaRow(
            int rowNumber,
            String applicationNo,
            String registerNo,
            String diplomaCourse,
            String institutionName,
            String board,
            String secondYearPercentage,
            String thirdYearPercentage,
            String aggregatePercentage
    ) {
    }

    public record PgRow(
            int rowNumber,
            String applicationNo,
            String registerNo,
            String universityName,
            String universityPlace,
            String institutionName,
            String institutionPlace,
            String examPassed,
            String monthYearPassing,
            String totalPercentage,
            String mainSubjectPercentage,
            String degreeRegistrationNumber
    ) {
    }
}
