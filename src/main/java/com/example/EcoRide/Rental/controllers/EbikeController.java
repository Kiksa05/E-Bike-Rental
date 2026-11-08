package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Ebike;
import com.example.EcoRide.Rental.dao.EbikeDao;
import com.example.EcoRide.Rental.dao.RentalDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ebikes")
public class EbikeController {

    private final EbikeDao ebikeDao;

    private final RentalDao rentalDao;

    public EbikeController(EbikeDao ebikeDao, RentalDao rentalDao) {
        this.ebikeDao = ebikeDao;
        this.rentalDao = rentalDao;
    }

    @GetMapping("/{id}")
    public Ebike getEbikeById(@PathVariable int id) {
        return ebikeDao.findById(id).orElse(null);
    }

    // Only the times, never who booked them: anyone can open this to see when a bike is free.
    public record BookedSlot(LocalDateTime start, LocalDateTime end) {}

    @GetMapping("/{id}/booked")
    public List<BookedSlot> getBookedSlots(@PathVariable int id) {
        return rentalDao.findUpcomingBookings(id, LocalDateTime.now()).stream()
                .map(rental -> new BookedSlot(rental.getRentalStartTime(), rental.getRentalEndTime()))
                .toList();
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



    public record EbikeRequest(String model, double batteryLevel, String status, double price) {}

    @PostMapping
    public void saveEbike(@RequestBody EbikeRequest request) {
        ebikeDao.save(apply(request, new Ebike()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ebike> updateEbike(@PathVariable int id, @RequestBody EbikeRequest request) {
        Ebike ebike = ebikeDao.findById(id).orElse(null);
        if (ebike != null) {
            return new ResponseEntity<>(ebikeDao.save(apply(request, ebike)), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private Ebike apply(EbikeRequest request, Ebike ebike) {
        ebike.setModel(request.model());
        ebike.setBatteryLevel(request.batteryLevel());
        ebike.setStatus(request.status());
        ebike.setPrice(request.price());
        return ebike;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEbike(@PathVariable int id) {
        Ebike ebike = ebikeDao.findById(id).orElse(null);
        if (ebike != null) {
            ebikeDao.delete(ebike);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
