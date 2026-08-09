package com.rgcet.admission.repository;

import com.rgcet.admission.entity.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {

    Optional<BusRoute> findByRouteNameIgnoreCase(String routeName);
}
