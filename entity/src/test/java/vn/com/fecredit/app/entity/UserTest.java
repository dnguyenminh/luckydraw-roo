package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;
import vn.com.fecredit.app.entity.enums.RoleName;

import static org.junit.jupiter.api.Assertions.*;

class UserTest extends BaseEntityTest {

    private User user;
    private Role adminRole;
    private Role userRole;

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
                .roleName(RoleName.ADMIN)
                .description("Admin role")
                .displayOrder(1)
                .status(CommonStatus.ACTIVE)
                .build();

        userRole = Role.builder()
                .roleName(RoleName.PARTICIPANT)
                .description("User role")
                .displayOrder(2)
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void hasRole_WhenUserHasActiveRole_ShouldReturnTrue() {
        // Given
        user.addRole(adminRole);
        
        // When & Then
        assertTrue(user.hasRole(RoleName.ADMIN));
    }

    @Test
    void hasRole_WhenUserHasInactiveRole_ShouldReturnFalse() {
        // Given
        adminRole.setStatus(CommonStatus.INACTIVE);
        user.addRole(adminRole);
        
        // When & Then
        assertFalse(user.hasRole(RoleName.ADMIN));
    }

    @Test
    void hasRole_WhenUserDoesNotHaveRole_ShouldReturnFalse() {
        // Given user doesn't have any roles
        
        // When & Then
        assertFalse(user.hasRole(RoleName.ADMIN));
    }

    @Test
    void addRole_ShouldSetBidirectionalRelationship() {
        // When
        user.addRole(adminRole);
        
        // Then
        assertTrue(user.getRoles().contains(adminRole));
        assertTrue(adminRole.getUsers().contains(user));
    }

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

    @Test
    void isAccountActive_WhenAllFlagsAreTrue_ShouldReturnTrue() {
        // Given user with all flags set to active (from setup)
        
        // When & Then
        assertTrue(user.isAccountActive());
    }

    @Test
    void isAccountActive_WhenDisabled_ShouldReturnFalse() {
        // Given
        user.setEnabled(false);
        
        // When & Then
        assertFalse(user.isAccountActive());
    }
}
