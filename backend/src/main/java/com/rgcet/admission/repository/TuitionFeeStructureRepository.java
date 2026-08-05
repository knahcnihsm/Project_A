package com.rgcet.admission.repository;

import com.rgcet.admission.entity.AdmissionCategory;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.Program;
import com.rgcet.admission.entity.TuitionFeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface TuitionFeeStructureRepository extends JpaRepository<TuitionFeeStructure, Long> {

    List<TuitionFeeStructure> findByProgramAndDepartmentAndCategory(Program program, Department department, AdmissionCategory category);

    List<TuitionFeeStructure> findByProgramAndDepartment(Program program, Department department);

    List<TuitionFeeStructure> findByProgram(Program program);
}
