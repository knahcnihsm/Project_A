package com.rgcet.admission.repository;

import com.rgcet.admission.entity.Student;
import com.rgcet.admission.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    Optional<Student> findByApplicationNoIgnoreCase(String applicationNo);

    Optional<Student> findByRegisterNoIgnoreCase(String registerNo);

    boolean existsByApplicationNoIgnoreCase(String applicationNo);

    long countByStatus(StudentStatus status);

    @Query("select s from Student s where lower(s.applicationNo) in :applicationNos")
    List<Student> findByApplicationNoInIgnoreCase(@Param("applicationNos") Collection<String> applicationNos);

    @Query("select s from Student s where lower(s.registerNo) in :registerNos")
    List<Student> findByRegisterNoInIgnoreCase(@Param("registerNos") Collection<String> registerNos);
}
