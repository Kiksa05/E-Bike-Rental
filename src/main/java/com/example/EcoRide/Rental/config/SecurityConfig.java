package com.example.EcoRide.Rental.config;

import com.example.EcoRide.Rental.dao.CustomerDao;
import com.example.EcoRide.Rental.models.Customer;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = "admin";

    private final SecretKey key;

    // No default on purpose: a known signing key would let anyone forge an admin token.
    public SecurityConfig(@Value("${app.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
        }
        key = new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        WebExpressionAuthorizationManager ownerOrAdmin =
                new WebExpressionAuthorizationManager("hasRole('admin') or #id == authentication.name");

        return http
                // Tokens are sent by hand in a header, not automatically like cookies, so CSRF does not apply.
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR stateless JWT in Authorization header, no cookies
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Without this, a 400 from a controller would turn into a 401 on its way to the error page.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/customers/login", "/customers/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ebikes", "/ebikes/**", "/stations", "/stations/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/rentals/pending").authenticated()
                        .requestMatchers(HttpMethod.GET, "/rentals/mine").authenticated()
                        // Owner-or-admin is checked in the controller, since the owner is stored on the rental.
                        .requestMatchers(HttpMethod.PUT, "/rentals/{id}/cancel").authenticated()
                        .requestMatchers(HttpMethod.GET, "/customers/{id}").access(ownerOrAdmin)
                        .requestMatchers(HttpMethod.PUT, "/customers/{id}", "/customers/{id}/password").access(ownerOrAdmin)
                        .anyRequest().hasRole(ADMIN))
                .oauth2ResourceServer(server -> server.jwt(jwt -> jwt.jwtAuthenticationConverter(roleConverter())))
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder jwtDecoder(CustomerDao customerDao) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), jwt -> {
            // Checked on every request, so disabling an account or changing a role takes effect at once.
            Customer customer = parseId(jwt.getSubject()).flatMap(customerDao::findById).orElse(null);
            boolean valid = customer != null && !customer.isDisabled()
                    && Objects.equals(customer.getRole(), jwt.getClaimAsString("role"));
            return valid ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Account is disabled or has changed", null));
        }));
        return decoder;
    }

    private static Optional<Integer> parseId(String subject) {
        try {
            return Optional.of(Integer.parseInt(subject));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private JwtAuthenticationConverter roleConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
