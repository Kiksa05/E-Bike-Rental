package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Customer;
import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.service.Email;
import jakarta.mail.MessagingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private static final Duration TOKEN_LIFETIME = Duration.ofHours(2);

    private final CustomerDao customerDao;

    private final Email emailService;

    private final JwtEncoder jwtEncoder;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CustomerController(CustomerDao customerDao, Email emailService, JwtEncoder jwtEncoder) {
        this.customerDao = customerDao;
        this.emailService = emailService;
        this.jwtEncoder = jwtEncoder;
    }

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable int id) {
        return customerDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerDao.findAll();
    }

    public record CustomerUpdateRequest(String name, String email, String phone, String password) {}

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable int id, @RequestBody CustomerUpdateRequest request) {
        Customer customer = customerDao.findById(id).orElse(null);
        if (customer != null) {
            String email = normalizeEmail(request.email());
            if (email == null || email.isBlank()) {
                return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
            }
            if (customerDao.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
            }

            customer.setName(request.name());
            customer.setEmail(email);
            customer.setPhone(request.phone());

            if (request.password() != null && !request.password().isBlank()) {
                customer.setPassword(passwordEncoder.encode(request.password()));
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

    public record RegisterRequest(String name, String email, String password, String phone) {}

    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@RequestBody RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (email == null || email.isBlank()) {
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        }
        if (request.password() == null || request.password().isBlank()) {
            return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
        }

        // Check if email already exists
        if (customerDao.existsByEmailIgnoreCase(email)) {
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }

        Customer customer = new Customer(request.name(), request.phone());
        customer.setEmail(email);
        // New accounts are never admins; admins are set directly in the database.
        customer.setRole("user");
        customer.setPassword(passwordEncoder.encode(request.password()));

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

    public record LoginRequest(String email, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest request) {
        // Find customer by email
        if (request.password() == null || request.password().isBlank()) {
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        Customer existingCustomer = customerDao.findByEmailIgnoreCase(normalizeEmail(request.email()));
        if (existingCustomer == null
                || !passwordEncoder.matches(request.password(), existingCustomer.getPassword())) {
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String role = existingCustomer.getRole() == null ? "user" : existingCustomer.getRole();

        Map<String, String> response = new HashMap<>();
        response.put("token", createToken(existingCustomer.getId(), role));
        response.put("userId", String.valueOf(existingCustomer.getId()));
        response.put("userRole", role);
        response.put("userName", existingCustomer.getName());

        return ResponseEntity.ok(response);
    }

    // shortcut: tokens stay valid until they expire even after logout, add a revocation list if that becomes a problem.
    private String createToken(int customerId, String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(customerId))
                .claim("role", role)
                .issuedAt(now)
                .expiresAt(now.plus(TOKEN_LIFETIME))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
