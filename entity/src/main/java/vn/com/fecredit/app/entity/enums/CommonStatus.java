package vn.com.fecredit.app.entity.enums;

/**
 * Common status values for entities in the system.
 * <p>
 * This enum defines standard status values that can be used across
 * different entities to maintain consistency in status representation.
 * </p>
 */
public enum CommonStatus {

    /**
     * Entity is active and available for use
     */
    ACTIVE,

    /**
     * Entity is inactive but still exists in the system
     */
    INACTIVE,

    /**
     * Entity is pending activation or approval
     */
    PENDING,

    /**
     * Entity is archived (historical record)
     */
    ARCHIVED,

    /**
     * Entity has been deleted (soft delete)
     */
    DELETED;

    /**
     * Check if this status represents an active state
     *
     * @return true if the status is ACTIVE
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Check if this status represents an inactive state
     *
     * @return true if the status is not ACTIVE
     */
    public boolean isInactive() {
        return this != ACTIVE;
    }
}
