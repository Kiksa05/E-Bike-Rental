package com.example.EcoRide.Rental.dao;

import com.example.EcoRide.Rental.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface StationDao extends JpaRepository<Station, Integer> {
//    Station getStationById(int id);
//    List<Station> getAllStations();
//    void saveStation(Station station);
//    void updateStation(Station station);
//    void deleteStation(int id);
}