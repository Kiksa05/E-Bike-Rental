package com.example.EcoRide.Rental.dao;

import com.example.EcoRide.Rental.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentDao extends JpaRepository<Payment, Integer>{
//    Payment getPaymentById(int id);
//    List<Payment> getAllPayments();
//    void savePayment(Payment payment);
//    void updatePayment(Payment payment);
//    void deletePayment(int id);
}
