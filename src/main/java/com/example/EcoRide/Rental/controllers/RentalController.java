package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.dao.EbikeDao;
import com.example.EcoRide.Rental.models.Customer;
import com.example.EcoRide.Rental.models.Ebike;
import com.example.EcoRide.Rental.models.Rental;
import com.example.EcoRide.Rental.dao.RentalDao;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


@RestController
@RequestMapping("/rentals")
public class RentalController {

    private static final Logger logger = LoggerFactory.getLogger(RentalController.class);

    private List<Rental> pendingRentals = new ArrayList<>();

    private final RentalDao rentalDao;

    private final CustomerDao customerDao;

    private final EbikeDao ebikeDao;

    public RentalController(RentalDao rentalDao, CustomerDao customerDao, EbikeDao ebikeDao) {
        this.rentalDao = rentalDao;
        this.customerDao = customerDao;
        this.ebikeDao = ebikeDao;
    }

    @GetMapping("/{id}")
    public Rental getRentalById(@PathVariable int id) {
        return rentalDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalDao.findAll(Sort.by(Sort.Direction.DESC, "rentalStartTime"));
    }

    @GetMapping("/mine")
    public List<Rental> getMyRentals(Authentication authentication) {
        return rentalDao.findByCustomerIdOrderByRentalStartTimeDesc(Integer.parseInt(authentication.getName()));
    }

    public record IdRef(int id) {}

    public record ReservationRequest(IdRef ebike, LocalDateTime rentalStartTime,
                                     LocalDateTime rentalEndTime, String passportNumber) {}

    @PostMapping("/pending")
    // READ_COMMITTED: once the bike lock is ours, the overlap check must see bookings committed while we waited.
    // MySQL's default (REPEATABLE_READ) would keep showing the data as it was before the wait.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<String> createPendingReservation(@RequestBody ReservationRequest request,
                                                           Authentication authentication) {
        if (request.ebike() == null || request.ebike().id() == 0) {
            return new ResponseEntity<>("Ebike ID is required", HttpStatus.BAD_REQUEST);
        }
        if (request.passportNumber() == null || request.passportNumber().isEmpty()) {
            return new ResponseEntity<>("Passport number is required", HttpStatus.BAD_REQUEST);
        }

        if (request.rentalStartTime() == null || request.rentalEndTime() == null
                || !request.rentalEndTime().isAfter(request.rentalStartTime())) {
            return new ResponseEntity<>("End time must be after the start time", HttpStatus.BAD_REQUEST);
        }
        if (!onTheHour(request.rentalStartTime()) || !onTheHour(request.rentalEndTime())) {
            return new ResponseEntity<>("Reservations start and end on the hour, like 13:00", HttpStatus.BAD_REQUEST);
        }
        // The current hour is still bookable, so you can pick a bike up right away.
        if (request.rentalStartTime().isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS))) {
            return new ResponseEntity<>("The start time is in the past", HttpStatus.BAD_REQUEST);
        }

        // The customer comes from the signed token, so nobody can reserve as someone else.
        Customer customer = customerDao.findById(Integer.parseInt(authentication.getName())).orElse(null);
        Ebike ebike = ebikeDao.findByIdForUpdate(request.ebike().id()).orElse(null);

        if (customer == null) {
            return new ResponseEntity<>("Customer not found", HttpStatus.BAD_REQUEST);
        }
        if (ebike == null) {
            return new ResponseEntity<>("Ebike not found", HttpStatus.BAD_REQUEST);
        }
        if ("Not Available".equals(ebike.getStatus())) {
            // Set by an admin, e.g. while the bike is being repaired.
            return new ResponseEntity<>("This bike is not available right now", HttpStatus.CONFLICT);
        }
        if (rentalDao.existsOverlap(ebike.getId(), request.rentalStartTime(), request.rentalEndTime(), 0)) {
            return new ResponseEntity<>("This bike is already booked for that time. Pick another time or bike.",
                    HttpStatus.CONFLICT);
        }

        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setEbike(ebike);
        rental.setRentalStartTime(request.rentalStartTime());
        rental.setRentalEndTime(request.rentalEndTime());
        rental.setPassportNumber(request.passportNumber());
        rental.setRentalStatus("Pending");
        rentalDao.save(rental);
        return new ResponseEntity<>("Reservation created successfully and is pending approval.", HttpStatus.CREATED);
    }

    private static boolean onTheHour(LocalDateTime time) {
        return time.equals(time.truncatedTo(ChronoUnit.HOURS));
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
        if (rentalDao.existsApprovedOverlap(ebike.getId(), rental.getRentalStartTime(), rental.getRentalEndTime(), id)) {
            return new ResponseEntity<>("Another approved reservation already has this bike at that time",
                    HttpStatus.CONFLICT);
        }

        // The bike stays bookable for other times; the overlap check blocks only this time slot.
        rental.setRentalStatus("Approved");
        rentalDao.save(rental);
        return new ResponseEntity<>("Rental approved", HttpStatus.OK);
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


    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> deleteRental(@PathVariable int id) {
        Rental rental = rentalDao.findById(id).orElse(null);
        if (rental != null) {
            rentalDao.delete(rental);
            return ResponseEntity.ok("Rental deleted");
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
