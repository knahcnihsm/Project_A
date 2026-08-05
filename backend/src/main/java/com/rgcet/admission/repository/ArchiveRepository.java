package com.rgcet.admission.repository;

import com.rgcet.admission.entity.Archive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {

    Optional<Archive> findByStudentStudentId(Long studentId);

    void deleteByStudentStudentId(Long studentId);
}
