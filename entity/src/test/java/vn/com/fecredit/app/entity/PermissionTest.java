package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;

class PermissionTest {

    private Permission permission;
    private Role role;

    @BeforeEach
    void setUp() {
        permission = Permission.builder()
                .name("test_permission")
                .description("Test permission description")
                .status(CommonStatus.ACTIVE)
                .build();
                
        role = Role.builder()
                .roleType(RoleType.ROLE_ADMIN)
                .description("Admin role")
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void testAddRole() {
        // When
        permission.addRole(role);
        
        // Then
        assertTrue(permission.getRoles().contains(role));
        assertTrue(role.getPermissions().contains(permission));
    }

    @Test
    void testRemoveRole() {
        // Given
        permission.addRole(role);
        assertTrue(permission.getRoles().contains(role));
        assertTrue(role.getPermissions().contains(permission));
        
        // When
        permission.removeRole(role);
        
        // Then
        assertFalse(permission.getRoles().contains(role));
        assertFalse(role.getPermissions().contains(permission));
    }

    @Test
    void testValidateState_WithNullName_ShouldThrowException() {
        // Given
        permission.setName(null);
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> permission.validateState());
    }

    @Test
    void testValidateState_WithEmptyName_ShouldThrowException() {
        // Given
        permission.setName("  ");
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> permission.validateState());
    }

    @Test
    void testValidateState_ShouldConvertNameToUppercase() {
        // Given
        permission.setName("test_permission");
        
        // When
        permission.validateState();
        
        // Then
        assertEquals("TEST_PERMISSION", permission.getName());
    }
    
    @Test
    void testValidateState_WithNullRoles_ShouldInitializeRoles() {
        // Given
        permission.setRoles(null);
        
        // When
        permission.validateState();
        
        // Then
        assertNotNull(permission.getRoles());
        assertTrue(permission.getRoles().isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        Permission permission1 = Permission.builder()
                .name("READ_DATA")
                .description("Permission to read data")
                .status(CommonStatus.ACTIVE)
                .build();
        
        Permission permission2 = Permission.builder()
                .name("READ_DATA")
                .description("Different description")
                .status(CommonStatus.INACTIVE)
                .build();
        
        Permission permission3 = Permission.builder()
                .name("WRITE_DATA")
                .description("Permission to write data")
                .status(CommonStatus.ACTIVE)
                .build();
        
        // Then - permissions with same name should be equal regardless of other fields
        assertEquals(permission1, permission2, "Permissions with same name should be equal");
        assertNotEquals(permission1, permission3, "Permissions with different names should not be equal");
        assertEquals(permission1.hashCode(), permission2.hashCode(), "Hash codes should be equal for equal permissions");
        assertNotEquals(permission1.hashCode(), permission3.hashCode(), "Hash codes should differ for different permissions");
    }
}
