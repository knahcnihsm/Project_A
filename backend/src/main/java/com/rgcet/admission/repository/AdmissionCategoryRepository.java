package com.rgcet.admission.repository;

import com.rgcet.admission.entity.AdmissionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdmissionCategoryRepository extends JpaRepository<AdmissionCategory, Long> {

    Optional<AdmissionCategory> findByCategoryNameIgnoreCase(String name);
}
