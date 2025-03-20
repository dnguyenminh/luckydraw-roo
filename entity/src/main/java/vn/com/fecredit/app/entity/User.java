package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.RoleName;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_status", columnList = "status")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "roles")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class User extends AbstractStatusAwareEntity {

    @NotBlank(message = "Username is required")
    @Column(name = "username", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String username;

    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "account_expired", nullable = false)
    @Builder.Default
    private boolean accountExpired = false;

    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    @Column(name = "credentials_expired", nullable = false)
    @Builder.Default
    private boolean credentialsExpired = false;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"),
        indexes = {
            @Index(name = "idx_user_role_user", columnList = "user_id"),
            @Index(name = "idx_user_role_role", columnList = "role_id")
        }
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public boolean hasRole(RoleName roleName) {
        return roles.stream()
                .anyMatch(role -> role.getRoleName() == roleName && role.getStatus().isActive());
    }

    public void addRole(Role role) {
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        roles.remove(role);
        role.getUsers().remove(this);
    }

    public boolean isAccountActive() {
        return isEnabled() && !isAccountExpired() && !isAccountLocked() && !isCredentialsExpired();
    }
}
