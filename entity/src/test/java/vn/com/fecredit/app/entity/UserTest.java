package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.base.BaseEntityTest;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;

/**
 * Unit tests for the User entity class.
 * Tests user-role relationships and account status management.
 */
class UserTest extends BaseEntityTest {

    // Test fixtures
    private User user;
    @SuppressWarnings("unused") // Field used for test setup
    private Role userRole;
    private Role adminRole;

    /**
     * Set up test fixtures before each test
     */
    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .fullName("Test User")
                .enabled(true)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .status(CommonStatus.ACTIVE)
                .build();

        adminRole = Role.builder()
                .roleType(RoleType.ROLE_ADMIN)
                .description("Admin role")
                .displayOrder(1)
                .status(CommonStatus.ACTIVE)
                .build();

        userRole = Role.builder()
                .roleType(RoleType.ROLE_USER)
                .description("User role")
                .displayOrder(2)
                .status(CommonStatus.ACTIVE)
                .build();
    }

    /**
     * Test that hasRole returns true when a user has an active role
     */
    @Test
    void hasRole_WhenUserHasActiveRole_ShouldReturnTrue() {
        // Given
        user.addRole(adminRole);
        
        // When & Then
        assertTrue(user.hasRole(RoleType.ROLE_ADMIN));
    }

    /**
     * Test that hasRole returns false when a user has the role but it's inactive
     */
    @Test
    void hasRole_WhenUserHasInactiveRole_ShouldReturnFalse() {
        // Given
        adminRole.setStatus(CommonStatus.INACTIVE);
        user.addRole(adminRole);
        
        // When & Then
        assertFalse(user.hasRole(RoleType.ROLE_ADMIN));
    }

    /**
     * Test that hasRole returns false when a user doesn't have the role
     */
    @Test
    void hasRole_WhenUserDoesNotHaveRole_ShouldReturnFalse() {
        // Given user doesn't have any roles
        
        // When & Then
        assertFalse(user.hasRole(RoleType.ROLE_ADMIN));
    }

    /**
     * Test that adding a role establishes bidirectional relationship
     */
    @Test
    void addRole_ShouldSetBidirectionalRelationship() {
        // When
        user.addRole(adminRole);
        
        // Then
        assertTrue(user.getRoles().contains(adminRole));
        assertTrue(adminRole.getUsers().contains(user));
    }

    /**
     * Test that removing a role removes the bidirectional relationship
     */
    @Test
    void removeRole_ShouldRemoveBidirectionalRelationship() {
        // Given
        user.addRole(adminRole);
        
        // When
        user.removeRole(adminRole);
        
        // Then
        assertFalse(user.getRoles().contains(adminRole));
        assertFalse(adminRole.getUsers().contains(user));
    }

    /**
     * Test that account is active when all status flags are correctly set
     */
    @Test
    void isAccountActive_WhenAllFlagsAreTrue_ShouldReturnTrue() {
        // Given user with all flags set to active (from setup)
        
        // When & Then
        assertTrue(user.isAccountActive());
    }

    /**
     * Test that account is inactive when disabled
     */
    @Test
    void isAccountActive_WhenDisabled_ShouldReturnFalse() {
        // Given
        user.setEnabled(false);
        
        // When & Then
        assertFalse(user.isAccountActive());
    }
}
