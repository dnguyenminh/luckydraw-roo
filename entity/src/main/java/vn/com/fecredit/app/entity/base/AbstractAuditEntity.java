package vn.com.fecredit.app.entity.base;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Abstract base entity with audit fields using Spring Data JPA Auditing.
 * Provides created/updated timestamps and user information.
 */
@MappedSuperclass
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditEntity extends AbstractPersistableEntity {

    /**
     * The username of the user who created this entity
     */
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    /**
     * The timestamp when this entity was created
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The username of the user who last updated this entity
     */
    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    /**
     * The timestamp when this entity was last updated
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Hook method for additional pre-persist actions in subclasses
     */
    public void doPrePersist() {
        // Do nothing by default, subclasses can override
    }

    /**
     * Hook method for additional pre-update actions in subclasses
     */
    public void doPreUpdate() {
        // Do nothing by default, subclasses can override
    }

    /**
     * Set created at timestamp
     * @param createdAt the creation timestamp
     * @return this entity for chaining
     */
    public AbstractAuditEntity setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    /**
     * Set created by user
     * @param createdBy the user who created this entity
     * @return this entity for chaining
     */
    public AbstractAuditEntity setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }
    
    /**
     * Set updated at timestamp
     * @param updatedAt the update timestamp
     * @return this entity for chaining
     */
    public AbstractAuditEntity setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    /**
     * Set updated by user
     * @param updatedBy the user who updated this entity
     * @return this entity for chaining
     */
    public AbstractAuditEntity setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }
}
