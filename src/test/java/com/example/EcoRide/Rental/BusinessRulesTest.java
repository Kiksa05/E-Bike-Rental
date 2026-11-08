package com.example.EcoRide.Rental;

import com.example.EcoRide.Rental.config.SecurityConfig;
import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.dao.EbikeDao;
import com.example.EcoRide.Rental.dao.PaymentDao;
import com.example.EcoRide.Rental.dao.RentalDao;
import com.example.EcoRide.Rental.dao.StationDao;
import com.example.EcoRide.Rental.models.Customer;
import com.example.EcoRide.Rental.models.Ebike;
import com.example.EcoRide.Rental.models.Payment;
import com.example.EcoRide.Rental.models.Rental;
import com.example.EcoRide.Rental.models.Station;
import com.example.EcoRide.Rental.service.Email;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** What the app does once a request is allowed in. Database calls are mocked, so no real data is touched. */
@WebMvcTest
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=test-secret-test-secret-test-secret-1234")
class BusinessRulesTest {

    private static final RequestPostProcessor ADMIN =
            jwt().jwt(token -> token.subject("1")).authorities(new SimpleGrantedAuthority("ROLE_admin"));
    private static final RequestPostProcessor USER_2 = jwt().jwt(token -> token.subject("2"));

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

    // Rentals

    @Test
    void approvingBlocksOnlyTheBookedTimeNotTheWholeBike() throws Exception {
        Rental rental = pendingRental();
        when(rentalDao.findById(1)).thenReturn(Optional.of(rental));

        mvc.perform(put("/rentals/approve/1").with(ADMIN)).andExpect(status().isOk());

        assertEquals("Approved", rental.getRentalStatus());
        assertEquals("Available", rental.getEbike().getStatus());
        verify(rentalDao).save(rental);
        verify(ebikeDao, never()).save(any());
    }

    @Test
    void rejectingLeavesTheBikeAvailable() throws Exception {
        Rental rental = pendingRental();
        when(rentalDao.findById(1)).thenReturn(Optional.of(rental));

        mvc.perform(put("/rentals/reject/1").with(ADMIN)).andExpect(status().isOk());

        assertEquals("Rejected", rental.getRentalStatus());
        assertEquals("Available", rental.getEbike().getStatus());
        verify(rentalDao).save(rental);
        verify(ebikeDao, never()).save(any());
    }

    @Test
    void deletingARentalKeepsTheAdminsBikeStatus() throws Exception {
        Rental rental = pendingRental();
        rental.getEbike().setStatus("Not Available"); // e.g. an admin took it out for repairs
        when(rentalDao.findById(1)).thenReturn(Optional.of(rental));

        mvc.perform(delete("/rentals/1").with(ADMIN)).andExpect(status().isOk());

        assertEquals("Not Available", rental.getEbike().getStatus());
        verify(rentalDao).delete(rental);
    }

    @Test
    void unknownRentalsReturnNotFound() throws Exception {
        mvc.perform(put("/rentals/approve/99").with(ADMIN)).andExpect(status().isNotFound());
        mvc.perform(put("/rentals/reject/99").with(ADMIN)).andExpect(status().isNotFound());
        mvc.perform(delete("/rentals/99").with(ADMIN)).andExpect(status().isNotFound());
    }

    @Test
    void reservationNeedsABikeAndPassport() throws Exception {
        String times = "\"rentalStartTime\":\"2099-12-01T10:00\",\"rentalEndTime\":\"2099-12-01T12:00\"";

        send(post("/rentals/pending").with(USER_2), "{\"passportNumber\":\"X1\"," + times + "}")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ebike ID is required"));
        send(post("/rentals/pending").with(USER_2), "{\"ebike\":{\"id\":3}," + times + "}")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Passport number is required"));
        send(post("/rentals/pending").with(USER_2), "{\"ebike\":{\"id\":3},\"passportNumber\":\"\"," + times + "}")
                .andExpect(status().isBadRequest());

        verify(rentalDao, never()).save(any());
    }

    @Test
    void reservationNeedsBothTimes() throws Exception {
        send(post("/rentals/pending").with(USER_2),
                "{\"ebike\":{\"id\":3},\"passportNumber\":\"X1\",\"rentalStartTime\":\"2099-12-01T10:00\"}")
                .andExpect(status().isBadRequest());

        verify(rentalDao, never()).save(any());
    }

    @Test
    void reservationForAnUnknownBikeIsRefused() throws Exception {
        when(customerDao.findById(2)).thenReturn(Optional.of(new Customer()));

        send(post("/rentals/pending").with(USER_2), """
                {"ebike":{"id":99},"passportNumber":"X1",
                 "rentalStartTime":"2099-12-01T10:00","rentalEndTime":"2099-12-01T12:00"}""")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ebike not found"));

        verify(rentalDao, never()).save(any());
    }

    // Registration and accounts

    @Test
    void registrationStoresAHashAndNormalisesTheEmail() throws Exception {
        send(post("/customers/register"), registration("  Rider@Demo.Local ", "pw12345")).andExpect(status().isOk());

        ArgumentCaptor<Customer> saved = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).save(saved.capture());
        assertEquals("rider@demo.local", saved.getValue().getEmail());
        assertNotEquals("pw12345", saved.getValue().getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("pw12345", saved.getValue().getPassword()));
    }

    @Test
    void registrationNeedsAnEmailAndPassword() throws Exception {
        send(post("/customers/register"), registration(" ", "pw12345"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is required"));
        send(post("/customers/register"), registration("rider@demo.local", " "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password is required"));

        verify(customerDao, never()).save(any());
    }

    @Test
    void duplicateEmailIsRefused() throws Exception {
        when(customerDao.existsByEmailIgnoreCase("rider@demo.local")).thenReturn(true);

        send(post("/customers/register"), registration("rider@demo.local", "pw12345"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already in use"));

        verify(customerDao, never()).save(any());
    }

    @Test
    void twoSimultaneousSignupsGetACleanError() throws Exception {
        // Both pass the exists check, then the database's unique rule stops the second one.
        when(customerDao.save(any(Customer.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        send(post("/customers/register"), registration("rider@demo.local", "pw12345"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already in use"));
    }

    @Test
    void loginWithAnUnknownEmailGivesTheSameErrorAsAWrongPassword() throws Exception {
        send(post("/customers/login"), "{\"email\":\"nobody@demo.local\",\"password\":\"pw12345\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }

    @Test
    void updateKeepsThePasswordWhenNoneIsSent() throws Exception {
        Customer existing = customerWithId2();
        when(customerDao.findById(2)).thenReturn(Optional.of(existing));
        when(customerDao.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

        send(put("/customers/2").with(USER_2), "{\"name\":\"New\",\"email\":\"u@demo.local\",\"phone\":\"2\"}")
                .andExpect(status().isOk());

        assertEquals("old-hash", existing.getPassword());
        assertEquals("New", existing.getName());
    }

    @Test
    void profileUpdateCannotChangeThePassword() throws Exception {
        Customer existing = customerWithId2();
        when(customerDao.findById(2)).thenReturn(Optional.of(existing));
        when(customerDao.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

        // Passwords change only through /password, which asks for the current one.
        send(put("/customers/2").with(USER_2), "{\"name\":\"U\",\"email\":\"u@demo.local\",\"phone\":\"1\",\"password\":\"new-pass\"}")
                .andExpect(status().isOk());

        assertEquals("old-hash", existing.getPassword());
    }

    @Test
    void updateCannotTakeAnotherCustomersEmail() throws Exception {
        when(customerDao.findById(2)).thenReturn(Optional.of(customerWithId2()));
        when(customerDao.existsByEmailIgnoreCaseAndIdNot("taken@demo.local", 2)).thenReturn(true);

        send(put("/customers/2").with(USER_2), "{\"name\":\"U\",\"email\":\"taken@demo.local\",\"phone\":\"1\"}")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already in use"));

        verify(customerDao, never()).save(any());
    }

    @Test
    void customerJsonNeverContainsThePassword() throws Exception {
        when(customerDao.findAll()).thenReturn(List.of(customerWithId2()));

        String body = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/customers").with(ADMIN))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertFalse(body.contains("old-hash"));
        assertFalse(body.contains("password"));
    }

    @Test
    void sameMailIgnoresCaseAndHandlesMissingValues() {
        Customer lower = new Customer();
        lower.setEmail("rider@demo.local");
        Customer upper = new Customer();
        upper.setEmail("RIDER@demo.local");

        assertTrue(lower.sameMail(upper));
        assertFalse(lower.sameMail(null));
        assertFalse(lower.sameMail(new Customer()));
        assertFalse(new Customer().sameMail(lower));
    }

    // Bikes, stations and payments

    @Test
    void updatingABikeChangesItsDetailsButKeepsItsStation() throws Exception {
        Station station = new Station();
        Ebike ebike = new Ebike(1, "Old", 10, "Available", station);
        when(ebikeDao.findById(1)).thenReturn(Optional.of(ebike));
        when(ebikeDao.save(any(Ebike.class))).thenAnswer(call -> call.getArgument(0));

        send(put("/ebikes/1").with(ADMIN), "{\"id\":9,\"model\":\"New\",\"batteryLevel\":80,\"status\":\"Available\",\"price\":25}")
                .andExpect(status().isOk());

        assertEquals(1, ebike.getId());
        assertEquals("New", ebike.getModel());
        assertEquals(80, ebike.getBatteryLevel());
        assertEquals(25, ebike.getPrice());
        assertSame(station, ebike.getStation());
    }

    @Test
    void updatingAStationKeepsItsBikes() throws Exception {
        Station station = new Station();
        List<Ebike> bikes = List.of(new Ebike());
        station.setAvailableBikes(bikes);
        when(stationDao.findById(1)).thenReturn(Optional.of(station));
        when(stationDao.save(any(Station.class))).thenAnswer(call -> call.getArgument(0));

        send(put("/stations/1").with(ADMIN), "{\"location\":\"Celje\",\"availableBikes\":[]}").andExpect(status().isOk());

        assertEquals("Celje", station.getLocation());
        assertSame(bikes, station.getAvailableBikes());
    }

    @Test
    void newBikesStationsAndPaymentsIgnoreASuppliedId() throws Exception {
        send(post("/ebikes").with(ADMIN), "{\"id\":1,\"model\":\"New\"}").andExpect(status().isOk());
        send(post("/stations").with(ADMIN), "{\"id\":1,\"location\":\"Celje\"}").andExpect(status().isOk());
        send(post("/payments").with(ADMIN), "{\"id\":1,\"rentalId\":2,\"amount\":15,\"paymentStatus\":\"Paid\"}").andExpect(status().isOk());

        ArgumentCaptor<Ebike> bike = ArgumentCaptor.forClass(Ebike.class);
        verify(ebikeDao).save(bike.capture());
        assertEquals(0, bike.getValue().getId());

        ArgumentCaptor<Station> station = ArgumentCaptor.forClass(Station.class);
        verify(stationDao).save(station.capture());
        assertEquals(0, station.getValue().getId());

        ArgumentCaptor<Payment> payment = ArgumentCaptor.forClass(Payment.class);
        verify(paymentDao).save(payment.capture());
        assertEquals(0, payment.getValue().getId());
        assertEquals(2, payment.getValue().getRentalId());
    }

    @Test
    void updatingOrDeletingSomethingUnknownReturnsNotFound() throws Exception {
        send(put("/ebikes/99").with(ADMIN), "{}").andExpect(status().isNotFound());
        send(put("/stations/99").with(ADMIN), "{}").andExpect(status().isNotFound());
        send(put("/payments/99").with(ADMIN), "{}").andExpect(status().isNotFound());
        send(put("/customers/99").with(ADMIN), "{}").andExpect(status().isNotFound());
        mvc.perform(delete("/ebikes/99").with(ADMIN)).andExpect(status().isNotFound());
        mvc.perform(delete("/stations/99").with(ADMIN)).andExpect(status().isNotFound());
        mvc.perform(delete("/payments/99").with(ADMIN)).andExpect(status().isNotFound());
        mvc.perform(delete("/customers/99").with(ADMIN)).andExpect(status().isNotFound());
    }

    private Rental pendingRental() {
        Ebike ebike = new Ebike();
        ebike.setStatus("Available");
        Rental rental = new Rental();
        rental.setEbike(ebike);
        rental.setRentalStatus("Pending");
        return rental;
    }

    private Customer customerWithId2() {
        return new Customer(2, "U", "u@demo.local", "1", 0, "old-hash", "user");
    }

    private String registration(String emailAddress, String password) {
        return "{\"name\":\"R\",\"email\":\"" + emailAddress + "\",\"password\":\"" + password + "\",\"phone\":\"1\"}";
    }

    private ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
