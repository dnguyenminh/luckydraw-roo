package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.PermissionName;
import vn.com.fecredit.app.entity.enums.RoleType;

/**
 * Unit tests for the Role entity.
 * Tests the business logic and entity relationships of roles.
 */
class RoleTest {

    private Role role;
    private Permission permission;
    private User user;

    @BeforeEach
    void setUp() {
        // Create test roles
        role = Role.builder()
                .roleType(RoleType.ROLE_ADMIN)
                .description("Admin role")
                .status(CommonStatus.ACTIVE)
                .build();

        // Create a test permission
        permission = Permission.builder()
                .name(PermissionName.CREATE_USER)
                .description("Test permission")
                .status(CommonStatus.ACTIVE)
                .build();

        // Create a test user
        user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void testAddPermission_ShouldEstablishBidirectionalRelationship() {
        // When
        role.addPermission(permission);

        // Then
        assertTrue(role.getPermissions().contains(permission));
        assertTrue(permission.getRoles().contains(role));
    }

    @Test
    void testRemovePermission_ShouldRemoveBidirectionalRelationship() {
        // Given
        role.addPermission(permission);
        assertTrue(role.getPermissions().contains(permission));
        assertTrue(permission.getRoles().contains(role));

        // When
        role.removePermission(permission);

        // Then
        assertFalse(role.getPermissions().contains(permission));
        assertFalse(permission.getRoles().contains(role));
    }

    @Test
    void testAddUser_ShouldEstablishBidirectionalRelationship() {
        // When
        role.addUser(user);

        // Then
        assertTrue(role.getUsers().contains(user));
        assertTrue(user.getRole().equals(role));
    }

    @Test
    void testRemoveUser_ShouldRemoveBidirectionalRelationship() {
        // Given
        role.addUser(user);

        // When
        role.removeUser(user);

        // Then
        assertFalse(role.getUsers().contains(user));
        assertNull(user.getRole());
    }

    @Test
    void testHasPermission() {
        // Given
        permission.setName(PermissionName.CREATE_CONFIGURATION);
        role.addPermission(permission);

        // When & Then
        assertTrue(role.hasPermission(PermissionName.CREATE_CONFIGURATION));
        assertFalse(role.hasPermission(PermissionName.CREATE_EVENT));


        // Test with inactive permission
        permission.setStatus(CommonStatus.INACTIVE);
        assertFalse(role.hasPermission(PermissionName.CREATE_CONFIGURATION));
    }

    @Test
    void testHasPermission_WithNonexistentPermission_ShouldReturnFalse() {
        // When & Then
        assertFalse(role.hasPermission(null));
    }

    @Test
    void testValidateState_WithValidState_ShouldNotThrowException() {
        // When & Then - no exception should be thrown
        assertDoesNotThrow(() -> role.validateState());
    }

    @Test
    void testValidateState_WithNullRoleType() {
        // Given
        role.setRoleType(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> role.validateState());
    }

    @Test
    void testValidateState_WithNegativeDisplayOrder() {
        // Given
        role.setDisplayOrder(-1);

        // When & Then
        assertThrows(IllegalStateException.class, () -> role.validateState());
    }

    @Test
    void testInitializeRole_ShouldInitializeCollections() {
        // Given
        Role role = new Role();
        role.setUsers(null);
        role.setPermissions(null);
        role.setDisplayOrder(null);

        // When
        role.initializeRole();

        // Then
        assertNotNull(role.getUsers());
        assertNotNull(role.getPermissions());
        assertEquals(0, role.getDisplayOrder());
    }
}
