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
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;
import vn.com.fecredit.app.entity.enums.PermissionName;

class RoleRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Role adminRole, userRole, inactiveRole;
    private Permission createPermission, readPermission;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        // Remove this line as the table doesn't exist anymore
        // entityManager.createNativeQuery("DELETE FROM user_roles").executeUpdate();
        
        // Keep the other delete statements
        entityManager.createNativeQuery("DELETE FROM role_permissions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM permissions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM roles").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        // Create permissions
        createPermission = Permission.builder()
            .name(PermissionName.CREATE_USER)  // Add required name field
            .description("Create permission")
            .status(CommonStatus.ACTIVE)
            .build();
        createPermission.setVersion(0L);
        createPermission.setCreatedBy("system");
        createPermission.setUpdatedBy("system");
        createPermission.setCreatedAt(now);
        createPermission.setUpdatedAt(now);
        createPermission = permissionRepository.save(createPermission);

        readPermission = Permission.builder()
            .name(PermissionName.READ_USER)    // Add required name field
            .description("Read permission")
            .status(CommonStatus.ACTIVE)
            .build();
        readPermission.setVersion(0L);
        readPermission.setCreatedBy("system");
        readPermission.setUpdatedBy("system");
        readPermission.setCreatedAt(now);
        readPermission.setUpdatedAt(now);
        readPermission = permissionRepository.save(readPermission);

        // Create roles with RoleType enum
        adminRole = Role.builder()
            .roleType(RoleType.ROLE_ADMIN) // Use enum not String
            .description("Administrator")
            .displayOrder(1)
            .status(CommonStatus.ACTIVE)
            .permissions(new HashSet<>())
            .build();
        adminRole.setCreatedBy("system");
        adminRole.setUpdatedBy("system");
        adminRole.setCreatedAt(now);
        adminRole.setUpdatedAt(now);
        adminRole = roleRepository.save(adminRole);
        adminRole.getPermissions().add(createPermission);
        adminRole.getPermissions().add(readPermission);

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
        userRole.getPermissions().add(readPermission);

        inactiveRole = Role.builder()
            .roleType(RoleType.ROLE_GUEST) // Use enum not String
            .description("Guest User")
            .displayOrder(3)
            .status(CommonStatus.INACTIVE)
            .permissions(new HashSet<>())
            .build();
        inactiveRole.setVersion(0L);
        inactiveRole.setCreatedBy("system");
        inactiveRole.setUpdatedBy("system");
        inactiveRole.setCreatedAt(now);
        inactiveRole.setUpdatedAt(now);
        inactiveRole = roleRepository.save(inactiveRole);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByRoleType_ShouldReturnRole_WhenExists() {
        Optional<Role> result = roleRepository.findByRoleType(RoleType.ROLE_ADMIN); // Use enum

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Administrator");
    }

    @Test
    void findByStatus_ShouldReturnFilteredRoles() {
        List<Role> activeRoles = roleRepository.findByStatus(CommonStatus.ACTIVE);
        List<Role> inactiveRoles = roleRepository.findByStatus(CommonStatus.INACTIVE);

        assertThat(activeRoles).hasSize(2);
        assertThat(inactiveRoles).hasSize(1);
        assertThat(inactiveRoles.get(0).getRoleType()).isEqualTo(RoleType.ROLE_GUEST);
    }

    @Test
    void findByDisplayOrderLessThan_ShouldReturnFilteredRoles() {
        List<Role> roles = roleRepository.findByDisplayOrderLessThanOrderByDisplayOrderAsc(3);

        assertThat(roles).hasSize(2);
        assertThat(roles).extracting("roleType").containsExactlyInAnyOrder(
            RoleType.ROLE_ADMIN, RoleType.ROLE_USER);
    }

    @Test
    void findByPermissionsContains_ShouldReturnRolesWithPermission() {
        List<Role> rolesWithCreate = roleRepository.findByPermissionsContaining(createPermission);
        List<Role> rolesWithRead = roleRepository.findByPermissionsContaining(readPermission);

        assertThat(rolesWithCreate).hasSize(1);
        assertThat(rolesWithCreate.get(0).getRoleType()).isEqualTo(RoleType.ROLE_ADMIN);

        assertThat(rolesWithRead).hasSize(2);
        assertThat(rolesWithRead).extracting("roleType").containsExactlyInAnyOrder(
            RoleType.ROLE_ADMIN, RoleType.ROLE_USER);
    }

    @Test
    void existsByRoleType_ShouldReturnTrueWhenExists() {
        boolean exists = roleRepository.existsByRoleType(RoleType.ROLE_ADMIN); // Use enum
        boolean notExists = roleRepository.existsByRoleType(RoleType.ROLE_PARTICIPANT); // Use enum

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        long activeCount = roleRepository.countByStatus(CommonStatus.ACTIVE);
        long inactiveCount = roleRepository.countByStatus(CommonStatus.INACTIVE);

        assertThat(activeCount).isEqualTo(2);
        assertThat(inactiveCount).isEqualTo(1);
    }

    @Test
    void findByStatusOrderByDisplayOrderAsc_ShouldReturnOrderedRoles() {
        List<Role> orderedRoles = roleRepository.findByStatusOrderByDisplayOrderAsc(CommonStatus.ACTIVE);

        assertThat(orderedRoles).hasSize(2);
        assertThat(orderedRoles.get(0).getRoleType()).isEqualTo(RoleType.ROLE_ADMIN); // Order 1
        assertThat(orderedRoles.get(1).getRoleType()).isEqualTo(RoleType.ROLE_USER);  // Order 2
    }
}
