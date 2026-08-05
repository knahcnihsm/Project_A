package com.rgcet.admission.repository;

import com.rgcet.admission.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByProgramNameIgnoreCase(String name);
}
