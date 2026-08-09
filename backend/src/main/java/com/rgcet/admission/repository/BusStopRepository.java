package com.rgcet.admission.repository;

import com.rgcet.admission.entity.BusStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusStopRepository extends JpaRepository<BusStop, Long> {

    Optional<BusStop> findByStopNameIgnoreCase(String stopName);

    Optional<BusStop> findByStopNameIgnoreCaseAndRouteRouteNameIgnoreCase(String stopName, String routeName);

    List<BusStop> findByStopNameIgnoreCaseAndRouteRouteName(String stopName, String routeName);
}
