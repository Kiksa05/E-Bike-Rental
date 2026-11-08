package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Payment;
import com.example.EcoRide.Rental.dao.PaymentDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentDao paymentDao;

    public PaymentController(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable int id) {
        return paymentDao.findById(id).orElse(null);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentDao.findAll();
    }

    public record PaymentRequest(int rentalId, double amount, String paymentStatus) {}

    @PostMapping
    public void savePayment(@RequestBody PaymentRequest request) {
        paymentDao.save(apply(request, new Payment()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable int id, @RequestBody PaymentRequest request) {
        Payment payment = paymentDao.findById(id).orElse(null);
        if (payment != null) {
            return new ResponseEntity<>(paymentDao.save(apply(request, payment)), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private Payment apply(PaymentRequest request, Payment payment) {
        payment.setRentalId(request.rentalId());
        payment.setAmount(request.amount());
        payment.setPaymentStatus(request.paymentStatus());
        return payment;
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
