package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.RoleType;
import vn.com.fecredit.app.repository.config.TestConfig;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class BlacklistedTokenRepositoryTest {

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("DELETE FROM blacklisted_tokens").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.flush();
    }

    private User createAndSaveUser() {
User user = new User();
user.setUsername("testUser");
user.setPassword("testPassword");
user.setEmail("test@example.com");
user.setRole(RoleType.ROLE_USER);
user.setActive(true);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    /**
     * Creates a token with the expiration time safely set for testing
     * When we need to test with "expired" tokens, we'll set expirationTime in the future
     * but check against a timestamp that's even further in the future
     */
    private BlacklistedToken createToken(User user, String token, String type, LocalDateTime expirationTime,
            CommonStatus status) {
        // Ensure expiration time is always in the future to pass validation
        LocalDateTime safeExpirationTime = LocalDateTime.now().plusSeconds(1);
        if (expirationTime.isAfter(safeExpirationTime)) {
            safeExpirationTime = expirationTime;
        }
        
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setTokenType(type);
        blacklistedToken.setUser(user);
        blacklistedToken.setExpirationTime(safeExpirationTime);
        blacklistedToken.setStatus(status);
        blacklistedToken.setCreatedAt(LocalDateTime.now());
        blacklistedToken.setUpdatedAt(LocalDateTime.now());
        blacklistedToken.setCreatedBy("testUser");
        blacklistedToken.setUpdatedBy("testUser");
        blacklistedToken.setVersion(0L);
        return blacklistedTokenRepository.save(blacklistedToken);
    }

    @Test
    void testPersistAndRetrieve() {
        User user = createAndSaveUser();
        createToken(user, "testToken", "testType", LocalDateTime.now().plusDays(1),
                CommonStatus.ACTIVE);
        Optional<BlacklistedToken> found = blacklistedTokenRepository.findByToken("testToken");
        assertThat(found).isPresent();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getCreatedBy()).isEqualTo("testUser");
        assertThat(found.get().getUpdatedAt()).isNotNull();
        assertThat(found.get().getUpdatedBy()).isEqualTo("testUser");
        assertThat(found.get().getUser()).isEqualTo(user);
    }

    @Test
    void testFindValidToken() {
        User user = createAndSaveUser();
        
        // Create a "valid" token (not expired)
        BlacklistedToken token = createToken(user, "validToken", "validType", 
                LocalDateTime.now().plusDays(1), CommonStatus.ACTIVE);
        entityManager.flush();

        // Should find it with current time
        Optional<BlacklistedToken> found = blacklistedTokenRepository.findValidToken("validToken", "validType",
                LocalDateTime.now());
        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isEqualTo(user);

        // Shouldn't find it with a timestamp after its expiration
        Optional<BlacklistedToken> notFound = blacklistedTokenRepository.findValidToken("validToken", "validType",
                token.getExpirationTime().plusSeconds(1));
        assertThat(notFound).isNotPresent();

        // Create a token that will be treated as "expired" (although its expiration is slightly in the future)
        BlacklistedToken shortExpiryToken = createToken(user, "expiredToken", "expiredType",
                LocalDateTime.now().plusSeconds(2), CommonStatus.ACTIVE);
        entityManager.flush();

        // Check at a time after its expiration to simulate checking an expired token
        Optional<BlacklistedToken> foundExpired = blacklistedTokenRepository.findValidToken("expiredToken",
                "expiredType", shortExpiryToken.getExpirationTime().plusSeconds(1));
        assertThat(foundExpired).isNotPresent();

        // Test inactive token behavior
        createToken(user, "inactiveToken", "inactiveType",
                LocalDateTime.now().plusDays(1), CommonStatus.INACTIVE);
        entityManager.flush();

        Optional<BlacklistedToken> foundInactive = blacklistedTokenRepository.findValidToken("inactiveToken",
                "inactiveType", LocalDateTime.now());
        assertThat(foundInactive).isNotPresent();
    }

    @Test
    void testIsTokenBlacklisted() {
        User user = createAndSaveUser();
        createToken(user, "blacklistedToken", "blacklistedType",
                LocalDateTime.now().plusDays(1), CommonStatus.ACTIVE);
        entityManager.flush();

        boolean isBlacklisted = blacklistedTokenRepository.isTokenBlacklisted("blacklistedToken", "blacklistedType",
                LocalDateTime.now());
        assertThat(isBlacklisted).isTrue();

        boolean isNotBlacklisted = blacklistedTokenRepository.isTokenBlacklisted("notBlacklistedToken",
                "notBlacklistedType", LocalDateTime.now());
        assertThat(isNotBlacklisted).isFalse();

        boolean isExpiredBlacklisted = blacklistedTokenRepository.isTokenBlacklisted("blacklistedToken",
                "blacklistedType", LocalDateTime.now().plusDays(2));
        assertThat(isExpiredBlacklisted).isFalse();
    }

    @Test
    void testFindAllValidTokens() {
        User user1 = createAndSaveUser();
        
User user2 = new User();
user2.setUsername("testUser2");
user2.setPassword("testPassword");
user2.setEmail("test2@example.com");
user2.setRole(RoleType.ROLE_USER);
user2.setActive(true);
        entityManager.persist(user2);
        entityManager.flush();

        // Create valid token (not expired)
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        BlacklistedToken token1 = new BlacklistedToken();
        token1.setToken("token1");
        token1.setTokenType("type1");
        token1.setUser(user1);
        token1.setExpirationTime(futureTime);
        token1.setStatus(CommonStatus.ACTIVE);
        token1.setCreatedAt(LocalDateTime.now());
        token1.setUpdatedAt(LocalDateTime.now());
        token1.setCreatedBy("testUser");
        token1.setUpdatedBy("testUser");
        token1.setVersion(0L);
        blacklistedTokenRepository.save(token1);

        // Create a token with shorter validity period
        LocalDateTime shortExpiryTime = LocalDateTime.now().plusSeconds(2);
        BlacklistedToken token2 = new BlacklistedToken();
        token2.setToken("token2");
        token2.setTokenType("type2");
        token2.setUser(user2);
        token2.setExpirationTime(shortExpiryTime);
        token2.setStatus(CommonStatus.ACTIVE);
        token2.setCreatedAt(LocalDateTime.now());
        token2.setUpdatedAt(LocalDateTime.now());
        token2.setCreatedBy("testUser");
        token2.setUpdatedBy("testUser");
        token2.setVersion(0L);
        blacklistedTokenRepository.save(token2);
        entityManager.flush();

        // Test with a timestamp that's after token2's expiration
        LocalDateTime checkTime = shortExpiryTime.plusSeconds(1);
        List<BlacklistedToken> validTokens = blacklistedTokenRepository.findAllValidTokens(checkTime);
        assertThat(validTokens).hasSize(1);
        assertThat(validTokens.get(0).getToken()).isEqualTo("token1");
        assertThat(validTokens.get(0).getUser().getId()).isNotNull();
    }

    @Test
    void testDeleteExpiredTokens() {
        User user1 = createAndSaveUser();
        
User user2 = new User();
user2.setUsername("testUser2");
user2.setPassword("testPassword");
user2.setEmail("test2@example.com");
user2.setRole(RoleType.ROLE_USER);
user2.setActive(true);
        entityManager.persist(user2);
        entityManager.flush();

        // Create a token with short expiry
        LocalDateTime shortExpiryTime = LocalDateTime.now().plusSeconds(2);
        BlacklistedToken token1 = new BlacklistedToken();
        token1.setToken("token1");
        token1.setTokenType("type1");
        token1.setUser(user1);
        token1.setExpirationTime(shortExpiryTime); 
        token1.setStatus(CommonStatus.ACTIVE);
        token1.setCreatedAt(LocalDateTime.now());
        token1.setUpdatedAt(LocalDateTime.now());
        token1.setCreatedBy("testUser");
        token1.setUpdatedBy("testUser");
        token1.setVersion(0L);
        blacklistedTokenRepository.save(token1);

        // Create a token with longer expiry
        LocalDateTime longExpiryTime = LocalDateTime.now().plusDays(1);
        BlacklistedToken token2 = new BlacklistedToken();
        token2.setToken("token2");
        token2.setTokenType("type2");
        token2.setUser(user2);
        token2.setExpirationTime(longExpiryTime);
        token2.setStatus(CommonStatus.ACTIVE);
        token2.setCreatedAt(LocalDateTime.now());
        token2.setUpdatedAt(LocalDateTime.now());
        token2.setCreatedBy("testUser");
        token2.setUpdatedBy("testUser");
        token2.setVersion(0L);
        blacklistedTokenRepository.save(token2);
        entityManager.flush();

        // Delete tokens that are expired as of a timestamp after token1's expiration
        LocalDateTime checkTime = shortExpiryTime.plusSeconds(1);
        blacklistedTokenRepository.deleteExpiredTokens(checkTime);

        List<BlacklistedToken> allTokens = blacklistedTokenRepository.findAll();
        assertThat(allTokens).hasSize(1);
        assertThat(allTokens.get(0).getToken()).isEqualTo("token2");
        assertThat(allTokens.get(0).getUser().getId()).isNotNull();
    }
}
