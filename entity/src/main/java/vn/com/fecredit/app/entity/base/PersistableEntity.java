package vn.com.fecredit.app.entity.base;

import java.io.Serializable;

/**
 * Interface for all persistable entities in the system.
 * <p>
 * This interface defines the basic contract for entity persistence operations.
 * It ensures that all entities provide methods to get and set their unique identifier.
 * </p>
 * <p>
 * The generic parameter {@code <T>} allows for different ID types (Long, Integer, String, etc.)
 * to be used depending on the entity's requirements.
 * </p>
 *
 * @param <T> The type of the primary key (must implement Serializable)
 */
public interface PersistableEntity<T extends Serializable> {

    /**
     * Returns the primary key identifier of this entity.
     *
     * @return The primary key value, or null if the entity hasn't been persisted yet
     */
    T getId();

    /**
     * Sets the primary key identifier for this entity.
     *
     * @param id The primary key value to set
     */
    void setId(T id);

    /**
     * Returns the version number used for optimistic locking.
     *
     * @return The current version number
     */
    Long getVersion();

    /**
     * Sets the version number for optimistic locking.
     *
     * @param version The version number to set
     * @return This entity for method chaining
     */
    PersistableEntity<T> setVersion(Long version);
}
