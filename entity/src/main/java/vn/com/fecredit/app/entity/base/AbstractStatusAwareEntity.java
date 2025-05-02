package vn.com.fecredit.app.entity.base;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Base class for entities with status tracking capability.
 * Extends audit capabilities and implements the StatusAware interface.
 * <p>
 * This class provides common functionality for all entities that need
 * status management (like ACTIVE, INACTIVE, etc.). It implements status
 * lifecycle methods and status-based filtering capabilities.
 * </p>
 *
 * <p>
 * The default no-argument constructor is provided by Lombok's {@code @NoArgsConstructor}
 * annotation and is required by JPA for entity instantiation. This constructor
 * initializes a new entity with the default status (typically ACTIVE).
 * </p>
 *
 * @param <T> The type of the identifier used by entities extending this class,
 *           must implement {@link Serializable}
 */
@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor // Creates a default no-args constructor for JPA
@AllArgsConstructor
@ToString(callSuper = true)
public abstract class AbstractStatusAwareEntity<T extends Serializable> extends AbstractAuditEntity<T> implements StatusAware {

    /**
     * Status of this entity (ACTIVE, INACTIVE, etc.)
     */
    @NotNull
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;

    /**
     * Temporary attributes for audit and other purposes
     * Not persisted to the database and not serialized
     */
    @Transient
    @ToString.Exclude
    private final Map<String, Object> temporaryAttributes = new ConcurrentHashMap<>();

    /**
     * Set the status of this entity
     *
     * @param status the new status
     * @return this entity for chaining
     */
    @Override
    public StatusAware setStatus(CommonStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Activate this entity by setting status to ACTIVE
     */
    public void activate() {
        this.status = CommonStatus.ACTIVE;
    }

    /**
     * Deactivate this entity by setting status to INACTIVE
     */
    public void deactivate() {
        this.status = CommonStatus.INACTIVE;
    }

    /**
     * Check if this entity is active
     *
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return status != null && CommonStatus.ACTIVE.equals(status);
    }

}
