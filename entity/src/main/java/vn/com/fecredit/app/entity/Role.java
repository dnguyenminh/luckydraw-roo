package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.RoleName;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "roles",
    indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true),
        @Index(name = "idx_role_status", columnList = "status")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "users")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Role extends AbstractStatusAwareEntity {

    @NotNull(message = "Role name is required")
    @Column(name = "name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    private RoleName roleName;

    @Min(value = 0, message = "Display order must be non-negative")
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    /**
     * Validate role state
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validateState() {
        if (roleName == null) {
            throw new IllegalStateException("Role name must be specified");
        }

        if (displayOrder == null) {
            throw new IllegalStateException("Display order must be specified");
        }

        if (displayOrder < 0) {
            throw new IllegalStateException("Display order must be non-negative");
        }
    }
}
