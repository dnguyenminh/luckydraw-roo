package vn.com.fecredit.app.controller.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mock implementation of JwtTokenProvider for testing purposes.
 * This is a standalone implementation that doesn't extend JwtTokenProvider.
 */
@Component
public class MockJwtTokenProvider {

    // private final String secretKey = "mock-secret-key-for-testing-purposes-only";

    public String createToken(String username, String roles) {
        return "mock-jwt-token";
    }

    public String getUsername(String token) {
        return "test-user";
    }

    public String getRoles(String token) {
        return "ROLE_USER";
    }

    public Date getExpiration(String token) {
        return new Date(System.currentTimeMillis() + 3600000);
    }

    public boolean validateToken(String token) {
        return true;
    }

    public Authentication getAuthentication(String token) {
        // Create authorities from the roles string
        List<SimpleGrantedAuthority> authorities = Arrays.stream("ROLE_USER".split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(
                "test-user",
                "",
                authorities);
    }

    public String resolveToken(HttpServletRequest req) {
        return "mock-token";
    }
}
