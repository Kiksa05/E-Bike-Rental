package com.example.EcoRide.Rental;

import com.example.EcoRide.Rental.config.SecurityConfig;
import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.dao.EbikeDao;
import com.example.EcoRide.Rental.dao.PaymentDao;
import com.example.EcoRide.Rental.dao.RentalDao;
import com.example.EcoRide.Rental.dao.StationDao;
import com.example.EcoRide.Rental.models.Customer;
import com.example.EcoRide.Rental.models.Ebike;
import com.example.EcoRide.Rental.models.Rental;
import com.example.EcoRide.Rental.service.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Double booking and reservation history. The overlap query itself is checked against MySQL in the Docker run. */
@WebMvcTest
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=test-secret-test-secret-test-secret-1234")
class ReservationRulesTest {

    private static final RequestPostProcessor ADMIN =
            jwt().jwt(token -> token.subject("1")).authorities(new SimpleGrantedAuthority("ROLE_admin"));
    private static final RequestPostProcessor USER_2 = jwt().jwt(token -> token.subject("2"));
    private static final LocalDateTime START = LocalDateTime.parse("2099-12-01T10:00");
    private static final LocalDateTime END = LocalDateTime.parse("2099-12-01T12:00");
    private static final String RESERVATION = """
            {"ebike":{"id":3},"passportNumber":"X1",
             "rentalStartTime":"2099-12-01T10:00","rentalEndTime":"2099-12-01T12:00"}""";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CustomerDao customerDao;
    @MockBean
    private RentalDao rentalDao;
    @MockBean
    private EbikeDao ebikeDao;
    @MockBean
    private PaymentDao paymentDao;
    @MockBean
    private StationDao stationDao;
    @MockBean
    private Email email;

    private Ebike bike;

    @BeforeEach
    void setUp() {
        bike = new Ebike();
        bike.setId(3);
        bike.setStatus("Available");
        when(customerDao.findById(2)).thenReturn(Optional.of(new Customer(2, "U", "u@demo.local", "1", 0, "hash", "user")));
        when(ebikeDao.findByIdForUpdate(3)).thenReturn(Optional.of(bike));
    }

    // Double booking

    @Test
    void freeTimeSlotIsBooked() throws Exception {
        reserve().andExpect(status().isCreated());

        verify(rentalDao).existsOverlap(3, START, END, 0);
        verify(rentalDao).save(any(Rental.class));
    }

    @Test
    void overlappingReservationIsRefused() throws Exception {
        when(rentalDao.existsOverlap(3, START, END, 0)).thenReturn(true);

        reserve().andExpect(status().isConflict())
                .andExpect(content().string("This bike is already booked for that time. Pick another time or bike."));

        verify(rentalDao, never()).save(any());
    }

    @Test
    void unavailableBikeCannotBeReserved() throws Exception {
        bike.setStatus("Not Available");

        reserve().andExpect(status().isConflict())
                .andExpect(content().string("This bike is not available right now"));

        verify(rentalDao, never()).save(any());
    }

    @Test
    void approvalIsRefusedWhenItClashesWithAnApprovedReservation() throws Exception {
        Rental rental = new Rental();
        rental.setId(7);
        rental.setEbike(bike);
        rental.setRentalStatus("Pending");
        rental.setRentalStartTime(START);
        rental.setRentalEndTime(END);
        when(rentalDao.findById(7)).thenReturn(Optional.of(rental));
        when(rentalDao.existsApprovedOverlap(3, START, END, 7)).thenReturn(true);

        mvc.perform(put("/rentals/approve/7").with(ADMIN)).andExpect(status().isConflict());

        assertEquals("Pending", rental.getRentalStatus());
        assertEquals("Available", bike.getStatus());
        verify(rentalDao, never()).save(any());
    }

    @Test
    void reservationsMustStartAndEndOnTheHour() throws Exception {
        for (String times : List.of(
                "\"rentalStartTime\":\"2099-12-01T13:47\",\"rentalEndTime\":\"2099-12-01T15:00\"",
                "\"rentalStartTime\":\"2099-12-01T13:00\",\"rentalEndTime\":\"2099-12-01T15:47\"",
                "\"rentalStartTime\":\"2099-12-01T13:00:30\",\"rentalEndTime\":\"2099-12-01T15:00\"")) {
            mvc.perform(post("/rentals/pending").with(USER_2).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"ebike\":{\"id\":3},\"passportNumber\":\"X1\"," + times + "}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Reservations start and end on the hour, like 13:00"));
        }
        verify(rentalDao, never()).save(any());
    }

    @Test
    void reservationsCannotStartInThePast() throws Exception {
        mvc.perform(post("/rentals/pending").with(USER_2).contentType(MediaType.APPLICATION_JSON).content("""
                        {"ebike":{"id":3},"passportNumber":"X1",
                         "rentalStartTime":"2020-01-01T10:00","rentalEndTime":"2020-01-01T12:00"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The start time is in the past"));

        verify(rentalDao, never()).save(any());
    }

    // Calendar

    @Test
    void anyoneCanSeeWhenABikeIsBookedButNotWho() throws Exception {
        Rental booked = new Rental();
        booked.setCustomer(new Customer(2, "Secret Name", "secret@demo.local", "1", 0, "hash", "user"));
        booked.setPassportNumber("SECRET-PASSPORT");
        booked.setRentalStatus("Approved");
        booked.setRentalStartTime(START);
        booked.setRentalEndTime(END);
        when(rentalDao.findUpcomingBookings(eq(3), any(LocalDateTime.class))).thenReturn(List.of(booked));

        String body = mvc.perform(get("/ebikes/3/booked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].start").value("2099-12-01T10:00:00"))
                .andExpect(jsonPath("$[0].end").value("2099-12-01T12:00:00"))
                .andReturn().getResponse().getContentAsString();

        assertFalse(body.toLowerCase().contains("secret"), body);
    }

    // Cancelling

    @Test
    void ownerCancelsAPendingReservation() throws Exception {
        Rental rental = ownedBy(2, "Pending", LocalDateTime.now().plusDays(1));

        mvc.perform(put("/rentals/7/cancel").with(USER_2)).andExpect(status().isOk());

        assertEquals("Cancelled", rental.getRentalStatus());
        verify(rentalDao).save(rental);
        verify(rentalDao, never()).delete(any());
    }

    @Test
    void ownerCancelsAnApprovedReservationBeforeItStarts() throws Exception {
        Rental rental = ownedBy(2, "Approved", LocalDateTime.now().plusHours(2));

        mvc.perform(put("/rentals/7/cancel").with(USER_2)).andExpect(status().isOk());

        assertEquals("Cancelled", rental.getRentalStatus());
    }

    @Test
    void adminCancelsSomeoneElsesReservation() throws Exception {
        Rental rental = ownedBy(2, "Approved", LocalDateTime.now().plusDays(1));

        mvc.perform(put("/rentals/7/cancel").with(ADMIN)).andExpect(status().isOk());

        assertEquals("Cancelled", rental.getRentalStatus());
    }

    @Test
    void otherUsersCannotCancelYourReservation() throws Exception {
        Rental rental = ownedBy(5, "Pending", LocalDateTime.now().plusDays(1));

        mvc.perform(put("/rentals/7/cancel").with(USER_2)).andExpect(status().isForbidden());

        assertEquals("Pending", rental.getRentalStatus());
        verify(rentalDao, never()).save(any());
    }

    @Test
    void startedRentalsCannotBeCancelled() throws Exception {
        Rental rental = ownedBy(2, "Approved", LocalDateTime.now().minusMinutes(5));

        mvc.perform(put("/rentals/7/cancel").with(USER_2))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This rental has already started"));

        assertEquals("Approved", rental.getRentalStatus());
    }

    @Test
    void onlyPendingOrApprovedReservationsCanBeCancelled() throws Exception {
        for (String finished : List.of("Cancelled", "Rejected")) {
            Rental rental = ownedBy(2, finished, LocalDateTime.now().plusDays(1));

            mvc.perform(put("/rentals/7/cancel").with(USER_2))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Only pending or approved reservations can be cancelled"));

            assertEquals(finished, rental.getRentalStatus());
        }
        verify(rentalDao, never()).save(any());
    }

    @Test
    void cancellingNeedsALoginAndAnExistingRental() throws Exception {
        mvc.perform(put("/rentals/7/cancel")).andExpect(status().isUnauthorized());
        mvc.perform(put("/rentals/99/cancel").with(USER_2)).andExpect(status().isNotFound());
    }

    private Rental ownedBy(int customerId, String status, LocalDateTime start) {
        Rental rental = new Rental();
        rental.setId(7);
        rental.setCustomer(new Customer(customerId, "C", "c@demo.local", "1", 0, "hash", "user"));
        rental.setEbike(bike);
        rental.setRentalStatus(status);
        rental.setRentalStartTime(start);
        rental.setRentalEndTime(start.plusHours(2));
        when(rentalDao.findById(7)).thenReturn(Optional.of(rental));
        return rental;
    }

    // History

    @Test
    void usersSeeOnlyTheirOwnReservations() throws Exception {
        Rental mine = new Rental();
        mine.setId(5);
        when(rentalDao.findByCustomerIdOrderByRentalStartTimeDesc(2)).thenReturn(List.of(mine));

        mvc.perform(get("/rentals/mine").with(USER_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5));

        // The id comes from the token; there is no way to ask for someone else's.
        verify(rentalDao).findByCustomerIdOrderByRentalStartTimeDesc(2);
        verify(rentalDao, never()).findAll();
    }

    @Test
    void adminSeesEveryReservationNewestFirst() throws Exception {
        mvc.perform(get("/rentals").with(ADMIN)).andExpect(status().isOk());

        verify(rentalDao).findAll(Sort.by(Sort.Direction.DESC, "rentalStartTime"));
    }

    @Test
    void historyNeedsALogin() throws Exception {
        mvc.perform(get("/rentals/mine")).andExpect(status().isUnauthorized());
        mvc.perform(get("/rentals").with(USER_2)).andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions reserve() throws Exception {
        return mvc.perform(post("/rentals/pending").with(USER_2).contentType(MediaType.APPLICATION_JSON).content(RESERVATION));
    }
}
