package com.example.EcoRide.Rental.service;

import com.example.EcoRide.Rental.dao.RentalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** A request nobody approved before its start time can no longer happen, so it is cancelled and its hours freed. */
@Service
public class ReservationExpiry {

    private static final Logger logger = LoggerFactory.getLogger(ReservationExpiry.class);

    private final RentalDao rentalDao;

    public ReservationExpiry(RentalDao rentalDao) {
        this.rentalDao = rentalDao;
    }

    // Bookings start on the hour, so checking every minute cancels a stale request within a minute of its start.
    @Scheduled(fixedRate = 60_000)
    public void cancelStalePendingReservations() {
        int cancelled = rentalDao.cancelPendingStartedBefore(LocalDateTime.now());
        if (cancelled > 0) {
            logger.info("Cancelled {} pending reservation(s) that were not approved before their start time", cancelled);
        }
    }
}
