package vn.com.fecredit.app.entity.base;

import java.io.Serializable;
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
 * Base abstract entity that adds audit capability to persistable entities.
 * Tracks creation and modification metadata including timestamps and users.
 * <p>
 * This class extends AbstractPersistableEntity by adding standard audit fields
 * that automatically record when and by whom entities are created and modified.
 * </p>
 *
 * <p>
 * The default no-argument constructor is provided by Lombok's {@code @NoArgsConstructor}
 * annotation and is required for JPA entity instantiation during the persistence
 * lifecycle.
 * </p>
 *
 * @param <T> The type of the identifier used by entities extending this class,
 *           must implement {@link Serializable}
 */
@MappedSuperclass
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Creates a default no-args constructor required by JPA
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditEntity<T extends Serializable> {

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
     * Set created at timestamp
     * @param createdAt the creation timestamp
     * @return this entity for chaining
     */
    public AbstractAuditEntity<T> setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Set created by user
     * @param createdBy the user who created this entity
     * @return this entity for chaining
     */
    public AbstractAuditEntity<T> setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Set updated at timestamp
     * @param updatedAt the update timestamp
     * @return this entity for chaining
     */
    public AbstractAuditEntity<T> setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * Set updated by user
     * @param updatedBy the user who updated this entity
     * @return this entity for chaining
     */
    public AbstractAuditEntity<T> setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }
}
