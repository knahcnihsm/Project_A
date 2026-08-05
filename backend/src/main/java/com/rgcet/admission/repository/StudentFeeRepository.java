package com.rgcet.admission.repository;

import com.rgcet.admission.entity.StudentFee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentFeeRepository extends JpaRepository<StudentFee, Long> {
}
