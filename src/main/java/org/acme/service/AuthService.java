package org.acme.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.dto.request.LoginRequest;
import org.acme.dto.response.TokenResponse;
import org.acme.exception.AuthenticationException;
import org.acme.security.SecurityRoles;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;

@ApplicationScoped
public class AuthService {

    public static final String ISSUER = "minibanco-api";
    public static final long TOKEN_EXPIRATION_SECONDS = 3600;

    private static final byte[] DUMMY_PASSWORD = "invalid-password".getBytes(StandardCharsets.UTF_8);
    private static final Map<String, TestUser> TEST_USERS = Map.of(
            "customer", new TestUser("customer123", SecurityRoles.CUSTOMER),
            "admin", new TestUser("admin123", SecurityRoles.ADMIN)
    );

    public TokenResponse login(LoginRequest request) {
        TestUser user = TEST_USERS.get(request.username());
        byte[] expectedPassword = user == null
                ? DUMMY_PASSWORD
                : user.password().getBytes(StandardCharsets.UTF_8);
        byte[] suppliedPassword = request.password().getBytes(StandardCharsets.UTF_8);

        if (user == null || !MessageDigest.isEqual(expectedPassword, suppliedPassword)) {
            throw new AuthenticationException();
        }

        String token = Jwt.issuer(ISSUER)
                .upn(request.username())
                .groups(user.role())
                .expiresIn(Duration.ofSeconds(TOKEN_EXPIRATION_SECONDS))
                .sign();
        return new TokenResponse(token, "Bearer", TOKEN_EXPIRATION_SECONDS);
    }

    private record TestUser(String password, String role) {
    }
}
