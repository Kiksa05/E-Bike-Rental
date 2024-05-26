package com.example.EcoRide.Rental.dao;

import com.example.EcoRide.Rental.models.Ebike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
//soujbbijknbjlkkkkbhjhvjvhjvh

@Repository
public interface EbikeDao extends JpaRepository<Ebike, Integer> {
    List<Ebike> findByModelAndStatus(String model, String status);
    List<Ebike> findByModelAndStatusAndBatteryLevelGreaterThan(String model, String status, double batteryLevel);
    List<Ebike> findByModelAndStatusAndStationId(String model, String status, int stationId);
}