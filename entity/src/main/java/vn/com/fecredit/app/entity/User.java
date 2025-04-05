package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.enums.RoleType;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing users in the system.
 * This entity stores user authentication and authorization information,
 * including credentials, personal details, and role assignments.
 * 
 * The User entity is central to the security model of the application,
 * connecting authentication (who the user is) to authorization (what they can do).
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true)
    }
)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class User extends AbstractStatusAwareEntity {

    /**
     * Unique username for authentication
     */
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String username;

    /**
     * Encrypted password for authentication
     */
    @Column(nullable = false)
    private String password;

    /**
     * User's email address, must be unique
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * User's full name for display purposes
     */
    @Column(name = "full_name")
    private String fullName;

    /**
     * Role type for this user (primary role)
     */
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleType role;

    /**
     * Flag indicating if the account is currently enabled
     */
    @Column(nullable = false)
    private boolean enabled;

    /**
     * Flag indicating if the account has expired
     */
    @Builder.Default
    private boolean accountExpired = false;

    /**
     * Flag indicating if the account is locked
     */
    @Builder.Default
    private boolean accountLocked = false;

    /**
     * Flag indicating if the credentials have expired
     */
    @Builder.Default
    private boolean credentialsExpired = false;

    /**
     * Roles assigned to this user for authorization
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ToString.Exclude
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<BlacklistedToken> blacklistedTokens = new HashSet<>();

    // Fix the relationship with BlacklistedToken - ensure back references are properly maintained
    public void addBlacklistedToken(BlacklistedToken token) {
        blacklistedTokens.add(token);
        token.setUser(this);
    }

    public void removeBlacklistedToken(BlacklistedToken token) {
        blacklistedTokens.remove(token);
        token.setUser(null);
    }


    /**
     * Returns whether the user account is currently enabled
     * @return true if the user account is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled status for this user account
     * @param enabled true to enable the account, false to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the collection of roles assigned to this user
     * @return set of Role objects associated with this user
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * Sets the collection of roles for this user
     * @param roles the set of roles to assign to this user
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /**
     * Check if user has the specified role
     * @param roleType the role type to check
     * @return true if the user has an active role with the given name
     */
    public boolean hasRole(RoleType roleType) {
        if (roles == null) {
            return false;
        }
        
        // Fixed - avoid NPE in database queries by checking each role object first
        return roles.stream()
                .filter(role -> role != null)
                .filter(role -> role.getStatus() != null && role.getStatus().isActive())
                .filter(role -> role.getRoleType() != null)
                .anyMatch(role -> role.getRoleType() == roleType);
    }
    
    /**
     * Add a role to this user
     * @param role the role to add
     */
    public void addRole(Role role) {
        if (role == null) return;
        
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        
        this.roles.add(role);
        Set<User> users = role.getUsers();
        if (users == null) {
            users = new HashSet<>();
            role.setUsers(users);
        }
        users.add(this);
    }
    
    /**
     * Remove a role from this user
     * @param role the role to remove
     */
    public void removeRole(Role role) {
        if (role == null) return;
        if (this.roles != null) {
            this.roles.remove(role);
        }
        
        Set<User> users = role.getUsers();
        if (users != null) {
            users.remove(this);
        }
    }
    
    /**
     * Check if the account is active (enabled and not expired/locked/etc)
     * @return true if the account is active and can be used
     */
    public boolean isAccountActive() {
        return isEnabled() && !accountExpired && !accountLocked && !credentialsExpired;
    }

    /**
     * Initialize collections after loading
     * Ensures that no collections are null after entity is loaded from database
     */
    @PostLoad
    public void initializeCollections() {
        if (roles == null) {
            roles = new HashSet<>();
        }
    }
}
