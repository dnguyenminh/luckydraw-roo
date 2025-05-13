package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;
import vn.com.fecredit.app.entity.enums.PermissionName;
import vn.com.fecredit.app.entity.enums.PermissionType;

/**
 * Entity representing a specific permission or privilege in the system.
 * <p>
 * Permissions define granular access rights to application features and actions.
 * Each permission represents a specific operation that can be assigned to roles,
 * providing fine-grained control over what users can do within the application.
 * </p>
 * <p>
 * Permissions are typically grouped and assigned to roles rather than directly
 * to users, simplifying the administration of access rights.
 * </p>
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Lombok will generate a no-args constructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "roles")
public class Permission extends AbstractSimplePersistableEntity<Long> {

    /**
     * Enum-based name of the permission
     * Identifies the specific operation or access right
     */
    @NotNull(message = "Name is required")
    @Column(name = "name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    private PermissionName name;

    /**
     * Type category of the permission (READ, WRITE, EXECUTE, ADMIN)
     * Used for organizing and filtering permissions by operation type
     */
    @Column(name = "permission_type")
    @Enumerated(EnumType.STRING)
    private PermissionType type;

    /**
     * Description of what the permission allows
     * Used for administrative documentation
     */
    @Column(name = "description")
    private String description;

    /**
     * Collection of roles that have been granted this permission
     * All security roles that include this specific access right
     */
    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Add this permission to a role
     * 
     * @param role the role to add this permission to
     */
    public void addRole(Role role) {
        if (role == null)
            return;

        if (roles == null) {
            roles = new HashSet<>();
        }

        roles.add(role);

        // Ensure bidirectional relationship
        if (!role.getPermissions().contains(this)) {
            role.getPermissions().add(this);
        }
    }

    /**
     * Remove this permission from a role
     * 
     * @param role the role to remove this permission from
     */
    public void removeRole(Role role) {
        if (role == null)
            return;

        if (roles != null) {
            roles.remove(role);
        }

        // Maintain bidirectional relationship
        if (role.getPermissions() != null) {
            role.getPermissions().remove(this);
        }
    }

    /**
     * Validate permission state
     * 
     * @throws IllegalStateException if invalid
     */
    public void validateState() {
        if (name == null) {
            throw new IllegalStateException("Permission name is required");
        }

        // Initialize roles if null
        if (roles == null) {
            roles = new HashSet<>();
        }
    }

    /**
     * Initialize permissions
     */
    @PostLoad
    public void initialize() {
        if (roles == null) {
            roles = new HashSet<>();
        }
    }

    // Override equals and hashCode directly instead of relying on annotations
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Permission that = (Permission) o;

        // Only compare name for equality
        if (name == null) {
            return that.name == null;
        }
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        // Consistent with equals
        return name != null ? name.hashCode() : 0;
    }
}
