package com.rgcet.admission;

import com.rgcet.admission.entity.HSCAcademicMark;
import com.rgcet.admission.service.CutoffCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CutoffCalculatorTest {

    private HSCAcademicMark mark(String subject, int obtained, int max) {
        HSCAcademicMark m = new HSCAcademicMark();
        m.setSubjectName(subject);
        m.setMarksObtained(BigDecimal.valueOf(obtained));
        m.setMaximumMarks(BigDecimal.valueOf(max));
        return m;
    }

    @Test
    void cutoffIsMathsPlusPhysicsPlusBestScience() {
        List<HSCAcademicMark> marks = List.of(
                mark("Maths", 95, 100),
                mark("Physics", 90, 100),
                mark("Chemistry", 85, 100),
                mark("Computer Science", 99, 100),
                mark("Biology", 80, 100),
                mark("Bio Technology", 88, 100));

        BigDecimal cutoff = CutoffCalculator.engineeringCutOff(marks);
        // 95 + 90 + max(85, 99, 80, 88) = 95 + 90 + 99 = 284
        assertEquals(0, cutoff.compareTo(BigDecimal.valueOf(284)));
    }

    @Test
    void cutoffUsesHighestScienceSubject() {
        List<HSCAcademicMark> marks = List.of(
                mark("Maths", 70, 100),
                mark("Physics", 60, 100),
                mark("Biology", 95, 100),
                mark("Chemistry", 55, 100));

        BigDecimal cutoff = CutoffCalculator.engineeringCutOff(marks);
        // 70 + 60 + 95 = 225
        assertEquals(0, cutoff.compareTo(BigDecimal.valueOf(225)));
    }

    @Test
    void cutoffNullWhenMathsOrPhysicsMissing() {
        assertNull(CutoffCalculator.engineeringCutOff(List.of(mark("Maths", 80, 100))));
        assertNull(CutoffCalculator.engineeringCutOff(null));
        assertNull(CutoffCalculator.engineeringCutOff(List.of()));
    }

    @Test
    void cutoffNormalisesEachSubjectToPercentage() {
        List<HSCAcademicMark> marks = List.of(
                mark("Maths", 190, 200),
                mark("Physics", 180, 200),
                mark("Computer Science", 170, 200));

        BigDecimal cutoff = CutoffCalculator.engineeringCutOff(marks);
        // 190/200=95 + 180/200=90 + 170/200=85 = 270 (not the raw sum 540)
        assertEquals(0, cutoff.compareTo(BigDecimal.valueOf(270)));
    }

    @Test
    void meritPercentIsCutoffOverThreeHundred() {
        assertEquals(0, CutoffCalculator.meritPercent(BigDecimal.valueOf(300)).compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, CutoffCalculator.meritPercent(BigDecimal.valueOf(150)).compareTo(BigDecimal.valueOf(50)));
        assertNull(CutoffCalculator.meritPercent(null));
    }

    @Test
    void overallPercentageFromAggregateMarks() {
        List<HSCAcademicMark> marks = List.of(
                mark("Maths", 80, 100),
                mark("Physics", 90, 100),
                mark("Chemistry", 70, 100));
        BigDecimal overall = CutoffCalculator.overallPercentage(marks, null);
        // (80 + 90 + 70) / 300 * 100 = 80.00
        assertEquals(0, overall.compareTo(new BigDecimal("80.00")));
    }
}
