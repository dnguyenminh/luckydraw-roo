package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.repository.config.TestConfig;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BlacklistedTokenRepositoryTest {

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private User user;
    private BlacklistedToken validToken;
    private BlacklistedToken expiredToken;
    private BlacklistedToken inactiveToken;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM blacklisted_tokens").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        user = createAndSaveUser();
        
        validToken = createAndSaveToken("valid-token", "ACCESS", 
            now.plusHours(1), user, CommonStatus.ACTIVE);
            
        // Create expired token using native SQL to bypass validation
        var futureToken = createAndSaveToken("expired-token", "ACCESS",
            now.plusHours(1), user, CommonStatus.ACTIVE);
        entityManager.flush();
            
        entityManager.createNativeQuery(
            "UPDATE blacklisted_tokens SET expiration_time = :expirationTime " +
            "WHERE token = :token")
            .setParameter("expirationTime", now.minusHours(1))
            .setParameter("token", "expired-token")
            .executeUpdate();
        
        expiredToken = blacklistedTokenRepository.findByToken("expired-token").orElseThrow();
            
        inactiveToken = createAndSaveToken("inactive-token", "REFRESH",
            now.plusHours(1), user, CommonStatus.INACTIVE);

        entityManager.flush();
        entityManager.clear();
    }

    private User createAndSaveUser() {
        User user = User.builder()
            .username("test-user")
            .password("password")
            .email("test@example.com")
            .fullName("Test User")
            .enabled(true)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("system")
            .updatedBy("system")
            .build();
        entityManager.persist(user);
        return user;
    }

    private BlacklistedToken createAndSaveToken(
            String token, String tokenType, LocalDateTime expirationTime, 
            User user, CommonStatus status) {
        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
            .token(token)
            .tokenType(tokenType)
            .expirationTime(expirationTime)
            .user(user)
            .status(status)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(blacklistedToken);
        return blacklistedToken;
    }

    @Test
    void findByToken_ShouldReturnToken_WhenExists() {
        var result = blacklistedTokenRepository.findByToken("valid-token");
        
        assertThat(result)
            .isPresent()
            .hasValueSatisfying(token -> {
                assertThat(token.getToken()).isEqualTo("valid-token");
                assertThat(token.getTokenType()).isEqualTo("ACCESS");
                assertThat(token.getExpirationTime()).isAfter(now);
            });
    }

    @Test
    void findValidToken_ShouldReturnToken_WhenValid() {
        var result = blacklistedTokenRepository.findValidToken(
            "valid-token", "ACCESS", now);
            
        assertThat(result).isPresent();
        
        result = blacklistedTokenRepository.findValidToken(
            "expired-token", "ACCESS", now);
        assertThat(result).isEmpty();
        
        result = blacklistedTokenRepository.findValidToken(
            "inactive-token", "REFRESH", now);
        assertThat(result).isEmpty();
    }

    @Test
    void isTokenBlacklisted_ShouldReturnTrue_WhenTokenIsValid() {
        assertThat(blacklistedTokenRepository.isTokenBlacklisted(
            "valid-token", "ACCESS", now)).isTrue();
            
        assertThat(blacklistedTokenRepository.isTokenBlacklisted(
            "expired-token", "ACCESS", now)).isFalse();
            
        assertThat(blacklistedTokenRepository.isTokenBlacklisted(
            "inactive-token", "REFRESH", now)).isFalse();
            
        assertThat(blacklistedTokenRepository.isTokenBlacklisted(
            "nonexistent-token", "ACCESS", now)).isFalse();
    }

    @Test
    void findByTokenType_ShouldReturnFilteredTokens() {
        var accessTokens = blacklistedTokenRepository.findByTokenType("ACCESS");
        assertThat(accessTokens).hasSize(2);
        
        var refreshTokens = blacklistedTokenRepository.findByTokenType("REFRESH");
        assertThat(refreshTokens).hasSize(1);
    }

    @Test
    void findByUserId_ShouldReturnUserTokens() {
        var tokens = blacklistedTokenRepository.findByUserId(user.getId());
        assertThat(tokens).hasSize(3);
    }

    @Test
    @Transactional
    void deleteExpiredTokens_ShouldRemoveExpiredTokens() {
        blacklistedTokenRepository.deleteExpiredTokens(now);
        entityManager.flush();
        entityManager.clear();
        
        var remainingTokens = blacklistedTokenRepository.findAll();
        assertThat(remainingTokens)
            .hasSize(2)
            .extracting("token")
            .containsExactlyInAnyOrder("valid-token", "inactive-token");
    }
}