package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Test class for the BlacklistedToken entity.
 */
class BlacklistedTokenTest {

    private BlacklistedToken blacklistedToken;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .username("testuser")
            .email("test@example.com")
            .status(CommonStatus.ACTIVE)
            .build();

        blacklistedToken = BlacklistedToken.builder()
            .token("test.jwt.token")
            .tokenType("access")
            .user(user)
            .status(CommonStatus.ACTIVE)
            .build();
    }

    @Test
    void testExpiration() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusMinutes(30);
        LocalDateTime past = now.minusMinutes(1);

        blacklistedToken.setExpirationTime(future);
        assertFalse(blacklistedToken.isExpired(now));

        blacklistedToken.setExpirationTime(past);
        assertTrue(blacklistedToken.isExpired(now));
    }

    @Test
    void testTokenValidation() {
        BlacklistedToken invalidToken = BlacklistedToken.builder()
            .token("")
            .tokenType("access")
            .user(user)
            .status(CommonStatus.ACTIVE)
            .build();

        assertThrows(IllegalStateException.class, () -> invalidToken.validateState());

        invalidToken.setToken("test.jwt.token");
        invalidToken.setTokenType("");
        assertThrows(IllegalStateException.class, () -> invalidToken.validateState());
    }

    @Test
    void testExpirationTimeSetting() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedToken.setExpirationTime(now.plusMinutes(30));
        
        assertFalse(blacklistedToken.isExpired(now));
        assertTrue(blacklistedToken.isExpired(now.plusMinutes(31)));
    }
}
