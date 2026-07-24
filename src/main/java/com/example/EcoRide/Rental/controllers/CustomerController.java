package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Customer;
import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.service.Email;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
@CrossOrigin
public class CustomerController {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private Email emailService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable int id) {
        return customerDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerDao.findAll();
    }

    @PostMapping
    public ResponseEntity<?> saveCustomer(@RequestBody Customer customer) {
        String email = normalizeEmail(customer.getEmail());
        if (email == null || email.isBlank()) {
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        }
        if (customer.getPassword() == null || customer.getPassword().isBlank()) {
            return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
        }
        if (customerDao.existsByEmailIgnoreCase(email)) {
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }

        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        try {
            return new ResponseEntity<>(customerDao.save(customer), HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable int id, @RequestBody Customer customerDetails) {
        Customer customer = customerDao.findById(id).orElse(null);
        if (customer != null) {
            String email = normalizeEmail(customerDetails.getEmail());
            if (email == null || email.isBlank()) {
                return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
            }
            if (customerDao.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
            }

            customer.setName(customerDetails.getName());
            customer.setEmail(email);
            customer.setPhone(customerDetails.getPhone());
            customer.setAccountBalance(customerDetails.getAccountBalance());

            if (customerDetails.getPassword() != null && !customerDetails.getPassword().isBlank()) {
                customer.setPassword(passwordEncoder.encode(customerDetails.getPassword()));
            }

            try {
                return new ResponseEntity<>(customerDao.save(customer), HttpStatus.OK);
            } catch (DataIntegrityViolationException e) {
                return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable int id) {
        Customer customer = customerDao.findById(id).orElse(null);
        if (customer != null) {
            customerDao.delete(customer);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@RequestBody Customer customer) {
        String email = normalizeEmail(customer.getEmail());
        if (email == null || email.isBlank()) {
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        }
        if (customer.getPassword() == null || customer.getPassword().isBlank()) {
            return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
        }

        String role = email.equalsIgnoreCase("kiksa@admin.com") ? "admin" : "user";
        // customer.setRole(role);
        // Check if email already exists
        if (customerDao.existsByEmailIgnoreCase(email)) {
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }

        customer.setEmail(email);
        customer.setRole(role);
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        try {
            customerDao.save(customer);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }

        // Send registration email with PDF attachment
        // try {
        // emailService.sendRegistrationEmail(customer.getEmail(), customer.getName());
        // } catch (MessagingException | IOException e) {
        // e.printStackTrace();
        // return ResponseEntity.status(500).body("Failed to send email");
        // }

        return new ResponseEntity<>("Registration successful", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody Customer customer) {
        // Find customer by email
        if (customer.getPassword() == null || customer.getPassword().isBlank()) {
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        Customer existingCustomer = customerDao.findByEmailIgnoreCase(normalizeEmail(customer.getEmail()));
        if (existingCustomer == null
                || !passwordEncoder.matches(customer.getPassword(), existingCustomer.getPassword())) {
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        Map<String, String> response = new HashMap<>();
        response.put("userId", String.valueOf(existingCustomer.getId()));
        response.put("userRole", existingCustomer.getRole());
        response.put("userName", existingCustomer.getName());

        return ResponseEntity.ok(response);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
