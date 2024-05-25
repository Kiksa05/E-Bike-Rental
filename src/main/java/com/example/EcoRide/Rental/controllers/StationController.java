package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Station;
import com.example.EcoRide.Rental.dao.StationDao;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PutMapping
    public void updateStation(@RequestBody Station station) {
        stationDao.save(station);
    }

    @DeleteMapping("/{id}")
    public void deleteStation(@PathVariable int id) {
        stationDao.deleteById(id);
    }
}
