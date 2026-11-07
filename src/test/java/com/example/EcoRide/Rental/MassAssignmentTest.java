package com.example.EcoRide.Rental;

import com.example.EcoRide.Rental.config.SecurityConfig;
import com.example.EcoRide.Rental.controllers.CustomerController;
import com.example.EcoRide.Rental.controllers.RentalController;
import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.dao.EbikeDao;
import com.example.EcoRide.Rental.dao.RentalDao;
import com.example.EcoRide.Rental.models.Customer;
import com.example.EcoRide.Rental.models.Ebike;
import com.example.EcoRide.Rental.models.Rental;
import com.example.EcoRide.Rental.service.Email;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Requests must not be able to set fields the API does not offer, such as id, role or status. */
@WebMvcTest({CustomerController.class, RentalController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=test-secret-test-secret-test-secret-1234")
class MassAssignmentTest {

    private static final RequestPostProcessor ADMIN =
            jwt().jwt(token -> token.subject("1")).authorities(new SimpleGrantedAuthority("ROLE_admin"));

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CustomerDao customerDao;
    @MockBean
    private RentalDao rentalDao;
    @MockBean
    private EbikeDao ebikeDao;
    @MockBean
    private Email email;

    @Test
    void registerIgnoresIdRoleAndBalance() throws Exception {
        send(post("/customers/register"), """
                {"id":1,"name":"Eve","email":"eve@demo.local","password":"pw12345","phone":"1",
                 "role":"admin","accountBalance":99999}""")
                .andExpect(status().isOk());

        ArgumentCaptor<Customer> saved = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).save(saved.capture());
        assertEquals(0, saved.getValue().getId(), "id must not come from the request");
        assertEquals("user", saved.getValue().getRole());
        assertEquals(0, saved.getValue().getAccountBalance());
        assertEquals("eve@demo.local", saved.getValue().getEmail());
    }

    @Test
    void updateCannotChangeRoleOrBalance() throws Exception {
        Customer existing = new Customer(5, "Eve", "eve@demo.local", "1", 10, "hash", "user");
        when(customerDao.findById(5)).thenReturn(Optional.of(existing));
        when(customerDao.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

        send(put("/customers/5").with(jwt().jwt(token -> token.subject("5"))), """
                {"id":1,"name":"Eve2","email":"eve@demo.local","phone":"2","role":"admin","accountBalance":99999}""")
                .andExpect(status().isOk());

        assertEquals(5, existing.getId());
        assertEquals("user", existing.getRole());
        assertEquals(10, existing.getAccountBalance());
        assertEquals("Eve2", existing.getName());
    }

    @Test
    void reservationIgnoresIdAndAlwaysStartsPending() throws Exception {
        when(customerDao.findById(2)).thenReturn(Optional.of(new Customer()));
        when(ebikeDao.findById(3)).thenReturn(Optional.of(new Ebike()));

        send(post("/rentals/pending").with(jwt().jwt(token -> token.subject("2"))), """
                {"id":1,"customer":{"id":2},"ebike":{"id":3},"passportNumber":"X1",
                 "rentalStartTime":"2026-12-01T10:00","rentalEndTime":"2026-12-01T12:00",
                 "rentalStatus":"Approved"}""")
                .andExpect(status().isCreated());

        ArgumentCaptor<Rental> saved = ArgumentCaptor.forClass(Rental.class);
        verify(rentalDao).save(saved.capture());
        assertEquals(0, saved.getValue().getId(), "id must not come from the request");
        assertEquals("Pending", saved.getValue().getRentalStatus());
        assertEquals("X1", saved.getValue().getPassportNumber());
    }

    @Test
    void endpointsThatSavedRawRequestsAreGone() throws Exception {
        // Sent as an admin, so a 405 proves the endpoint is gone rather than just blocked.
        send(post("/customers").with(ADMIN), "{\"email\":\"e@demo.local\",\"password\":\"pw\",\"role\":\"admin\"}")
                .andExpect(status().isMethodNotAllowed());
        send(post("/rentals").with(ADMIN), "{\"rentalStatus\":\"Approved\"}")
                .andExpect(status().isMethodNotAllowed());
        send(put("/rentals").with(ADMIN), "{\"rentalStatus\":\"Approved\"}")
                .andExpect(status().isMethodNotAllowed());
    }

    private org.springframework.test.web.servlet.ResultActions send(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, String body)
            throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
