package com.rgcet.admission.dto;

import jakarta.validation.Valid;

public record SubmitAdmissionRequest(
        Long studentId,
        @Valid PersonalStepRequest personal,
        @Valid ParentStepRequest parent,
        @Valid CommunicationStepRequest communication,
        @Valid AcademicStepRequest academic,
        @Valid QualifyingExamStepRequest qualifyingExam,
        @Valid HscMarksStepRequest hscMarks,
        @Valid DiplomaStepRequest diploma,
        @Valid PgStepRequest pg,
        @Valid FeeStepRequest fee,
        @Valid CertificatesStepRequest certificates
) {
}
