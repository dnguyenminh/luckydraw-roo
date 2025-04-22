package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;

/**
 * Entity representing a system-wide configuration setting.
 * <p>
 * Configurations provide a flexible way to store and retrieve application settings
 * without requiring code changes. Each configuration has a unique key, a value, and
 * a description. These settings control various aspects of the system behavior and
 * can be updated at runtime.
 * </p>
 * <p>
 * Common uses include defining business rules, feature flags, and operational parameters
 * that may need adjustment over time.
 * </p>
 */
@Entity
@Table(name = "configurations", indexes = {
        @Index(name = "idx_config_key", columnList = "config_key", unique = true)
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Configuration extends AbstractSimplePersistableEntity<Long> {

    /**
     * Unique configuration key
     * Used as the identifier for looking up configuration values
     */
    @NotBlank
    @Column(name = "config_key", nullable = false, unique = true)
    private String key;

    /**
     * Configuration value stored as string
     */
    @Column(name = "config_value", length = 4000)
    private String value;

    /**
     * Human-readable description of this configuration setting
     */
    @Column(name = "description", length = 500)
    private String description;
}
