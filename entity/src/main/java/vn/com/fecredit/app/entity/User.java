package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.com.fecredit.app.entity.enums.RoleType;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing users in the system
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractStatusAwareEntity {
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;
    
    @Column(nullable = false)
    private boolean active;
    
    private boolean accountExpired = false;
    
    private boolean accountLocked = false;
    
    private boolean credentialsExpired = false;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    public boolean isEnabled() {
        return active;
    }
    
    public void setEnabled(boolean enabled) {
        this.active = enabled;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
