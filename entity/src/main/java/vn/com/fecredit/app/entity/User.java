package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;


/**
 * Entity representing a user in the system with authentication and authorization capabilities.
 * <p>
 * The User entity stores authentication credentials, personal information, and account status
 * flags that determine the user's ability to access the system. Each user can be associated with
 * a role that defines their permissions within the application.
 * </p>
 * <p>
 * The entity also maintains a collection of blacklisted authentication tokens for security
 * purposes, enabling token revocation during logout or when security is compromised.
 * </p>
 * Key features:
 * <ul>
 *   <li>Username and password-based authentication</li>
 *   <li>Email validation for communication purposes</li>
 *   <li>Account status tracking (enabled, locked, expired)</li>
 *   <li>Role-based authorization</li>
 *   <li>Token blacklisting for enhanced security</li>
 * </ul>
 * <p>
 * This entity forms the foundation of the application's security model and user management system.
 * </p>
 *
 * @see Role
 * @see BlacklistedToken
 * @see AbstractSimplePersistableEntity
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class User extends AbstractSimplePersistableEntity<Long> {

    /**
     * Unique username for authentication
     * Used as the login identifier
     */
    @NotBlank(message = "Username is required")
    @Column(name = "username", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String username;

    /**
     * Encrypted password for authentication
     * Stored using secure hashing algorithm (BCrypt)
     */
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User's email address for communications and notifications
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * User's full name for display purposes
     */
    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

//    /**
//     * Flag indicating if the account is currently enabled
//     * Disabled accounts cannot log in
//     */
//    @Column(name = "enabled", nullable = false)
//    private boolean enabled = true;


    /**
     * Role assigned to this user for authorization purposes
     * Defines the permissions and access level of the user
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Role role;

    /**
     * Collection of blacklisted tokens associated with this user
     * Used for tracking revoked authentication tokens
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<BlacklistedToken> blacklistedTokens = new HashSet<>();

    /**
     * Adds a blacklisted token to this user with proper bidirectional relationship
     *
     * @param token the token to blacklist and associate with this user
     */
    public void addBlacklistedToken(BlacklistedToken token) {
        blacklistedTokens.add(token);
        token.setUser(this);
    }

    /**
     * Removes a blacklisted token from this user with proper relationship cleanup
     *
     * @param token the token to remove from this user's blacklist
     */
    public void removeBlacklistedToken(BlacklistedToken token) {
        blacklistedTokens.remove(token);
        token.setUser(null);
    }

    /**
     * Check if the user's account is valid for authentication
     *
     * @return true if the account can be used for login
     */
    @Transient
    public boolean isAccountValid() {
        return role != null;
    }

    /**
     * Checks if the user has a specific role.
     *
     * @param adminRole the role to check against
     * @return true if the user has the specified role and the role is active
     */
    public boolean hasRole(Role adminRole) {
        return this.role != null && adminRole != null && adminRole.isActive() && this.role.getRoleType() == adminRole.getRoleType();
    }
}
