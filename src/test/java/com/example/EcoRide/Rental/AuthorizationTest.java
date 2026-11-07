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
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Uses real signed tokens, so signature checks and role rules run exactly as they do in production. */
@WebMvcTest
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=test-secret-test-secret-test-secret-1234")
class AuthorizationTest {

    private static final String RESERVATION = """
            {"customer":{"id":1},"ebike":{"id":3},"passportNumber":"X1",
             "rentalStartTime":"2026-12-01T10:00","rentalEndTime":"2026-12-01T12:00"}""";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JwtEncoder jwtEncoder;

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

    @Test
    void anyoneCanBrowseBikesAndStations() throws Exception {
        mvc.perform(get("/ebikes")).andExpect(status().isOk());
        mvc.perform(get("/ebikes/searchByModelAndStatus?model=X&status=Available")).andExpect(status().isOk());
        mvc.perform(get("/stations")).andExpect(status().isOk());
    }

    @Test
    void noTokenIsRejectedEverywhereElse() throws Exception {
        mvc.perform(get("/customers")).andExpect(status().isUnauthorized());
        mvc.perform(get("/customers/2")).andExpect(status().isUnauthorized());
        mvc.perform(json(put("/customers/2"), "{}")).andExpect(status().isUnauthorized());
        mvc.perform(get("/rentals")).andExpect(status().isUnauthorized());
        mvc.perform(get("/rentals/pending")).andExpect(status().isUnauthorized());
        mvc.perform(json(post("/rentals/pending"), RESERVATION)).andExpect(status().isUnauthorized());
        mvc.perform(put("/rentals/approve/1")).andExpect(status().isUnauthorized());
        mvc.perform(json(post("/ebikes"), "{}")).andExpect(status().isUnauthorized());
        mvc.perform(get("/payments")).andExpect(status().isUnauthorized());
    }

    @Test
    void normalUserCannotDoAdminWork() throws Exception {
        String user = token("2", "user");
        mvc.perform(as(get("/customers"), user)).andExpect(status().isForbidden());
        mvc.perform(as(delete("/customers/3"), user)).andExpect(status().isForbidden());
        mvc.perform(as(get("/rentals"), user)).andExpect(status().isForbidden());
        mvc.perform(as(get("/rentals/pending"), user)).andExpect(status().isForbidden());
        mvc.perform(as(put("/rentals/approve/1"), user)).andExpect(status().isForbidden());
        mvc.perform(as(put("/rentals/reject/1"), user)).andExpect(status().isForbidden());
        mvc.perform(as(json(post("/ebikes"), "{}"), user)).andExpect(status().isForbidden());
        mvc.perform(as(json(post("/stations"), "{}"), user)).andExpect(status().isForbidden());
        mvc.perform(as(get("/payments"), user)).andExpect(status().isForbidden());
    }

    @Test
    void usersCanOnlyTouchTheirOwnAccount() throws Exception {
        String user = token("2", "user");
        when(customerDao.findById(2)).thenReturn(Optional.of(new Customer(2, "U", "u@demo.local", "1", 0, "hash", "user")));
        when(customerDao.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));
        String body = "{\"name\":\"U\",\"email\":\"u@demo.local\",\"phone\":\"1\",\"password\":\"new-password\"}";

        mvc.perform(as(get("/customers/2"), user)).andExpect(status().isOk());
        mvc.perform(as(json(put("/customers/2"), body), user)).andExpect(status().isOk());

        // The password takeover: another customer's account must stay out of reach.
        mvc.perform(as(get("/customers/3"), user)).andExpect(status().isForbidden());
        mvc.perform(as(json(put("/customers/3"), body), user)).andExpect(status().isForbidden());
    }

    @Test
    void adminCanApproveAndListCustomers() throws Exception {
        Rental rental = new Rental();
        rental.setEbike(new Ebike());
        when(rentalDao.findById(1)).thenReturn(Optional.of(rental));
        String admin = token("1", "admin");

        mvc.perform(as(put("/rentals/approve/1"), admin)).andExpect(status().isOk());
        mvc.perform(as(get("/customers"), admin)).andExpect(status().isOk());
        mvc.perform(as(get("/customers/2"), admin)).andExpect(status().isOk());
    }

    @Test
    void forgedAndGarbageTokensAreRejected() throws Exception {
        var otherKey = new SecretKeySpec("a-different-key-a-different-key-1234".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        String forged = sign(new NimbusJwtEncoder(new ImmutableSecret<>(otherKey)), "1", "admin", Instant.now().plusSeconds(600));

        mvc.perform(as(get("/customers"), forged)).andExpect(status().isUnauthorized());
        mvc.perform(as(get("/customers"), "not-a-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        // Spring allows 60 seconds of clock difference, so expire well before that.
        String expired = sign(jwtEncoder, "1", "admin", Instant.now().minusSeconds(120));

        mvc.perform(as(get("/customers"), expired)).andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsATokenThatWorks() throws Exception {
        String hash = new BCryptPasswordEncoder(4).encode("demo1234");
        when(customerDao.findByEmailIgnoreCase("kiksa@admin.com"))
                .thenReturn(new Customer(1, "A", "kiksa@admin.com", "1", 0, hash, "admin"));

        String response = mvc.perform(json(post("/customers/login"), "{\"email\":\"kiksa@admin.com\",\"password\":\"demo1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRole").value("admin"))
                .andExpect(jsonPath("$.token").isString())
                .andReturn().getResponse().getContentAsString();

        String token = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("token").asText();
        mvc.perform(as(get("/customers"), token)).andExpect(status().isOk());
    }

    @Test
    void wrongPasswordGetsNoToken() throws Exception {
        String hash = new BCryptPasswordEncoder(4).encode("demo1234");
        when(customerDao.findByEmailIgnoreCase("kiksa@admin.com"))
                .thenReturn(new Customer(1, "A", "kiksa@admin.com", "1", 0, hash, "admin"));

        mvc.perform(json(post("/customers/login"), "{\"email\":\"kiksa@admin.com\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(containsString("Invalid")));
    }

    @Test
    void registeringTheOldAdminEmailDoesNotGrantAdmin() throws Exception {
        mvc.perform(json(post("/customers/register"), "{\"name\":\"E\",\"email\":\"kiksa@admin.com\",\"password\":\"pw12345\",\"phone\":\"1\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Customer> saved = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).save(saved.capture());
        assertEquals("user", saved.getValue().getRole());
    }

    @Test
    void reservationUsesTheCustomerFromTheToken() throws Exception {
        Customer tokenOwner = new Customer(2, "U", "u@demo.local", "1", 0, "hash", "user");
        when(customerDao.findById(2)).thenReturn(Optional.of(tokenOwner));
        when(ebikeDao.findById(3)).thenReturn(Optional.of(new Ebike()));

        // The body names customer 1, but the token belongs to customer 2.
        mvc.perform(as(json(post("/rentals/pending"), RESERVATION), token("2", "user"))).andExpect(status().isCreated());

        ArgumentCaptor<Rental> saved = ArgumentCaptor.forClass(Rental.class);
        verify(rentalDao).save(saved.capture());
        assertSame(tokenOwner, saved.getValue().getCustomer());
    }

    @Test
    void reservationMustEndAfterItStarts() throws Exception {
        String backwards = """
                {"ebike":{"id":3},"passportNumber":"X1",
                 "rentalStartTime":"2026-12-01T12:00","rentalEndTime":"2026-12-01T10:00"}""";

        mvc.perform(as(json(post("/rentals/pending"), backwards), token("2", "user"))).andExpect(status().isBadRequest());

        verify(rentalDao, never()).save(any());
    }

    private String token(String subject, String role) {
        return sign(jwtEncoder, subject, role, Instant.now().plusSeconds(600));
    }

    private String sign(JwtEncoder encoder, String subject, String role, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(expiresAt.minusSeconds(3600))
                .expiresAt(expiresAt)
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
