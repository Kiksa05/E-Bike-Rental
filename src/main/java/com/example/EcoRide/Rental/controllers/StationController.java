package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Station;
import com.example.EcoRide.Rental.dao.StationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stations")
public class StationController {

    @Autowired
    private StationDao stationDao;

    @GetMapping("/{id}")
    public Station getStationById(@PathVariable int id) {
        return stationDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Station> getAllStations() {
        return stationDao.findAll();
    }

    @PostMapping
    public void saveStation(@RequestBody Station station) {
        stationDao.save(station);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Station> updateStation(@PathVariable int id, @RequestBody Station stationDetails) {
        Station station = stationDao.findById(id).orElse(null);
        if (station != null) {
            station.setLocation(stationDetails.getLocation());
            station.setAvailableBikes(stationDetails.getAvailableBikes());
            return new ResponseEntity<>(stationDao.save(station), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable int id) {
        Station station = stationDao.findById(id).orElse(null);
        if (station != null) {
            stationDao.delete(station);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
