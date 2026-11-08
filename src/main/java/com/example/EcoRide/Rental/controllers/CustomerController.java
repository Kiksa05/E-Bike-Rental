package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Customer;
import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.dao.RentalDao;
import com.example.EcoRide.Rental.models.Rental;
import com.example.EcoRide.Rental.service.Email;
import jakarta.mail.MessagingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private static final Duration TOKEN_LIFETIME = Duration.ofHours(2);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final CustomerDao customerDao;

    private final Email emailService;

    private final JwtEncoder jwtEncoder;

    private final RentalDao rentalDao;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CustomerController(CustomerDao customerDao, Email emailService, JwtEncoder jwtEncoder,
                              RentalDao rentalDao) {
        this.customerDao = customerDao;
        this.emailService = emailService;
        this.jwtEncoder = jwtEncoder;
        this.rentalDao = rentalDao;
    }

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable int id) {
        return customerDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerDao.findAll();
    }

    public record CustomerUpdateRequest(String name, String email, String phone, String passportNumber) {}

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable int id, @RequestBody CustomerUpdateRequest request) {
        Customer customer = customerDao.findById(id).orElse(null);
        if (customer == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
        customer.setPassportNumber(request.passportNumber());

        try {
            return new ResponseEntity<>(customerDao.save(customer), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }
    }

    public record PasswordChangeRequest(String currentPassword, String newPassword) {}

    @PutMapping("/{id}/password")
    public ResponseEntity<String> changePassword(@PathVariable int id, @RequestBody PasswordChangeRequest request,
                                                 Authentication caller) {
        Customer customer = customerDao.findById(id).orElse(null);
        if (customer == null) {
            return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
        }
        // Your own password needs the current one, so someone at your unlocked screen cannot take the account.
        // Admins resetting another user's password skip it.
        if (isSelf(caller, id) && (request.currentPassword() == null
                || !passwordEncoder.matches(request.currentPassword(), customer.getPassword()))) {
            return new ResponseEntity<>("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        if (request.newPassword() == null || request.newPassword().length() < MIN_PASSWORD_LENGTH) {
            return new ResponseEntity<>("New password must be at least 8 characters", HttpStatus.BAD_REQUEST);
        }

        customer.setPassword(passwordEncoder.encode(request.newPassword()));
        customerDao.save(customer);
        return ResponseEntity.ok("Password changed");
    }

    public record RoleRequest(String role) {}

    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeRole(@PathVariable int id, @RequestBody RoleRequest request, Authentication caller) {
        Customer customer = customerDao.findById(id).orElse(null);
        if (customer == null) {
            return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
        }
        if (!"user".equals(request.role()) && !"admin".equals(request.role())) {
            return new ResponseEntity<>("Role must be user or admin", HttpStatus.BAD_REQUEST);
        }
        if (isSelf(caller, id)) {
            return new ResponseEntity<>("You cannot change your own role", HttpStatus.BAD_REQUEST);
        }
        customer.setRole(request.role());
        return ResponseEntity.ok(customerDao.save(customer));
    }

    // Disables instead of deleting, so the data stays for the legal retention period.
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> disableCustomer(@PathVariable int id, Authentication caller) {
        Customer customer = customerDao.findById(id).orElse(null);
        if (customer == null) {
            return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
        }
        if (isSelf(caller, id)) {
            return new ResponseEntity<>("You cannot disable your own account", HttpStatus.BAD_REQUEST);
        }

        // Drop reservations that have not finished yet; past rentals stay as history.
        LocalDateTime now = LocalDateTime.now();
        List<Rental> open = rentalDao.findByCustomerId(id).stream()
                .filter(rental -> "Pending".equals(rental.getRentalStatus())
                        || ("Approved".equals(rental.getRentalStatus())
                            && rental.getRentalEndTime() != null && rental.getRentalEndTime().isAfter(now)))
                .toList();
        rentalDao.deleteAll(open);

        customer.setDisabled(true);
        customerDao.save(customer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableCustomer(@PathVariable int id) {
        Customer customer = customerDao.findById(id).orElse(null);
        if (customer == null) {
            return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
        }
        customer.setDisabled(false);
        return ResponseEntity.ok(customerDao.save(customer));
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
        if (existingCustomer.isDisabled()) {
            return new ResponseEntity<>("This account has been disabled", HttpStatus.FORBIDDEN);
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

    private boolean isSelf(Authentication caller, int id) {
        return caller.getName().equals(String.valueOf(id));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
