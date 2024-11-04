package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Customer;
import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.service.Email;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/customers")
@CrossOrigin
public class CustomerController {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private Email emailService;


    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable int id) {
        return customerDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerDao.findAll();
    }

    @PostMapping
    public void saveCustomer(@RequestBody Customer customer) {
        customerDao.save(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable int id, @RequestBody Customer customerDetails) {
        Customer customer = customerDao.findById(id).orElse(null);
        if(customer != null){
            customer.setName(customerDetails.getName());
            customer.setEmail(customerDetails.getEmail());
            customer.setPhone(customerDetails.getPhone());
            customer.setAccountBalance(customerDetails.getAccountBalance());
            customer.setPassword(customerDetails.getPassword());
            return new ResponseEntity<>(customerDao.save(customer), HttpStatus.OK);
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
        String email = customer.getEmail();
        String role = email.equalsIgnoreCase("admin@admin.com") ? "admin" : "user";
//        customer.setRole(role);
        // Check if email already exists
        if (customerDao.findByEmail(customer.getEmail()) != null) {
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }

        customer.setRole(role);
        // Save the customer with plain text password (not recommended for production)
        customerDao.save(customer);

        // Send registration email with PDF attachment
        try {
            emailService.sendRegistrationEmail(customer.getEmail(), customer.getName());
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to send email");
        }

        return new ResponseEntity<>("Registration successful", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginCustomer(@RequestBody Customer customer) {
        // Find customer by email
        Customer existingCustomer = customerDao.findByEmail(customer.getEmail());
        if (existingCustomer == null || !customer.getPassword().equals(existingCustomer.getPassword())) {
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        String userId = String.valueOf(existingCustomer.getId());
        String userRole = existingCustomer.getRole();
        String jsonResponse = "{\"userId\": \"" + userId + "\", \"userRole\": \"" + userRole + "\"}";

        return ResponseEntity.ok(jsonResponse);
    }

}
