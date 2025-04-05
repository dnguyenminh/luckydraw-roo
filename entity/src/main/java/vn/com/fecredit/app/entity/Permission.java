package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

/**
 * Permission entity representing system permissions that can be assigned to roles.
 * Permissions control access to specific functionalities or resources in the system.
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Lombok will generate a no-args constructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "roles")
// @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Permission extends AbstractStatusAwareEntity {

    /**
     * Unique name of the permission
     */
    @NotBlank(message = "Permission name is required")
    @Column(name = "name", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String name;

    /**
     * Description of what the permission grants access to
     */
    @Column(name = "description")
    private String description;

    /**
     * Roles that have this permission
     */
    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Add this permission to a role
     * @param role the role to add this permission to
     */
    public void addRole(Role role) {
        if (role == null) return;
        
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
     * @param role the role to remove this permission from
     */
    public void removeRole(Role role) {
        if (role == null) return;
        
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
     * @throws IllegalStateException if invalid
     */
    public void validateState() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Permission name is required");
        }
        
        // Convert name to uppercase
        name = name.toUpperCase();
        
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
        
        if (name != null) {
            name = name.toUpperCase();
        }
    }

    // Override equals and hashCode directly instead of relying on annotations
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Permission that = (Permission) o;
        
        // Only compare name for equality (case-insensitive)
        if (name == null) {
            return that.name == null;
        }
        return name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        // Consistent with equals - use toUpperCase for case insensitivity
        return name != null ? name.toUpperCase().hashCode() : 0;
    }
}
