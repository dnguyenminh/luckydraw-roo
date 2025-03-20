package vn.com.fecredit.app.entity.base;

import vn.com.fecredit.app.entity.CommonStatus;

/**
 * Interface for entities that have a status field.
 * Keeps the contract focused only on status-related operations.
 */
public interface StatusAware {
    /**
     * Get the current status of the entity
     * @return the current status
     */
    CommonStatus getStatus();

    /**
     * Set the status of the entity
     * @param status the new status
     */
    void setStatus(CommonStatus status);
    
    /**
     * Check if the entity is active
     * @return true if the status is ACTIVE
     */
    default boolean isActive() {
        return getStatus() == CommonStatus.ACTIVE;
    }
    
    /**
     * Check if the entity is deleted
     * @return true if the status is DELETED
     */
    default boolean isDeleted() {
        return getStatus() == CommonStatus.DELETED;
    }
    
    /**
     * Mark the entity as deleted
     */
    default void markAsDeleted() {
        setStatus(CommonStatus.DELETED);
    }
    
    /**
     * Mark the entity as active
     */
    default void markAsActive() {
        setStatus(CommonStatus.ACTIVE);
    }
}
