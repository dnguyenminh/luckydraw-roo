package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;

@Rollback // Add this annotation to ensure changes are rolled back
class RoleRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    // Inject EntityManager directly in the test class
    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();

    @SuppressWarnings("unused") // These fields are set up for test data but not directly referenced
    private Role adminRole;

    @SuppressWarnings("unused")
    private Role userRole;

    @SuppressWarnings("unused")
    private Role inactiveRole;

    @BeforeEach
    void setUp() {
        try {
            cleanDatabase();
            createTestData();
        } catch (Exception e) {
            // Log but don't fail the test, we'll see the actual failure in the test itself
            System.err.println("Error during test setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cleanDatabase() {
        try {
            // Try to execute the delete statements, but don't fail if tables don't exist
            // yet
            try {
                entityManager.createNativeQuery("DELETE FROM role_permissions").executeUpdate();
            } catch (Exception e) {
                // Table might not exist yet, that's ok
            }

            try {
                entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
            } catch (Exception e) {
                // Table might not exist yet, that's ok
            }

            try {
                entityManager.createNativeQuery("DELETE FROM roles").executeUpdate();
            } catch (Exception e) {
                // Table might not exist yet, that's ok
            }

            entityManager.flush();
        } catch (Exception e) {
            // Log the error but continue - the test will fail naturally if setup fails
            System.err.println("Error during database cleanup: " + e.getMessage());
        }
    }

    private void createTestData() {
        adminRole = createAndSaveRole(RoleType.ROLE_ADMIN, "Admin Role", 1, CommonStatus.ACTIVE);
        userRole = createAndSaveRole(RoleType.ROLE_USER, "User Role", 2, CommonStatus.ACTIVE);
        inactiveRole = createAndSaveRole(RoleType.ROLE_PARTICIPANT, "Participant Role", 3, CommonStatus.INACTIVE);

        entityManager.flush();
        entityManager.clear();
    }

    private Role createAndSaveRole(RoleType name, String description, int displayOrder, CommonStatus status) {
        Role role = Role.builder()
                .roleType(name)
                .description(description)
                .displayOrder(displayOrder)
                .status(status)
                .version(0L)
                .users(new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        // Instead of using entityManager directly, use the repository
        return roleRepository.save(role);
    }

    @Test
    void findByRoleName_ShouldReturnRole_WhenExists() {
        // Given
        Role adminRole = roleRepository.findByRoleType(RoleType.ROLE_ADMIN).orElse(null);
        assertThat(adminRole).isNotNull(); // Use the variable to avoid the warning
        entityManager.flush();

        // When
        var result = roleRepository.findByRoleType(RoleType.ROLE_ADMIN);

        // Then
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(role -> {
                    assertThat(role.getRoleType()).isEqualTo(RoleType.ROLE_ADMIN);
                    assertThat(role.getDescription()).isEqualTo("Admin Role");
                    assertThat(role.getDisplayOrder()).isEqualTo(1);
                });
    }

    @Test
    void findByRoleName_ShouldReturnEmpty_WhenNotExists() {
        var result = roleRepository.findByRoleType(RoleType.ROLE_MANAGER);
        assertThat(result).isEmpty();
    }

    @Test
    void findByStatus_ShouldReturnFilteredRoles() {
        var activeRoles = roleRepository.findByStatus(CommonStatus.ACTIVE);
        assertThat(activeRoles)
                .hasSize(2)
                .extracting("roleType") // Changed from "roleName" to "roleType"
                .containsExactlyInAnyOrder(RoleType.ROLE_ADMIN, RoleType.ROLE_USER);

        var inactiveRoles = roleRepository.findByStatus(CommonStatus.INACTIVE);
        assertThat(inactiveRoles)
                .hasSize(1)
                .extracting("roleType") // Changed from "roleName" to "roleType"
                .containsExactly(RoleType.ROLE_PARTICIPANT);
    }

    @Test
    void findByStatusOrderByDisplayOrderAsc_ShouldReturnSortedRoles() {
        var activeRoles = roleRepository.findByStatusOrderByDisplayOrderAsc(CommonStatus.ACTIVE);
        assertThat(activeRoles)
                .hasSize(2)
                .extracting("roleType") // Changed from "roleName" to "roleType"
                .containsExactly(RoleType.ROLE_ADMIN, RoleType.ROLE_USER);
    }

    @Test
    void existsByRoleName_ShouldReturnTrue_WhenExists() {
        assertThat(roleRepository.existsByRoleType(RoleType.ROLE_ADMIN)).isTrue();
        assertThat(roleRepository.existsByRoleType(RoleType.ROLE_MANAGER)).isFalse();
    }
}