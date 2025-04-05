package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.RoleType;

/**
 * Represents a role in the application for authorization purposes.
 * A role contains a set of permissions and can be assigned to users.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"users", "permissions"})
public class Role extends AbstractStatusAwareEntity {

    /**
     * The role type
     */
    @NotNull(message = "Role type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    private RoleType roleType;

    /**
     * Description of the role
     */
    @Column(name = "description")
    private String description;

    /**
     * Display order for UI presentation
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Users who have this role
     */
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();
    /**
     * Permissions assigned to this role
     */
    @ManyToMany
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Constructor with role name
     * @param roleName the role name
     */
    public Role(RoleType roleName) {
        this.roleType = roleName;
        this.displayOrder = 0;
        this.users = new HashSet<>();
    }

    /**
     * Initialize role with all required values
     */
    @PostLoad
    public void initializeRole() {
        if (this.users == null) {
            this.users = new HashSet<>();
        }
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
    }

    /**
     * Get the users associated with this role
     * @return set of users with this role
     */
    public Set<User> getUsers() {
        if (users == null) {
            users = new HashSet<>();
        }
        return users;
    }

    /**
     * Set the users associated with this role
     * @param users the users to set
     */
    public void setUsers(Set<User> users) {
        this.users = users != null ? users : new HashSet<>();
    }

    /**
     * Add a user to this role
     * @param user the user to add
     */
    public void addUser(User user) {
        if (user == null) return;
        
        if (this.users == null) {
            this.users = new HashSet<>();
        }
        
        this.users.add(user);
        Set<Role> roles = user.getRoles();
        if (roles == null) {
            roles = new HashSet<>();
            user.setRoles(roles);
        }
        roles.add(this);
    }

    /**
     * Remove a user from this role
     * @param user the user to remove
     */
    public void removeUser(User user) {
        if (user == null) return;
        if (this.users != null) {
            this.users.remove(user);
        }
        
        Set<Role> roles = user.getRoles();
        if (roles != null) {
            roles.remove(this);
        }
    }

    /**
     * Add a permission to this role
     * @param permission the permission to add
     */
    public void addPermission(Permission permission) {
        if (permission == null) return;
        
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        
        permissions.add(permission);
        
        // Ensure bidirectional relationship
        if (permission.getRoles() == null) {
            permission.setRoles(new HashSet<>());
        }
        
        // Add this role to the permission's roles collection if not already there
        if (!permission.getRoles().contains(this)) {
            permission.getRoles().add(this);
        }
    }
    
    /**
     * Remove a permission from this role
     * @param permission the permission to remove
     */
    public void removePermission(Permission permission) {
        if (permission == null) return;
        
        // Only proceed with removal if the permission was actually in the collection
        if (permissions != null && permissions.remove(permission)) {
            // Maintain bidirectional relationship by removing this role from permission
            if (permission.getRoles() != null) {
                permission.getRoles().remove(this);
            }
        }
    }

    /**
     * Check if role has a specific permission
     * @param permissionName the name of the permission to check
     * @return true if the role has the permission and both the role and permission are active
     */
    public boolean hasPermission(String permissionName) {
        return permissions.stream()
            .anyMatch(p -> p.getStatus().isActive() && 
                     p.getName().equalsIgnoreCase(permissionName));
    }

    /**
     * Validates the state of the role
     * Ensures that the role has valid required properties before operations
     * @throws IllegalStateException if the role name is null or display order is negative
     */
    public void validateState() throws IllegalStateException {
        if (roleType == null) {
            throw new IllegalStateException("Role name cannot be null");
        }
        
        if (displayOrder == null) {
            throw new IllegalStateException("Display order cannot be null");
        }
        
        if (displayOrder < 0) {
            throw new IllegalStateException("Display order cannot be negative");
        }
    }

    /**
     * Get the role type
     * @return the role type
     */
    public RoleType getRoleType() {
        return roleType;
    }

    /**
     * Set the role type
     * @param roleType the role type to set
     */
    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    @Override
    public void doPrePersist() {
        super.doPrePersist();
        validateState();
    }

    @Override
    public void doPreUpdate() {
        super.doPreUpdate();
        validateState();
    }
}
