package com.rgcet.admission.repository;

import com.rgcet.admission.entity.StudentCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentCertificateRepository extends JpaRepository<StudentCertificate, Long> {
}
