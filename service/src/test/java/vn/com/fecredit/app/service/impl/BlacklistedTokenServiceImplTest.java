package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.repository.BlacklistedTokenRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistedTokenServiceImplTest {

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @InjectMocks
    private BlacklistedTokenServiceImpl blacklistedTokenService;

    private BlacklistedToken validToken;
    private BlacklistedToken expiredToken;
    private BlacklistedToken inactiveToken;
    private User testUser;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status(CommonStatus.ACTIVE)
                .build();

        validToken = BlacklistedToken.builder()
                .id(1L)
                .token("valid-token-123")
                .tokenType("ACCESS")
                .user(testUser)
                .expirationTime(now.plusHours(1))
                .status(CommonStatus.ACTIVE)
                .build();

        expiredToken = BlacklistedToken.builder()
                .id(2L)
                .token("expired-token-456")
                .tokenType("ACCESS")
                .user(testUser)
                .expirationTime(now.minusHours(1))
                .status(CommonStatus.ACTIVE)
                .build();

        inactiveToken = BlacklistedToken.builder()
                .id(3L)
                .token("inactive-token-789")
                .tokenType("REFRESH")
                .user(testUser)
                .expirationTime(now.plusHours(1))
                .status(CommonStatus.INACTIVE)
                .build();
    }

    @Test
    void findByToken_ShouldReturnToken_WhenExists() {
        // Given
        when(blacklistedTokenRepository.findByToken("valid-token-123")).thenReturn(Optional.of(validToken));

        // When
        Optional<BlacklistedToken> result = blacklistedTokenService.findByToken("valid-token-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("valid-token-123", result.get().getToken());
        verify(blacklistedTokenRepository).findByToken("valid-token-123");
    }

    @Test
    void findByTokenType_ShouldReturnTokens() {
        // Given
        when(blacklistedTokenRepository.findByTokenType("ACCESS"))
                .thenReturn(Arrays.asList(validToken, expiredToken));

        // When
        List<BlacklistedToken> result = blacklistedTokenService.findByTokenType("ACCESS");

        // Then
        assertEquals(2, result.size());
        verify(blacklistedTokenRepository).findByTokenType("ACCESS");
    }

    @Test
    void findByUserId_ShouldReturnTokens() {
        // Given
        Long userId = 1L;
        when(blacklistedTokenRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(validToken, expiredToken, inactiveToken));

        // When
        List<BlacklistedToken> result = blacklistedTokenService.findByUserId(userId);

        // Then
        assertEquals(3, result.size());
        verify(blacklistedTokenRepository).findByUserId(userId);
    }

    @Test
    void findValidToken_ShouldReturnToken_WhenValid() {
        // Given
        String token = "valid-token-123";
        String tokenType = "ACCESS";
        when(blacklistedTokenRepository.findValidToken(token, tokenType, now))
                .thenReturn(Optional.of(validToken));

        // When
        Optional<BlacklistedToken> result = blacklistedTokenService.findValidToken(token, tokenType, now);

        // Then
        assertTrue(result.isPresent());
        assertEquals("valid-token-123", result.get().getToken());
        verify(blacklistedTokenRepository).findValidToken(token, tokenType, now);
    }

    @Test
    void findValidToken_ShouldReturnEmpty_WhenTokenExpired() {
        // Given
        String token = "expired-token-456";
        String tokenType = "ACCESS";
        when(blacklistedTokenRepository.findValidToken(token, tokenType, now))
                .thenReturn(Optional.empty());

        // When
        Optional<BlacklistedToken> result = blacklistedTokenService.findValidToken(token, tokenType, now);

        // Then
        assertTrue(result.isEmpty());
        verify(blacklistedTokenRepository).findValidToken(token, tokenType, now);
    }

    @Test
    void findAllValidTokens_ShouldReturnValidTokens() {
        // Given
        when(blacklistedTokenRepository.findAllValidTokens(now))
                .thenReturn(Collections.singletonList(validToken));

        // When
        List<BlacklistedToken> result = blacklistedTokenService.findAllValidTokens(now);

        // Then
        assertEquals(1, result.size());
        assertEquals("valid-token-123", result.get(0).getToken());
        verify(blacklistedTokenRepository).findAllValidTokens(now);
    }

    @Test
    void isTokenBlacklisted_ShouldReturnTrue_WhenTokenIsBlacklisted() {
        // Given
        String token = "valid-token-123";
        String tokenType = "ACCESS";
        when(blacklistedTokenRepository.isTokenBlacklisted(token, tokenType, now))
                .thenReturn(true);

        // When
        boolean result = blacklistedTokenService.isTokenBlacklisted(token, tokenType, now);

        // Then
        assertTrue(result);
        verify(blacklistedTokenRepository).isTokenBlacklisted(token, tokenType, now);
    }

    @Test
    void isTokenBlacklisted_ShouldReturnFalse_WhenTokenIsNotBlacklisted() {
        // Given
        String token = "non-blacklisted-token";
        String tokenType = "ACCESS";
        when(blacklistedTokenRepository.isTokenBlacklisted(token, tokenType, now))
                .thenReturn(false);

        // When
        boolean result = blacklistedTokenService.isTokenBlacklisted(token, tokenType, now);

        // Then
        assertFalse(result);
        verify(blacklistedTokenRepository).isTokenBlacklisted(token, tokenType, now);
    }

    @Test
    void deleteExpiredTokens_ShouldCallRepository() {
        // Given
        LocalDateTime expirationTime = now.minusHours(1);

        // When
        blacklistedTokenService.deleteExpiredTokens(expirationTime);

        // Then
        verify(blacklistedTokenRepository).deleteExpiredTokens(expirationTime);
    }

    @Test
    void findByStatus_ShouldReturnFilteredTokens() {
        // Given
        when(blacklistedTokenRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Arrays.asList(validToken, expiredToken));

        // When
        List<BlacklistedToken> result = blacklistedTokenService.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertEquals(2, result.size());
        verify(blacklistedTokenRepository).findByStatus(CommonStatus.ACTIVE);
    }
}
