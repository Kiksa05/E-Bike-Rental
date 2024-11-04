package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.dao.EbikeDao;
import com.example.EcoRide.Rental.models.Customer;
import com.example.EcoRide.Rental.models.Ebike;
import com.example.EcoRide.Rental.models.Rental;
import com.example.EcoRide.Rental.dao.RentalDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


@RestController
@RequestMapping("/rentals")
@CrossOrigin
public class RentalController {

    private static final Logger logger = LoggerFactory.getLogger(RentalController.class);

    private List<Rental> pendingRentals = new ArrayList<>();

    @Autowired
    private RentalDao rentalDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private EbikeDao ebikeDao;

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

    @PostMapping("/pending")
    public ResponseEntity<String> createPendingReservation(@RequestBody Rental rental) {
        System.out.println("Received rental object: " + rental);

        if (rental.getCustomer() == null || rental.getCustomer().getId() == 0) {
            return new ResponseEntity<>("Customer ID is required", HttpStatus.BAD_REQUEST);
        }
        if (rental.getEbike() == null || rental.getEbike().getId() == 0) {
            return new ResponseEntity<>("Ebike ID is required", HttpStatus.BAD_REQUEST);
        }
        if (rental.getPassportNumber() == null || rental.getPassportNumber().isEmpty()) {
            return new ResponseEntity<>("Passport number is required", HttpStatus.BAD_REQUEST);
        }

        // Fetch the customer and ebike from the base
        Customer customer = customerDao.findById(rental.getCustomer().getId()).orElse(null);
        Ebike ebike = ebikeDao.findById(rental.getEbike().getId()).orElse(null);

        if (customer == null) {
            return new ResponseEntity<>("Customer not found", HttpStatus.BAD_REQUEST);
        }
        if (ebike == null) {
            return new ResponseEntity<>("Ebike not found", HttpStatus.BAD_REQUEST);
        }

        rental.setCustomer(customer);
        rental.setEbike(ebike);
        rental.setRentalStatus("Pending");
        rentalDao.save(rental);
        return new ResponseEntity<>("Reservation created successfully and is pending approval.", HttpStatus.CREATED);
    }

    @GetMapping("/pending")
    public List<Rental> getPendingRentals() {
        return rentalDao.findByRentalStatus("Pending");
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveRental(@PathVariable int id) {
        Rental rental = rentalDao.findById(id).orElse(null);
        if (rental == null) {
            return new ResponseEntity<>("Rental not found", HttpStatus.NOT_FOUND);
        }

        Ebike ebike = rental.getEbike();
        if (ebike == null) {
            return new ResponseEntity<>("Ebike not found", HttpStatus.NOT_FOUND);
        }

        rental.setRentalStatus("Approved");
        ebike.setStatus("Not Available");
        rentalDao.save(rental);
        ebikeDao.save(ebike);
        return new ResponseEntity<>("Rental approved and ebike status updated to 'Not Available'", HttpStatus.OK);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<String> rejectRental(@PathVariable int id) {
        Rental rental = rentalDao.findById(id).orElse(null);
        if (rental == null) {
            return new ResponseEntity<>("Rental not found", HttpStatus.NOT_FOUND);
        }

        rental.setRentalStatus("Rejected");
        rentalDao.save(rental);
        return new ResponseEntity<>("Rental rejected", HttpStatus.OK);
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
        rental.setPassportNumber(rentalDetails.getPassportNumber());
        return new ResponseEntity<>(rentalDao.save(rental), HttpStatus.OK);
    }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
}

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> deleteRental(@PathVariable int id) {
        Rental rental = rentalDao.findById(id).orElse(null);
        if (rental != null) {
            Ebike ebike = rental.getEbike();
            ebike.setStatus("Available");
            ebikeDao.save(ebike); // Update the bike status to "Available"
            rentalDao.delete(rental); // Delete the rental
            return ResponseEntity.ok("Rental deleted and bike status updated");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Rental not found");
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
