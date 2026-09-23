package com.anpr.accesscontrol.repository;

import com.anpr.accesscontrol.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Spring Data JPA automatski generise SQL na osnovu imena metode -
    // ne treba pisati nijednu liniju upita rucno za ovako jednostavne slucajeve.
    Optional<Vehicle> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);
}
