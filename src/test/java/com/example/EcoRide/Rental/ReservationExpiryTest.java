package com.example.EcoRide.Rental;

import com.example.EcoRide.Rental.dao.RentalDao;
import com.example.EcoRide.Rental.service.ReservationExpiry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** The UPDATE itself is checked against MySQL in the Docker run. */
class ReservationExpiryTest {

    @Test
    void cancelsPendingRequestsThatHaveStarted() {
        RentalDao rentalDao = mock(RentalDao.class);
        LocalDateTime before = LocalDateTime.now();

        new ReservationExpiry(rentalDao).cancelStalePendingReservations();

        ArgumentCaptor<LocalDateTime> now = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(rentalDao).cancelPendingStartedBefore(now.capture());
        assertTrue(!now.getValue().isBefore(before) && !now.getValue().isAfter(LocalDateTime.now()));
    }
}
