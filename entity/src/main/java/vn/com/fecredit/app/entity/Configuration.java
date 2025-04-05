package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

/**
 * Configuration entity representing system-wide settings.
 * Provides a flexible key-value storage for application configuration parameters
 * that can be modified at runtime without code changes.
 */
@Entity
@Table(
    name = "configurations",
    indexes = {
        @Index(name = "idx_config_key", columnList = "config_key", unique = true)
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Configuration extends AbstractStatusAwareEntity {

    /**
     * Unique configuration key identifier
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
