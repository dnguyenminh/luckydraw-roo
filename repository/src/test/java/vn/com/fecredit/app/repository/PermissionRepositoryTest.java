package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;


@Rollback
class PermissionRepositoryTest  extends AbstractRepositoryTest{

    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private Permission viewUserPermission;
    private Permission createUserPermission;
    private Permission viewEventPermission;
    private Role adminRole;
    private Role userRole;
    private final LocalDateTime now = LocalDateTime.now();
    
    @BeforeEach
    void setUp() {
        // Clean up database
        entityManager.createNativeQuery("DELETE FROM role_permissions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM permissions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM roles").executeUpdate();
        entityManager.flush();
        
        // Create test permissions
        viewUserPermission = Permission.builder()
                .name("VIEW_USERS")
                .description("Permission to view users")
                .status(CommonStatus.ACTIVE)
                .build();
        viewUserPermission.setCreatedBy("system");
        viewUserPermission.setUpdatedBy("system");
        viewUserPermission.setCreatedAt(now);
        viewUserPermission.setUpdatedAt(now);
        
        createUserPermission = Permission.builder()
                .name("CREATE_USER")
                .description("Permission to create users")
                .status(CommonStatus.ACTIVE)
                .build();
        createUserPermission.setCreatedBy("system");
        createUserPermission.setUpdatedBy("system");
        createUserPermission.setCreatedAt(now);
        createUserPermission.setUpdatedAt(now);
        
        viewEventPermission = Permission.builder()
                .name("VIEW_EVENTS")
                .description("Permission to view events")
                .status(CommonStatus.INACTIVE)
                .build();
        viewEventPermission.setCreatedBy("system");
        viewEventPermission.setUpdatedBy("system");
        viewEventPermission.setCreatedAt(now);
        viewEventPermission.setUpdatedAt(now);
        
        permissionRepository.save(viewUserPermission);
        permissionRepository.save(createUserPermission);
        permissionRepository.save(viewEventPermission);
        
        // Create test roles
        adminRole = Role.builder()
                .roleType(RoleType.ROLE_ADMIN)
                .description("Administrator role")
                .displayOrder(1)
                .status(CommonStatus.ACTIVE)
                .build();
        adminRole.setCreatedBy("system");
        adminRole.setUpdatedBy("system");
        adminRole.setCreatedAt(now);
        adminRole.setUpdatedAt(now);
        
        userRole = Role.builder()
                .roleType(RoleType.ROLE_USER)
                .description("User role")
                .displayOrder(2)
                .status(CommonStatus.ACTIVE)
                .build();
        userRole.setCreatedBy("system");
        userRole.setUpdatedBy("system");
        userRole.setCreatedAt(now);
        userRole.setUpdatedAt(now);
        
        roleRepository.save(adminRole);
        roleRepository.save(userRole);
        
        // Establish relationships
        adminRole.addPermission(viewUserPermission);
        adminRole.addPermission(createUserPermission);
        userRole.addPermission(viewUserPermission);
        
        roleRepository.save(adminRole);
        roleRepository.save(userRole);
        
        entityManager.flush();
        entityManager.clear();
    }
    
    @Test
    void findByName_ShouldReturnMatchingPermission() {
        // When
        Optional<Permission> result = permissionRepository.findByName("VIEW_USERS");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Permission to view users");
        assertThat(result.get().getRoles()).hasSize(2);
    }
    
    @Test
    void existsByName_ShouldReturnCorrectResult() {
        // When & Then
        assertThat(permissionRepository.existsByName("VIEW_USERS")).isTrue();
        assertThat(permissionRepository.existsByName("UNKNOWN_PERMISSION")).isFalse();
    }
    
    @Test
    void findByStatus_ShouldReturnMatchingPermissions() {
        // When
        List<Permission> activePermissions = permissionRepository.findByStatus(CommonStatus.ACTIVE);
        List<Permission> inactivePermissions = permissionRepository.findByStatus(CommonStatus.INACTIVE);
        
        // Then
        assertThat(activePermissions).hasSize(2);
        assertThat(activePermissions).extracting("name").containsExactlyInAnyOrder("VIEW_USERS", "CREATE_USER");
        
        assertThat(inactivePermissions).hasSize(1);
        assertThat(inactivePermissions).extracting("name").containsExactly("VIEW_EVENTS");
    }
    
    @Test
    void findByRoleId_ShouldReturnAssignedPermissions() {
        // When
        List<Permission> adminPermissions = permissionRepository.findByRoleId(adminRole.getId());
        List<Permission> userPermissions = permissionRepository.findByRoleId(userRole.getId());
        
        // Then
        assertThat(adminPermissions).hasSize(2);
        assertThat(adminPermissions).extracting("name").containsExactlyInAnyOrder("VIEW_USERS", "CREATE_USER");
        
        assertThat(userPermissions).hasSize(1);
        assertThat(userPermissions).extracting("name").containsExactly("VIEW_USERS");
    }
    
    @Test
    void findActiveByRoleId_ShouldReturnOnlyActivePermissions() {
        // Given
        // Make VIEW_USERS permission inactive
        Permission permission = permissionRepository.findByName("VIEW_USERS").orElseThrow();
        permission.setStatus(CommonStatus.INACTIVE);
        permissionRepository.save(permission);
        
        entityManager.flush();
        entityManager.clear();
        
        // When
        List<Permission> activeAdminPermissions = permissionRepository.findActiveByRoleId(adminRole.getId());
        
        // Then
        assertThat(activeAdminPermissions).hasSize(1);
        assertThat(activeAdminPermissions).extracting("name").containsExactly("CREATE_USER");
    }
    
    @Test
    void findNotInRole_ShouldReturnUnassignedPermissions() {
        // When
        List<Permission> notInUserRole = permissionRepository.findNotInRole(userRole.getId());
        
        // Then
        assertThat(notInUserRole).hasSize(2);
        assertThat(notInUserRole).extracting("name").containsExactlyInAnyOrder("CREATE_USER", "VIEW_EVENTS");
    }
    
    @Test
    void basicCrudOperations_ShouldWorkCorrectly() {
        // Count
        long count = permissionRepository.count();
        assertThat(count).isEqualTo(3);
        
        // Find by ID
        Permission found = permissionRepository.findById(viewUserPermission.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("VIEW_USERS");
        
        // Create new permission
        Permission newPermission = Permission.builder()
                .name("DELETE_USER")
                .description("Permission to delete users")
                .status(CommonStatus.ACTIVE)
                .build();
        newPermission.setCreatedBy("system");
        newPermission.setUpdatedBy("system");
        newPermission.setCreatedAt(now);
        newPermission.setUpdatedAt(now);
        
        Permission saved = permissionRepository.save(newPermission);
        assertThat(saved.getId()).isNotNull();
        
        // Update permission
        found.setDescription("Updated description");
        permissionRepository.save(found);
        
        entityManager.flush();
        entityManager.clear();
        
        Permission updated = permissionRepository.findById(found.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        
        // Delete permission
        permissionRepository.deleteById(viewEventPermission.getId());
        
        entityManager.flush();
        entityManager.clear();
        
        boolean exists = permissionRepository.existsById(viewEventPermission.getId());
        assertThat(exists).isFalse();
        
        // Count after operations
        long newCount = permissionRepository.count();
        assertThat(newCount).isEqualTo(3); // Added one and deleted one
    }
}