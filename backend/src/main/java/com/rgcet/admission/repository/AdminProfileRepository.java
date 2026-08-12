package com.rgcet.admission.repository;

import com.rgcet.admission.entity.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {
    Optional<AdminProfile> findByUsername(String username);
    Optional<AdminProfile> findTopByOrderByIdAsc();
}
