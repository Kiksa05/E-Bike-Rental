package com.example.EcoRide.Rental.dao;

import com.example.EcoRide.Rental.models.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface RentalDao extends JpaRepository<Rental, Integer> {
    @Query("SELECT r FROM Rental r WHERE r.customer.id = :customerId AND r.ebike.id = (SELECT e.id FROM Ebike e WHERE e.model = :ebikeModel) AND r.rentalStartTime = :startDate AND r.rentalEndTime = :endDate")
    List<Rental> findRentalsByCustomerEbikeAndDate(int customerId, String ebikeModel, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT r FROM Rental r WHERE r.rentalStatus = :status")
    List<Rental> findByRentalStatus(@Param("status") String status);

    List<Rental> findByCustomerId(int customerId);

    List<Rental> findByCustomerIdOrderByRentalStartTimeDesc(int customerId);

    // Two time ranges overlap when each starts before the other ends; back-to-back bookings (10-12, 12-14) are fine.
    @Query("SELECT COUNT(r) > 0 FROM Rental r WHERE r.ebike.id = :ebikeId AND r.id <> :excludeId"
            + " AND r.rentalStatus IN ('Pending', 'Approved')"
            + " AND r.rentalStartTime < :end AND r.rentalEndTime > :start")
    boolean existsOverlap(@Param("ebikeId") int ebikeId, @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end, @Param("excludeId") int excludeId);

    @Query("SELECT COUNT(r) > 0 FROM Rental r WHERE r.ebike.id = :ebikeId AND r.id <> :excludeId"
            + " AND r.rentalStatus = 'Approved'"
            + " AND r.rentalStartTime < :end AND r.rentalEndTime > :start")
    boolean existsApprovedOverlap(@Param("ebikeId") int ebikeId, @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end, @Param("excludeId") int excludeId);
}
