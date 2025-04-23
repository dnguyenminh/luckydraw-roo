package vn.com.fecredit.app.entity.base;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Base abstract entity for entities with simple (single-column) primary keys.
 * <p>
 * This class extends AbstractPersistableEntity to provide a standard implementation
 * for entities that use simple primary keys with auto-generation strategies. It uses
 * the {@link GenerationType#IDENTITY} strategy which relies on auto-increment columns
 * in the database.
 * </p>
 * <p>
 * Entity classes with simple primary keys (like Long, Integer, or UUID) should extend
 * this class rather than implementing their own ID management.
 * </p>
 * 
 * <p>
 * The default no-argument constructor is provided by Lombok's {@code @NoArgsConstructor}
 * annotation and is required by JPA for entity instantiation. It creates an entity
 * with null ID, which gets populated during persistence with an auto-generated value.
 * </p>
 *
 * @param <T> The type of the identifier used by entities extending this class,
 *           must implement {@link Serializable}
 */
@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Creates a default no-args constructor required by JPA
@AllArgsConstructor
@ToString
public abstract class AbstractSimplePersistableEntity<T extends Serializable> extends AbstractPersistableEntity<T> {

    /**
     * Primary key using identity generation strategy.
     * <p>
     * This field is automatically populated by the database when a new entity is persisted.
     * The {@link GenerationType#IDENTITY} strategy relies on an auto-increment column in
     * the database to generate unique identifier values.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private T id;

    /**
     * Returns the primary key identifier of this entity.
     *
     * @return The primary key value, or null if the entity hasn't been persisted yet
     */
    public T getId() {
        return id;
    }

    /**
     * Sets the primary key identifier for this entity.
     * <p>
     * In most cases, this method should not be called directly as the ID is
     * managed automatically by JPA and the database.
     * </p>
     *
     * @param id The primary key value to set
     */
    public void setId(T id) {
        this.id = id;
    }

}
