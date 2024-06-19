package com.example.EcoRide.Rental.controllers;

import com.example.EcoRide.Rental.models.Payment;
import com.example.EcoRide.Rental.dao.PaymentDao;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PutMapping
    public void updatePayment(@PathVariable int id,@RequestBody Payment paymentDetails) {
        Payment payment = paymentDao.findById(id).orElse(null);
        payment.setPaymentStatus(paymentDetails.getPaymentStatus());
        payment.setAmount(paymentDetails.getAmount());
        payment.setRentalId(paymentDetails.getRentalId());
        paymentDao.save(payment);
    }

    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable int id) {
        Payment payment = paymentDao.findById(id).orElse(null);
        paymentDao.delete(payment);
    }
}
