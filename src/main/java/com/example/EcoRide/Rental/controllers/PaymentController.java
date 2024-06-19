package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Payment;
import com.example.EcoRide.Rental.dao.PaymentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentDao paymentDao;

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable int id) {
        return paymentDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentDao.findAll();
    }

    @PostMapping
    public void savePayment(@RequestBody Payment payment) {
        paymentDao.save(payment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable int id, @RequestBody Payment paymentDetails) {
        Payment payment = paymentDao.findById(id).orElse(null);
        if (payment != null) {
            payment.setRentalId(paymentDetails.getRentalId());
            payment.setAmount(paymentDetails.getAmount());
            payment.setPaymentStatus(paymentDetails.getPaymentStatus());
            return new ResponseEntity<>(paymentDao.save(payment), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable int id) {
        Payment payment = paymentDao.findById(id).orElse(null);
        if (payment != null) {
            paymentDao.delete(payment);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
