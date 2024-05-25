package com.example.EcoRide.Rental.dao;

import com.example.EcoRide.Rental.models.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalDao extends JpaRepository<Rental, Integer> {
//    Rental getRentalById(int id);
//    List<Rental> getAllRentals();
//    void saveRental(Rental rental);
//    void updateRental(Rental rental);
//    void deleteRental(int id);
}
