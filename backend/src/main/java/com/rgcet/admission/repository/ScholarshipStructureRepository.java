package com.rgcet.admission.repository;

import com.rgcet.admission.entity.AdmissionCategory;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.Program;
import com.rgcet.admission.entity.ScholarshipStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScholarshipStructureRepository extends JpaRepository<ScholarshipStructure, Long> {

    List<ScholarshipStructure> findByProgramAndDepartmentAndCategory(Program program, Department department, AdmissionCategory category);
}
