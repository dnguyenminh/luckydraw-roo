package vn.com.fecredit.app.controller.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mock JWT token provider for testing purposes.
 * Creates valid JWT tokens for use in controller tests.
 */
@Component
public class MockJwtTokenProvider {

    private final String secretKey = "test-secret-key-that-is-long-enough-for-hs256-algorithm-requirement-minimum-256-bits";

    /**
     * Create a mock JWT token for the given username and role
     *
     * @param username The user's username
     * @param role The user's role
     * @return A valid JWT token
     */
    public String createToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        long now = System.currentTimeMillis();
        long validity = now + 3600000; // 1 hour

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(validity))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
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
