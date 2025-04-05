package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;

@Rollback
class BlacklistedTokenRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private BlacklistedToken validToken;
    private BlacklistedToken expiredToken;

    @BeforeEach
    void setUp() {
        // Clear existing data
        blacklistedTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        entityManager.flush();

        // Create test user
        LocalDateTime now = LocalDateTime.now();
        testUser = User.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .fullName("Test User")
                .enabled(true)
                .role(RoleType.ROLE_USER)
                .status(CommonStatus.ACTIVE)
                .build();
        // Add these required audit fields
        testUser.setCreatedAt(now);
        testUser.setUpdatedAt(now);
        testUser.setCreatedBy("system");
        testUser.setUpdatedBy("system");
        testUser = userRepository.save(testUser);

        // Create test tokens
        validToken = BlacklistedToken.builder()
                .token("valid-token-123")
                .tokenType("ACCESS")
                .user(testUser)
                .expirationTime(now.plusDays(1))
                .status(CommonStatus.ACTIVE)
                .build();
        validToken.setCreatedAt(now);
        validToken.setUpdatedAt(now);
        validToken.setCreatedBy("system");
        validToken.setUpdatedBy("system");

        expiredToken = BlacklistedToken.builder()
                .token("expired-token-456")
                .tokenType("ACCESS")
                .user(testUser)
                .expirationTime(now.minusDays(1))
                .status(CommonStatus.ACTIVE)
                .build();
        expiredToken.setCreatedAt(now.minusDays(2));
        expiredToken.setUpdatedAt(now.minusDays(2));
        expiredToken.setCreatedBy("system");
        expiredToken.setUpdatedBy("system");

        blacklistedTokenRepository.save(validToken);
        blacklistedTokenRepository.save(expiredToken);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testPersistAndRetrieve() {
        // Given
        Long tokenId = validToken.getId();

        // When
        BlacklistedToken found = blacklistedTokenRepository.findById(tokenId).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getToken()).isEqualTo("valid-token-123");
        assertThat(found.getTokenType()).isEqualTo("ACCESS");
        assertThat(found.getUser()).isNotNull();
        assertThat(found.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void testIsTokenBlacklisted() {
        // When
        boolean isValidTokenBlacklisted = blacklistedTokenRepository.existsByTokenAndStatus(
                "valid-token-123", CommonStatus.ACTIVE);
        boolean isUnknownTokenBlacklisted = blacklistedTokenRepository.existsByTokenAndStatus(
                "unknown-token", CommonStatus.ACTIVE);

        // Then
        assertThat(isValidTokenBlacklisted).isTrue();
        assertThat(isUnknownTokenBlacklisted).isFalse();
    }

    @Test
    void testFindAllValidTokens() {
        // When
        List<BlacklistedToken> validTokens = blacklistedTokenRepository.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertThat(validTokens).hasSize(2);
    }

    @Test
    void testFindValidToken() {
        // When
        BlacklistedToken token = blacklistedTokenRepository.findByToken("valid-token-123").orElse(null);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getExpirationTime()).isAfter(LocalDateTime.now());
    }

    @Test
    void testDeleteExpiredTokens() {
        // When
        int deletedCount = blacklistedTokenRepository.deleteExpiredTokens(LocalDateTime.now());

        // Then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(blacklistedTokenRepository.count()).isEqualTo(1);
    }
}
