package com.rgcet.admission.service;

import com.rgcet.admission.entity.HSCAcademicMark;
import com.rgcet.admission.entity.HSCVocationalMark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class CutoffCalculator {

    public static final BigDecimal CUTOFF_MAX_SCORE = BigDecimal.valueOf(300);

    private static final List<String> SCIENCE_SUBJECTS =
            List.of("Chemistry", "Computer Science", "Biology", "Bio Technology", "Biotechnology");

    private CutoffCalculator() {
    }

    /**
     * Engineering Cut-Off = Maths + Physics + best of (Chemistry / Computer Science / Biology / Bio Technology).
     * Each subject is normalised to its percentage (marksObtained / maximumMarks * 100) before summing,
     * so subjects marked out of any maximum contribute a score out of 100. The maximum possible cutoff is 300.
     */
    public static BigDecimal engineeringCutOff(List<HSCAcademicMark> academicMarks) {
        if (academicMarks == null || academicMarks.isEmpty()) {
            return null;
        }
        BigDecimal maths = subjectPercentage(academicMarks, "Maths", "Mathematics");
        BigDecimal physics = subjectPercentage(academicMarks, "Physics");
        if (maths == null || physics == null) {
            return null;
        }
        BigDecimal bestScience = SCIENCE_SUBJECTS.stream()
                .map(s -> subjectPercentage(academicMarks, s))
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(null);
        if (bestScience == null) {
            return null;
        }
        return maths.add(physics).add(bestScience);
    }

    /** Merit percentage used for fee slab lookup: engineeringCutOff / 300 * 100. */
    public static BigDecimal meritPercent(BigDecimal engineeringCutOff) {
        if (engineeringCutOff == null || CUTOFF_MAX_SCORE.signum() == 0) {
            return null;
        }
        return engineeringCutOff.multiply(BigDecimal.valueOf(100))
                .divide(CUTOFF_MAX_SCORE, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal subjectPercentage(BigDecimal obtained, BigDecimal maximum) {
        if (obtained == null || maximum == null || maximum.signum() == 0) {
            return null;
        }
        return obtained.multiply(BigDecimal.valueOf(100))
                .divide(maximum, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal overallPercentage(List<HSCAcademicMark> academic,
                                               List<HSCVocationalMark> vocational) {
        BigDecimal obtained = totalMarksObtained(academic, vocational);
        BigDecimal maximum = totalMaxMarks(academic, vocational);
        return subjectPercentage(obtained, maximum);
    }

    public static BigDecimal totalMaxMarks(List<HSCAcademicMark> academic,
                                           List<HSCVocationalMark> vocational) {
        BigDecimal total = BigDecimal.ZERO;
        boolean any = false;
        if (academic != null) {
            for (HSCAcademicMark m : academic) {
                if (m.getMaximumMarks() != null) {
                    total = total.add(m.getMaximumMarks());
                    any = true;
                }
            }
        }
        if (vocational != null) {
            for (HSCVocationalMark m : vocational) {
                if (m.getMaximumMarks() != null) {
                    total = total.add(m.getMaximumMarks());
                    any = true;
                }
            }
        }
        return any ? total : null;
    }

    public static BigDecimal totalMarksObtained(List<HSCAcademicMark> academic,
                                                List<HSCVocationalMark> vocational) {
        BigDecimal total = BigDecimal.ZERO;
        boolean any = false;
        if (academic != null) {
            for (HSCAcademicMark m : academic) {
                if (m.getMarksObtained() != null) {
                    total = total.add(m.getMarksObtained());
                    any = true;
                }
            }
        }
        if (vocational != null) {
            for (HSCVocationalMark m : vocational) {
                if (m.getMarksObtained() != null) {
                    total = total.add(m.getMarksObtained());
                    any = true;
                }
            }
        }
        return any ? total : null;
    }

    private static BigDecimal subjectPercentage(List<HSCAcademicMark> academicMarks, String... names) {
        for (HSCAcademicMark mark : academicMarks) {
            if (mark.getSubjectName() == null) {
                continue;
            }
            for (String name : names) {
                if (mark.getSubjectName().equalsIgnoreCase(name)) {
                    return subjectPercentage(mark.getMarksObtained(), mark.getMaximumMarks());
                }
            }
        }
        return null;
    }
}
