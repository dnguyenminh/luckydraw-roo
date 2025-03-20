package vn.com.fecredit.app.entity.base;

/**
 * Interface for all entity status enums.
 * This interface provides type-safety and common status operations.
 */
public interface EntityStatus {
    /**
     * Get the name of the status
     * @return status name
     */
    String name();

    /**
     * Check if this is an active status
     * @return true if status represents an active state
     */
    boolean isActive();

    /**
     * Check if this is a deleted status
     * @return true if status represents a deleted state
     */
    boolean isDeleted();

    /**
     * Get the display name of the status
     * @return human-readable status name
     */
    default String getDisplayName() {
        return name().replace('_', ' ').toLowerCase();
    }
}