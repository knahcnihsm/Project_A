package com.rgcet.admission.dto;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

public record HscMarksStepRequest(
        String stream,
        @Valid List<SubjectMark> academicMarks,
        @Valid List<SubjectMark> vocationalMarks
) {
    public record SubjectMark(
            String subject,
            String monthYear,
            BigDecimal maxMarks,
            BigDecimal marksObtained
    ) {
    }
}
