package com.rgcet.admission.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migrates legacy enum values stored in student_details before the new, narrower
 * Gender/Caste enums (MALE/FEMALE/OTHERS, OBC/SC/ST/OTHERS) are read back by
 * Hibernate. EnumType.STRING calls Enum.valueOf() on read, so any row still
 * holding an old value (TRANSGENDER, OC, BC, BCM, MBC, SCA) would otherwise
 * throw on the first student query. Runs before the application serves traffic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LegacyEnumMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        int genderRows = jdbcTemplate.update(
                "UPDATE student_details SET gender = ? WHERE gender = ?", "OTHERS", "TRANSGENDER");
        int obcRows = jdbcTemplate.update(
                "UPDATE student_details SET caste = ? WHERE caste IN (?, ?, ?)", "OBC", "BC", "BCM", "MBC");
        int othersRows = jdbcTemplate.update(
                "UPDATE student_details SET caste = ? WHERE caste IN (?, ?)", "OTHERS", "OC", "SCA");
        if (genderRows + obcRows + othersRows > 0) {
            log.info("Legacy enum migration: gender={}, caste->OBC={}, caste->OTHERS={}",
                    genderRows, obcRows, othersRows);
        }
    }
}
