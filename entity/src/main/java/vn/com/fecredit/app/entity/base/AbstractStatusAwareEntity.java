package vn.com.fecredit.app.entity.base;

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
 * Base entity class that includes status management.
 * Extends AuditEntity to include auditing information.
 */
@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public abstract class AbstractStatusAwareEntity extends AbstractAuditEntity implements StatusAware {

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
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return status != null && CommonStatus.ACTIVE.equals(status);
    }
    
    /**
     * Get temporary attributes for this entity instance
     * @return map of temporary attributes
     */
    public Map<String, Object> getTemporaryAttributes() {
        return temporaryAttributes;
    }
}