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
import static org.mockito.ArgumentMatchers.any;
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
    private static final LocalDateTime START = LocalDateTime.parse("2026-12-01T10:00");
    private static final LocalDateTime END = LocalDateTime.parse("2026-12-01T12:00");
    private static final String RESERVATION = """
            {"ebike":{"id":3},"passportNumber":"X1",
             "rentalStartTime":"2026-12-01T10:00","rentalEndTime":"2026-12-01T12:00"}""";

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
