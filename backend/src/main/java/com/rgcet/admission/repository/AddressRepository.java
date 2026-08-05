package com.rgcet.admission.repository;

import com.rgcet.admission.entity.Address;
import com.rgcet.admission.entity.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByStudentStudentIdAndAddressType(Long studentId, AddressType type);
}
