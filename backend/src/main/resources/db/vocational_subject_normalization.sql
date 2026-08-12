-- One-time migration: normalise vocational HSC marks to the canonical structure.
-- Related Subject II carries Theory + Practical I + Practical II sub-rows.
-- Run once against PostgreSQL (erp_admission). Idempotent: re-running is harmless.

-- 1. Relabel legacy flat practical / theory rows to canonical PRACTICAL I and PRACTICAL II.
UPDATE hsc_vocational_marks
SET subject_name = 'PRACTICAL I'
WHERE subject_name IN ('RELATED SUBJECT II PRACTICAL I', 'RELATED SUBJECT II PRACTICAL');

UPDATE hsc_vocational_marks
SET subject_name = 'PRACTICAL II'
WHERE subject_name IN ('RELATED SUBJECT II PRACTICAL II');

-- A standalone THEORY row is the theory component of Related Subject II.
UPDATE hsc_vocational_marks
SET subject_name = 'RELATED SUBJECT II'
WHERE subject_name IN ('THEORY', 'RELATED SUBJECT II THEORY');

-- 2. Ensure every vocational student has the canonical 5 rows.
--    Existing main rows are preserved; missing practical rows are added with 0 marks.
INSERT INTO hsc_vocational_marks (qualification_id, subject_name, month_year, maximum_marks, marks_obtained, percentage)
SELECT q.qualification_id, s.subject_name, NULL, 100, 0, 0
FROM qualifying_examination q
CROSS JOIN (VALUES
    ('RELATED SUBJECT II'),
    ('PRACTICAL I'),
    ('PRACTICAL II')
) AS s(subject_name)
WHERE EXISTS (
    SELECT 1 FROM hsc_vocational_marks v
    WHERE v.qualification_id = q.qualification_id
      AND v.subject_name = 'VOCATIONAL SUBJECT THEORY'
)
AND NOT EXISTS (
    SELECT 1 FROM hsc_vocational_marks v
    WHERE v.qualification_id = q.qualification_id
      AND v.subject_name = s.subject_name
);
