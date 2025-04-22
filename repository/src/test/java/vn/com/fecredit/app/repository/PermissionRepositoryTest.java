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
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.PermissionName;
import vn.com.fecredit.app.entity.enums.PermissionType;

class PermissionRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Permission createUserPermission, readUserPermission, updateUserPermission;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM role_permissions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM permissions").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        // Create permissions with proper PermissionName enum values
        createUserPermission = Permission.builder()
            .name(PermissionName.CREATE_USER)  // Set the required name field
            .description("Create User Permission")
            .type(PermissionType.WRITE)  // Use enum instead of String
            .status(CommonStatus.ACTIVE)
            .build();
        createUserPermission.setVersion(0L);
        createUserPermission.setCreatedBy("system");
        createUserPermission.setUpdatedBy("system");
        createUserPermission.setCreatedAt(now);
        createUserPermission.setUpdatedAt(now);
        createUserPermission = permissionRepository.save(createUserPermission);
        
        readUserPermission = Permission.builder()
            .name(PermissionName.READ_USER)  // Set the required name field
            .description("Read User Permission")
            .type(PermissionType.READ)  // Use enum instead of String
            .status(CommonStatus.ACTIVE)
            .build();
        readUserPermission.setVersion(0L);
        readUserPermission.setCreatedBy("system");
        readUserPermission.setUpdatedBy("system");
        readUserPermission.setCreatedAt(now);
        readUserPermission.setUpdatedAt(now);
        readUserPermission = permissionRepository.save(readUserPermission);
        
        updateUserPermission = Permission.builder()
            .name(PermissionName.UPDATE_USER)  // Set the required name field
            .description("Update User Permission")
            .type(PermissionType.WRITE)  // Use enum instead of String
            .status(CommonStatus.INACTIVE)
            .build();
        updateUserPermission.setVersion(0L);
        updateUserPermission.setCreatedBy("system");
        updateUserPermission.setUpdatedBy("system");
        updateUserPermission.setCreatedAt(now);
        updateUserPermission.setUpdatedAt(now);
        updateUserPermission = permissionRepository.save(updateUserPermission);
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByName_ShouldReturnPermission_WhenExists() {
        Optional<Permission> result = permissionRepository.findByName(PermissionName.CREATE_USER);
        
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Create User Permission");
    }
    
    @Test
    void findByStatus_ShouldReturnFilteredPermissions() {
        List<Permission> activePermissions = permissionRepository.findByStatus(CommonStatus.ACTIVE);
        List<Permission> inactivePermissions = permissionRepository.findByStatus(CommonStatus.INACTIVE);
        
        assertThat(activePermissions).hasSize(2);
        assertThat(inactivePermissions).hasSize(1);
        assertThat(inactivePermissions.get(0).getName()).isEqualTo(PermissionName.UPDATE_USER);
    }
    
    @Test
    void findByType_ShouldReturnFilteredPermissions() {
        List<Permission> writePermissions = permissionRepository.findByType(PermissionType.WRITE);  // Use enum instead of String
        List<Permission> readPermissions = permissionRepository.findByType(PermissionType.READ);     // Use enum instead of String
        
        assertThat(writePermissions).hasSize(2);
        assertThat(readPermissions).hasSize(1);
        assertThat(readPermissions.get(0).getName()).isEqualTo(PermissionName.READ_USER);
    }
}
