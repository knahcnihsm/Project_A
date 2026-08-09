package com.rgcet.admission.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LegacyEnumMigrationRunnerTest {

    @Test
    void runsAllLegacyEnumUpdateStatements() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(
                "UPDATE student_details SET gender = ? WHERE gender = ?", "OTHERS", "TRANSGENDER")).thenReturn(0);
        when(jdbcTemplate.update(
                "UPDATE student_details SET caste = ? WHERE caste IN (?, ?, ?)", "OBC", "BC", "BCM", "MBC")).thenReturn(0);
        when(jdbcTemplate.update(
                "UPDATE student_details SET caste = ? WHERE caste IN (?, ?)", "OTHERS", "OC", "SCA")).thenReturn(0);

        LegacyEnumMigrationRunner runner = new LegacyEnumMigrationRunner(jdbcTemplate);
        runner.run(null);

        verify(jdbcTemplate).execute("ALTER TABLE student_details DROP CONSTRAINT IF EXISTS student_details_gender_check");
        verify(jdbcTemplate).execute("ALTER TABLE student_details DROP CONSTRAINT IF EXISTS student_details_caste_check");
        verify(jdbcTemplate).update("UPDATE student_details SET gender = ? WHERE gender = ?", "OTHERS", "TRANSGENDER");
        verify(jdbcTemplate).update("UPDATE student_details SET caste = ? WHERE caste IN (?, ?, ?)", "OBC", "BC", "BCM", "MBC");
        verify(jdbcTemplate).update("UPDATE student_details SET caste = ? WHERE caste IN (?, ?)", "OTHERS", "OC", "SCA");
    }
}
