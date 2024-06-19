package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Rental;
import com.example.EcoRide.Rental.dao.RentalDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    @Autowired
    private RentalDao rentalDao;

    @GetMapping("/{id}")
    public Rental getRentalById(@PathVariable int id) {
        return rentalDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalDao.findAll();
    }

    @PostMapping
    public void saveRental(@RequestBody Rental rental) {
        rentalDao.save(rental);
    }

    @PutMapping
    public ResponseEntity<Rental> updateRental(@PathVariable int id,@RequestBody Rental rentalDetails) {
        Rental rental = rentalDao.findById(id).orElse(null);
        if (rental != null) {
        rental.setCustomer(rentalDetails.getCustomer());
        rental.setEbike(rentalDetails.getEbike());
        rental.setRentalStartTime(rentalDetails.getRentalStartTime());
        rental.setRentalEndTime(rentalDetails.getRentalEndTime());
        rental.setRentalStatus(rentalDetails.getRentalStatus());
        return new ResponseEntity<>(rentalDao.save(rental), HttpStatus.OK);
    }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRental(@PathVariable int id) {
        Rental rental = rentalDao.findById(id).orElse(null);
        if (rental != null) {
            rentalDao.delete(rental);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    //http://localhost:8080/rentals/search?customerId=3&ebikeModel=ModelX&startDate=2024-01-01T10:00:00&endDate=2024-01-01T12:00:00
    @GetMapping("/search")
    public List<Rental> findRentalsByCustomerEbikeAndDateRange(
            @RequestParam int customerId,
            @RequestParam String ebikeModel,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate) {
        return rentalDao.findRentalsByCustomerEbikeAndDate(customerId, ebikeModel, startDate, endDate);
    }
}
