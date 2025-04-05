package vn.com.fecredit.app.entity.enums;

/**
 * Enumeration of common entity statuses.
 * Used across all entities that implement the StatusAware interface.
 */
public enum CommonStatus {
    /**
     * Entity is active and can be used
     */
    ACTIVE,
    
    /**
     * Entity is inactive and should not be used
     */
    INACTIVE,
    
    /**
     * Entity is pending activation
     */
    PENDING,
    
    /**
     * Entity has been archived (soft delete)
     */
    ARCHIVED;
    
    /**
     * Check if this status represents an active state
     * @return true if this status is ACTIVE
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
}