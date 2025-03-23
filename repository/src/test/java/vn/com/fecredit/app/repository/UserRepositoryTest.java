package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest; // Add this import
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.entity.enums.RoleName;
import vn.com.fecredit.app.repository.config.TestConfig;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

//@DataJpaTest // Remove this annotation
@SpringBootTest // Add this annotation
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional // Add this annotation to ensure all test methods run in a transaction
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @SuppressWarnings("unused") // Field used for test data setup
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private User activeUser;
    private User inactiveUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM roles").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        adminRole = createAndSaveRole(RoleName.ADMIN, "Admin Role", 1);
        userRole = createAndSaveRole(RoleName.USER, "User Role", 2);

        activeUser = createAndSaveUser("active_user", "password123", "active@test.com", 
            "Active User", true);
        activeUser.addRole(adminRole);
        
        inactiveUser = createAndSaveUser("inactive_user", "password456", "inactive@test.com",
            "Inactive User", false);
        inactiveUser.addRole(userRole);

        entityManager.flush();
        entityManager.clear();
    }

    private Role createAndSaveRole(RoleName name, String description, int displayOrder) {
        Role role = Role.builder()
            .roleName(name)
            .description(description)
            .displayOrder(displayOrder)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .users(new HashSet<>())
            .build();
        entityManager.persist(role);
        return role;
    }

    private User createAndSaveUser(String username, String password, String email, 
            String fullName, boolean enabled) {
        User user = User.builder()
            .username(username)
            .password(password)
            .email(email)
            .fullName(fullName)
            .enabled(enabled)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .status(enabled ? CommonStatus.ACTIVE : CommonStatus.INACTIVE)
            .version(0L)
            .roles(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(user);
        return user;
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenNotExists() {
        var result = userRepository.findByUsername("nonexistent");
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        var result = userRepository.findByUsername("active_user");
        
        assertThat(result)
            .isPresent()
            .hasValueSatisfying(user -> {
                assertThat(user.getUsername()).isEqualTo("active_user");
                assertThat(user.getEmail()).isEqualTo("active@test.com");
                assertThat(user.isEnabled()).isTrue();
                assertThat(user.getRoles())
                    .extracting("roleName")
                    .containsExactly(RoleName.ADMIN);
            });
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        var result = userRepository.findByEmail("active@test.com");
        
        assertThat(result)
            .isPresent()
            .hasValueSatisfying(user -> {
                assertThat(user.getUsername()).isEqualTo("active_user");
                assertThat(user.isEnabled()).isTrue();
            });
    }

    @Test
    void findActiveUsersByRole_ShouldReturnCorrectUsers() {
        var adminUsers = userRepository.findActiveUsersByRole(RoleName.ADMIN);
        assertThat(adminUsers)
            .hasSize(1)
            .extracting("username")
            .containsExactly("active_user");

        var normalUsers = userRepository.findActiveUsersByRole(RoleName.USER);
        assertThat(normalUsers).isEmpty();
    }

    @Test
    void hasRole_ShouldReturnCorrectResult() {
        assertThat(userRepository.hasRole(activeUser.getId(), RoleName.ADMIN)).isTrue();
        assertThat(userRepository.hasRole(activeUser.getId(), RoleName.USER)).isFalse();
        assertThat(userRepository.hasRole(inactiveUser.getId(), RoleName.ADMIN)).isFalse();
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenExists() {
        assertThat(userRepository.existsByUsername("active_user")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        assertThat(userRepository.existsByEmail("active@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@test.com")).isFalse();
    }
}