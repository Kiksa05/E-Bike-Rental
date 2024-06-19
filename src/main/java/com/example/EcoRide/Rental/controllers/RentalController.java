package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Rental;
import com.example.EcoRide.Rental.dao.RentalDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void updateRental(@PathVariable int id,@RequestBody Rental rentalDetails) {
        Rental rental = rentalDao.findById(id).orElse(null);
        rental.setCustomerId(rentalDetails.getCustomerId());
        rental.setEbikeId(rentalDetails.getEbikeId());
        rental.setRentalStartTime(rentalDetails.getRentalStartTime());
        rental.setRentalEndTime(rentalDetails.getRentalEndTime());
        rentalDao.save(rental);
    }

    @DeleteMapping("/{id}")
    public void deleteRental(@PathVariable int id) {
        Rental rental = rentalDao.findById(id).orElse(null);
        rentalDao.delete(rental);
    }
}
