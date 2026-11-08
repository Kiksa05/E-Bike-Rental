package com.example.EcoRide.Rental.dao;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import com.example.EcoRide.Rental.models.Ebike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface EbikeDao extends JpaRepository<Ebike, Integer> {
    List<Ebike> findByModelAndStatus(String model, String status);
    List<Ebike> findByModelAndStatusAndBatteryLevelGreaterThan(String model, String status, double batteryLevel);
    List<Ebike> findByModelAndStatusAndStationId(String model, String status, int stationId);

    // Locks the bike's row until the transaction ends, so two bookings for it cannot be checked at the same time.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Ebike e WHERE e.id = :id")
    Optional<Ebike> findByIdForUpdate(@Param("id") int id);
}