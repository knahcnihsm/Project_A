package com.rgcet.admission.repository;

import com.rgcet.admission.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByCertificateNameIgnoreCase(String certificateName);
}
