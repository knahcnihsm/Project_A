package com.rgcet.admission.repository;

import com.rgcet.admission.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByDepartmentNameIgnoreCase(String name);
}
