package vn.com.fecredit.app.entity.base;

import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Interface for entities that maintain a status.
 * <p>
 * This interface defines the basic contract for entities that can be
 * enabled, disabled, or have other status values. It ensures that all
 * status-aware entities provide methods to get and set their status.
 * </p>
 */
public interface StatusAware {

    /**
     * Get the current status of this entity.
     *
     * @return The current status
     */
    CommonStatus getStatus();

    /**
     * Set the status of this entity.
     *
     * @param status The new status
     * @return This entity for chaining
     */
    StatusAware setStatus(CommonStatus status);

    /**
     * Check if this entity is active.
     *
     * @return true if the entity is active, false otherwise
     */
    boolean isActive();

    /**
     * Activate this entity by setting its status to ACTIVE.
     */
    void activate();

    /**
     * Deactivate this entity by setting its status to INACTIVE.
     */
    void deactivate();
}
