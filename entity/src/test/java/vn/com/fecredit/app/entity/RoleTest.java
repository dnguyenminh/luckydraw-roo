package vn.com.fecredit.app.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.enums.RoleName;

/**
 * Test class for the Role entity.
 */
class RoleTest {

    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder()
            .roleName(RoleName.USER)
            .displayOrder(1)
            .description("Regular user role")
            .status(CommonStatus.ACTIVE)
            .build();
    }

    @Test
    void testBasicProperties() {
        assertThat(role.getRoleName()).isEqualTo(RoleName.USER);
        assertThat(role.getDisplayOrder()).isEqualTo(1);
        assertThat(role.getDescription()).isEqualTo("Regular user role");
        assertTrue(role.getStatus().isActive());
    }

    @Test
    void testUserAssociation() {
        assertTrue(role.getUsers().isEmpty());

        User user = User.builder()
            .username("testuser")
            .email("test@example.com")
            .status(CommonStatus.ACTIVE)
            .build();

        role.getUsers().add(user);
        user.getRoles().add(role);

        assertFalse(role.getUsers().isEmpty());
        assertTrue(role.getUsers().contains(user));
        assertTrue(user.getRoles().contains(role));
    }

    @Test
    void testValidation() {
        Role invalid = Role.builder().build();
        assertThrows(IllegalStateException.class, () -> invalid.validateState());

        invalid.setRoleName(RoleName.ADMIN);
        invalid.setDisplayOrder(-1);
        assertThrows(IllegalStateException.class, () -> invalid.validateState());

        invalid.setDisplayOrder(1);
        assertDoesNotThrow(() -> invalid.validateState());
    }

    @Test
    void testDefaultValues() {
        Role newRole = new Role();
        assertThat(newRole.getDisplayOrder()).isEqualTo(0);
        assertNotNull(newRole.getUsers());
        assertTrue(newRole.getUsers().isEmpty());
    }

    @Test
    void testInvalidDisplayOrder() {
        role.setDisplayOrder(-1);
        assertThrows(IllegalStateException.class, () -> role.validateState());

        role.setDisplayOrder(null);
        assertThrows(IllegalStateException.class, () -> role.validateState());

        role.setDisplayOrder(0);
        assertDoesNotThrow(() -> role.validateState());
    }

    @Test
    void testEqualsAndHashCode() {
        Role role1 = Role.builder()
            .roleName(RoleName.USER)
            .displayOrder(1)
            .build();

        Role role2 = Role.builder()
            .roleName(RoleName.USER)
            .displayOrder(2)  // Different display order
            .build();

        // Should be equal even with different display orders as they extend AbstractPersistableEntity
        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }
}
