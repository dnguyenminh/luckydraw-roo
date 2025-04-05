package vn.com.fecredit.app.entity.base;

import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Interface for entities that maintain a status field.
 * Provides methods to get and set status, and to check if an entity is active.
 */
public interface StatusAware {
    
    /**
     * Get the status of this entity
     * @return the current status
     */
    CommonStatus getStatus();
    
    /**
     * Set the status of this entity
     * @param status the new status
     * @return this entity for chaining
     */
    StatusAware setStatus(CommonStatus status);
    
    /**
     * Check if this entity is active
     * @return true if status is ACTIVE
     */
    boolean isActive();
}
