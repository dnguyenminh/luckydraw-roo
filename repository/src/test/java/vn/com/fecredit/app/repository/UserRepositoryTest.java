package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;

class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private User adminUser, regularUser, inactiveUser;
    private Role adminRole, userRole;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        // Remove this line as the table doesn't exist anymore
        // entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
        
        // Keep the other delete statements
        entityManager.createNativeQuery("DELETE FROM blacklisted_tokens").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM roles").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        // Create roles with proper RoleType enum
        adminRole = Role.builder()
            .roleType(RoleType.ROLE_ADMIN) // Use enum not String
            .description("Administrator")
            .displayOrder(1)
            .status(CommonStatus.ACTIVE)
            .permissions(new HashSet<>())
            .build();
        adminRole.setVersion(0L);
        adminRole.setCreatedBy("system");
        adminRole.setUpdatedBy("system");
        adminRole.setCreatedAt(now);
        adminRole.setUpdatedAt(now);
        adminRole = roleRepository.save(adminRole);

        userRole = Role.builder()
            .roleType(RoleType.ROLE_USER) // Use enum not String
            .description("Regular User")
            .displayOrder(2)
            .status(CommonStatus.ACTIVE)
            .permissions(new HashSet<>())
            .build();
        userRole.setVersion(0L);
        userRole.setCreatedBy("system");
        userRole.setUpdatedBy("system");
        userRole.setCreatedAt(now);
        userRole.setUpdatedAt(now);
        userRole = roleRepository.save(userRole);

        // Create users with RoleType enum
        adminUser = User.builder()
            .username("admin")
            .password("$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS") // bcrypt hash for 'password'
            .email("admin@example.com")
            .fullName("Admin User")
            .status(CommonStatus.ACTIVE)
            .build();
        adminUser.setVersion(0L);
        adminUser.setCreatedBy("system");
        adminUser.setUpdatedBy("system");
        adminUser.setCreatedAt(now);
        adminUser.setUpdatedAt(now);
        adminUser = userRepository.save(adminUser);
        adminUser.setRole(adminRole);

        regularUser = User.builder()
            .username("user")
            .password("$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS")
            .email("user@example.com")
            .fullName("Regular User")
            .status(CommonStatus.ACTIVE)
            .build();
        regularUser.setVersion(0L);
        regularUser.setCreatedBy("system");
        regularUser.setUpdatedBy("system");
        regularUser.setCreatedAt(now);
        regularUser.setUpdatedAt(now);
        regularUser = userRepository.save(regularUser);
        regularUser.setRole(userRole);

        inactiveUser = User.builder()
            .username("inactive")
            .password("$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS")
            .email("inactive@example.com")
            .fullName("Inactive User")
            .status(CommonStatus.INACTIVE)
            .build();
        inactiveUser.setVersion(0L);
        inactiveUser.setCreatedBy("system");
        inactiveUser.setUpdatedBy("system");
        inactiveUser.setCreatedAt(now);
        inactiveUser.setUpdatedAt(now);
        inactiveUser = userRepository.save(inactiveUser);
        inactiveUser.setRole(userRole);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        Optional<User> result = userRepository.findByUsername("admin");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Admin User");
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        Optional<User> result = userRepository.findByEmail("user@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("user");
    }

    @Test
    void findByStatus_ShouldReturnFilteredUsers() {
        List<User> activeUsers = userRepository.findByStatus(CommonStatus.ACTIVE);
        List<User> inactiveUsers = userRepository.findByStatus(CommonStatus.INACTIVE);

        assertThat(activeUsers).hasSize(2);
        assertThat(inactiveUsers).hasSize(1);
        assertThat(inactiveUsers.get(0).getUsername()).isEqualTo("inactive");
    }

    @Test
    void findByUsernameContaining_ShouldReturnMatchingUsers() {
        List<User> usersWithUser = userRepository.findByUsernameContainingIgnoreCase("user");

        assertThat(usersWithUser).hasSize(1);
        assertThat(usersWithUser.get(0).getUsername()).isEqualTo("user");
    }

    @Test
    void existsByUsername_ShouldReturnTrueWhenExists() {
        boolean exists = userRepository.existsByUsername("admin");
        boolean notExists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void existsByEmail_ShouldReturnTrueWhenExists() {
        boolean exists = userRepository.existsByEmail("admin@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

}
