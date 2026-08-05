package com.rgcet.admission.repository;

import com.rgcet.admission.entity.Admission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {
}
