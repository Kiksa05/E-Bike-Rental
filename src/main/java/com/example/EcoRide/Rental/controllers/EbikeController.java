package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Ebike;
import com.example.EcoRide.Rental.dao.EbikeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ebikes")
public class EbikeController {

    @Autowired
    private EbikeDao ebikeDao;

    @GetMapping("/{id}")
    public Ebike getEbikeById(@PathVariable int id) {
        return ebikeDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Ebike> getAllEbikes() {
        return ebikeDao.findAll();
    }

//    /ebikes/searchByModelAndStatus?model=ModelX&status=available
    @GetMapping("/searchByModelAndStatus")
    public List<Ebike> getEbikesByModelAndStatus(@RequestParam String model, @RequestParam String status) {
        return ebikeDao.findByModelAndStatus(model, status);
    }

//    /ebikes/searchByModelStatusAndBattery?model=ModelX&status=available&batteryLevel=50
    @GetMapping("/searchByModelStatusAndBattery")
    public List<Ebike> getEbikesByModelStatusAndBatteryLevel(@RequestParam String model, @RequestParam String status, @RequestParam double batteryLevel) {
        return ebikeDao.findByModelAndStatusAndBatteryLevelGreaterThan(model, status, batteryLevel);
    }

//    /ebikes/searchByModelStatusAndStation?model=ModelX&status=available&stationId=1
    @GetMapping("/searchByModelStatusAndStation")
    public List<Ebike> getEbikesByModelStatusAndStation(@RequestParam String model, @RequestParam String status, @RequestParam int stationId) {
        return ebikeDao.findByModelAndStatusAndStationId(model, status, stationId);
    }






    @PostMapping
    public void saveEbike(@RequestBody Ebike ebike) {
        ebikeDao.save(ebike);
    }

    @PutMapping("/{id}")
    public Ebike updateEbike(@PathVariable int id, @RequestBody Ebike ebikeDetails) {
        Ebike ebike = ebikeDao.findById(id).orElse(null);
        if (ebike != null) {
            ebike.setModel(ebikeDetails.getModel());
            ebike.setBatteryLevel(ebikeDetails.getBatteryLevel());
            ebike.setStatus(ebikeDetails.getStatus());
            ebike.setStation(ebikeDetails.getStation());
            return ebikeDao.save(ebike);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteEbike(@PathVariable int id) {
        ebikeDao.deleteById(id);
    }
}
