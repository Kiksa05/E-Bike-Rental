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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

/** Profile editing, password changes, roles and disabling accounts. */
@WebMvcTest
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=test-secret-test-secret-test-secret-1234")
class ProfileManagementTest {

    private static final RequestPostProcessor ADMIN_1 =
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

    private Customer user;
    private Customer admin;

    @BeforeEach
    void setUp() {
        user = new Customer(2, "U", "u@demo.local", "1", 0, new BCryptPasswordEncoder(4).encode("old-password"), "user");
        admin = new Customer(1, "A", "a@demo.local", "1", 0, "hash", "admin");
        when(customerDao.findById(1)).thenReturn(Optional.of(admin));
        when(customerDao.findById(2)).thenReturn(Optional.of(user));
        when(customerDao.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));
    }

    // Profile

    @Test
    void userCanSetTheirPassportNumber() throws Exception {
        send(put("/customers/2").with(USER_2), "{\"name\":\"U\",\"email\":\"u@demo.local\",\"phone\":\"1\",\"passportNumber\":\"P123\"}")
                .andExpect(status().isOk());

        assertEquals("P123", user.getPassportNumber());
    }

    @Test
    void profileUpdateCannotDisableTheAccount() throws Exception {
        send(put("/customers/2").with(USER_2), "{\"name\":\"U\",\"email\":\"u@demo.local\",\"disabled\":true}")
                .andExpect(status().isOk());

        assertFalse(user.isDisabled());
    }

    // Password

    @Test
    void userChangesTheirPasswordWithTheCurrentOne() throws Exception {
        send(put("/customers/2/password").with(USER_2), "{\"currentPassword\":\"old-password\",\"newPassword\":\"new-password\"}")
                .andExpect(status().isOk());

        assertTrue(new BCryptPasswordEncoder().matches("new-password", user.getPassword()));
    }

    @Test
    void wrongCurrentPasswordIsRefused() throws Exception {
        send(put("/customers/2/password").with(USER_2), "{\"currentPassword\":\"guess\",\"newPassword\":\"new-password\"}")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Current password is incorrect"));
        send(put("/customers/2/password").with(USER_2), "{\"newPassword\":\"new-password\"}")
                .andExpect(status().isBadRequest());

        verify(customerDao, never()).save(any());
    }

    @Test
    void newPasswordNeedsEightCharacters() throws Exception {
        send(put("/customers/2/password").with(USER_2), "{\"currentPassword\":\"old-password\",\"newPassword\":\"short\"}")
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New password must be at least 8 characters"));

        verify(customerDao, never()).save(any());
    }

    @Test
    void adminResetsAnotherUsersPasswordWithoutTheCurrentOne() throws Exception {
        send(put("/customers/2/password").with(ADMIN_1), "{\"newPassword\":\"reset-password\"}")
                .andExpect(status().isOk());

        assertTrue(new BCryptPasswordEncoder().matches("reset-password", user.getPassword()));
    }

    @Test
    void usersCannotChangeSomeoneElsesPassword() throws Exception {
        send(put("/customers/1/password").with(USER_2), "{\"newPassword\":\"new-password\"}")
                .andExpect(status().isForbidden());
    }

    // Roles

    @Test
    void adminChangesARole() throws Exception {
        send(put("/customers/2/role").with(ADMIN_1), "{\"role\":\"admin\"}").andExpect(status().isOk());

        assertEquals("admin", user.getRole());
    }

    @Test
    void onlyKnownRolesAreAccepted() throws Exception {
        send(put("/customers/2/role").with(ADMIN_1), "{\"role\":\"superuser\"}").andExpect(status().isBadRequest());

        assertEquals("user", user.getRole());
    }

    @Test
    void adminCannotChangeTheirOwnRoleOrDisableThemselves() throws Exception {
        send(put("/customers/1/role").with(ADMIN_1), "{\"role\":\"user\"}").andExpect(status().isBadRequest());
        mvc.perform(delete("/customers/1").with(ADMIN_1)).andExpect(status().isBadRequest());

        assertEquals("admin", admin.getRole());
        assertFalse(admin.isDisabled());
    }

    @Test
    void usersCannotChangeRolesOrDisableAccounts() throws Exception {
        send(put("/customers/2/role").with(USER_2), "{\"role\":\"admin\"}").andExpect(status().isForbidden());
        mvc.perform(delete("/customers/2").with(USER_2)).andExpect(status().isForbidden());
        mvc.perform(put("/customers/2/enable").with(USER_2)).andExpect(status().isForbidden());
    }

    // Disabling

    @Test
    void disablingKeepsTheAccountAndHistoryButDropsOpenReservations() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Rental pending = rental(1, "Pending", now.plusDays(1));
        Rental upcoming = rental(2, "Approved", now.plusDays(2));
        Rental finished = rental(3, "Approved", now.minusDays(2));
        Rental rejected = rental(4, "Rejected", now.plusDays(1));
        when(rentalDao.findByCustomerId(2)).thenReturn(List.of(pending, upcoming, finished, rejected));

        mvc.perform(delete("/customers/2").with(ADMIN_1)).andExpect(status().isNoContent());

        assertTrue(user.isDisabled());
        verify(customerDao, never()).delete(any());
        verify(rentalDao).deleteAll(List.of(pending, upcoming));
        assertEquals("Available", upcoming.getEbike().getStatus());
        assertEquals("Not Available", finished.getEbike().getStatus());
    }

    @Test
    void adminCanEnableAnAccountAgain() throws Exception {
        user.setDisabled(true);

        mvc.perform(put("/customers/2/enable").with(ADMIN_1)).andExpect(status().isOk());

        assertFalse(user.isDisabled());
    }

    @Test
    void disabledAccountCannotLogIn() throws Exception {
        user.setDisabled(true);
        when(customerDao.findByEmailIgnoreCase("u@demo.local")).thenReturn(user);

        send(post("/customers/login"), "{\"email\":\"u@demo.local\",\"password\":\"old-password\"}")
                .andExpect(status().isForbidden())
                .andExpect(content().string("This account has been disabled"));
    }

    @Test
    void unknownAccountsReturnNotFound() throws Exception {
        send(put("/customers/99/role").with(ADMIN_1), "{\"role\":\"user\"}").andExpect(status().isNotFound());
        send(put("/customers/99/password").with(ADMIN_1), "{\"newPassword\":\"new-password\"}").andExpect(status().isNotFound());
        mvc.perform(put("/customers/99/enable").with(ADMIN_1)).andExpect(status().isNotFound());
    }

    private Rental rental(int id, String status, LocalDateTime end) {
        Ebike ebike = new Ebike();
        ebike.setStatus("Approved".equals(status) ? "Not Available" : "Available");
        Rental rental = new Rental();
        rental.setId(id);
        rental.setEbike(ebike);
        rental.setRentalStatus(status);
        rental.setRentalEndTime(end);
        return rental;
    }

    private ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
