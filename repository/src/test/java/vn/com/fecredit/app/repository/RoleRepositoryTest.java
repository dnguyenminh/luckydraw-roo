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
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.entity.enums.RoleName;
import vn.com.fecredit.app.repository.config.TestConfig;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Role adminRole;
    private Role userRole;
    private Role inactiveRole;

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
        adminRole = createAndSaveRole(RoleName.ADMIN, "Admin Role", 1, CommonStatus.ACTIVE);
        userRole = createAndSaveRole(RoleName.USER, "User Role", 2, CommonStatus.ACTIVE);
        inactiveRole = createAndSaveRole(RoleName.PARTICIPANT, "Participant Role", 3, CommonStatus.INACTIVE);

        entityManager.flush();
        entityManager.clear();
    }

    private Role createAndSaveRole(RoleName name, String description, int displayOrder, CommonStatus status) {
        Role role = Role.builder()
            .roleName(name)
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
        entityManager.persist(role);
        return role;
    }

    @Test
    void findByRoleName_ShouldReturnRole_WhenExists() {
        var result = roleRepository.findByRoleName(RoleName.ADMIN);
        
        assertThat(result)
            .isPresent()
            .hasValueSatisfying(role -> {
                assertThat(role.getRoleName()).isEqualTo(RoleName.ADMIN);
                assertThat(role.getDescription()).isEqualTo("Admin Role");
                assertThat(role.getDisplayOrder()).isEqualTo(1);
            });
    }

    @Test
    void findByRoleName_ShouldReturnEmpty_WhenNotExists() {
        var result = roleRepository.findByRoleName(RoleName.MANAGER);
        assertThat(result).isEmpty();
    }

    @Test
    void findByStatus_ShouldReturnFilteredRoles() {
        var activeRoles = roleRepository.findByStatus(CommonStatus.ACTIVE);
        assertThat(activeRoles)
            .hasSize(2)
            .extracting("roleName")
            .containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.USER);

        var inactiveRoles = roleRepository.findByStatus(CommonStatus.INACTIVE);
        assertThat(inactiveRoles)
            .hasSize(1)
            .extracting("roleName")
            .containsExactly(RoleName.PARTICIPANT);
    }

    @Test
    void findByStatusOrderByDisplayOrderAsc_ShouldReturnSortedRoles() {
        var activeRoles = roleRepository.findByStatusOrderByDisplayOrderAsc(CommonStatus.ACTIVE);
        assertThat(activeRoles)
            .hasSize(2)
            .extracting("roleName")
            .containsExactly(RoleName.ADMIN, RoleName.USER);
    }

    @Test
    void existsByRoleName_ShouldReturnTrue_WhenExists() {
        assertThat(roleRepository.existsByRoleName(RoleName.ADMIN)).isTrue();
        assertThat(roleRepository.existsByRoleName(RoleName.MANAGER)).isFalse();
    }
}