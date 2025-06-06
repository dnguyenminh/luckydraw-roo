package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
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
        // cleanDatabase();
        createTestData();
    }

    // private void cleanDatabase() {
    //     // Delete in the correct order to respect foreign key constraints
    //     entityManager.createNativeQuery("DELETE FROM blacklisted_tokens").executeUpdate();
    //     entityManager.createNativeQuery("DELETE FROM role_permissions").executeUpdate();
    //     entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
    //     entityManager.createNativeQuery("DELETE FROM roles").executeUpdate();
    //     entityManager.flush();
    // }

    private void createTestData() {
        // Try to find existing roles, or create new ones if not found
        Optional<Role> adminRoleOpt = roleRepository.findByRoleType(RoleType.ROLE_ADMIN);
        if (adminRoleOpt.isPresent()) {
            adminRole = adminRoleOpt.get();
        } else {
            // Create admin role if it doesn't exist
            adminRole = Role.builder()
                .roleType(RoleType.ROLE_ADMIN)
                .description("System Administrator")
                .displayOrder(1)
                .status(CommonStatus.ACTIVE)
                .build();
            adminRole.setVersion(0L);
            adminRole.setCreatedBy("system");
            adminRole.setUpdatedBy("system");
            adminRole.setCreatedAt(now);
            adminRole.setUpdatedAt(now);
            adminRole = roleRepository.save(adminRole);
        }
            
        Optional<Role> userRoleOpt = roleRepository.findByRoleType(RoleType.ROLE_USER);
        if (userRoleOpt.isPresent()) {
            userRole = userRoleOpt.get();
        } else {
            // Create user role if it doesn't exist
            userRole = Role.builder()
                .roleType(RoleType.ROLE_USER)
                .description("Regular User")
                .displayOrder(2)
                .status(CommonStatus.ACTIVE)
                .build();
            userRole.setVersion(0L);
            userRole.setCreatedBy("system");
            userRole.setUpdatedBy("system");
            userRole.setCreatedAt(now);
            userRole.setUpdatedAt(now);
            userRole = roleRepository.save(userRole);
        }

        // Create users with unique usernames to avoid conflicts
        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(6);
        
        adminUser = User.builder()
            .username("admin_test_" + uniqueSuffix)
            .password("$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS") // bcrypt hash for 'password'
            .email("admin_test_" + uniqueSuffix + "@example.com")
            .fullName("Admin Test User")
            .role(adminRole)
            .status(CommonStatus.ACTIVE)
            .build();
        adminUser.setVersion(0L);
        adminUser.setCreatedBy("system");
        adminUser.setUpdatedBy("system");
        adminUser.setCreatedAt(now);
        adminUser.setUpdatedAt(now);
        adminUser = userRepository.save(adminUser);

        regularUser = User.builder()
            .username("user_test_" + uniqueSuffix)
            .password("$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS")
            .email("user_test_" + uniqueSuffix + "@example.com")
            .fullName("Regular Test User")
            .role(userRole)
            .status(CommonStatus.ACTIVE)
            .build();
        regularUser.setVersion(0L);
        regularUser.setCreatedBy("system");
        regularUser.setUpdatedBy("system");
        regularUser.setCreatedAt(now);
        regularUser.setUpdatedAt(now);
        regularUser = userRepository.save(regularUser);

        inactiveUser = User.builder()
            .username("inactive_test_" + uniqueSuffix)
            .password("$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS")
            .email("inactive_test_" + uniqueSuffix + "@example.com")
            .fullName("Inactive Test User")
            .role(userRole)
            .status(CommonStatus.INACTIVE)
            .build();
        inactiveUser.setVersion(0L);
        inactiveUser.setCreatedBy("system");
        inactiveUser.setUpdatedBy("system");
        inactiveUser.setCreatedAt(now);
        inactiveUser.setUpdatedAt(now);
        inactiveUser = userRepository.save(inactiveUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        Optional<User> result = userRepository.findByUsername(adminUser.getUsername());

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo(adminUser.getFullName());
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        Optional<User> result = userRepository.findByEmail(regularUser.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(regularUser.getUsername());
    }

    @Test
    void findByStatus_ShouldReturnFilteredUsers() {
        List<User> activeUsers = userRepository.findByStatus(CommonStatus.ACTIVE);
        List<User> inactiveUsers = userRepository.findByStatus(CommonStatus.INACTIVE);

        assertThat(activeUsers).hasSizeGreaterThanOrEqualTo(2);
        assertThat(inactiveUsers).hasSizeGreaterThanOrEqualTo(1);
        assertThat(inactiveUsers).anyMatch(user -> user.getUsername().equals(inactiveUser.getUsername()));
    }

    @Test
    void findByUsernameContaining_ShouldReturnMatchingUsers() {
        String uniquePart = regularUser.getUsername().substring(5, 10); // Extract a unique part of the test username
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(uniquePart);

        assertThat(users).anyMatch(user -> user.getUsername().equals(regularUser.getUsername()));
    }

    @Test
    void existsByUsername_ShouldReturnTrueWhenExists() {
        boolean exists = userRepository.existsByUsername(adminUser.getUsername());
        boolean notExists = userRepository.existsByUsername("nonexistent_user_" + System.currentTimeMillis());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void existsByEmail_ShouldReturnTrueWhenExists() {
        boolean exists = userRepository.existsByEmail(adminUser.getEmail());
        boolean notExists = userRepository.existsByEmail("nonexistent_" + System.currentTimeMillis() + "@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
