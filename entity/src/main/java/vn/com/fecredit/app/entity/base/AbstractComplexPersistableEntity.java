package vn.com.fecredit.app.entity.base;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Base class for entities with complex (composite) primary keys.
 * <p>
 * This class extends AbstractPersistableEntity to provide a standard implementation
 * for entities that use embedded or composite keys. It uses the {@link EmbeddedId}
 * annotation to indicate that the primary key is a composite key defined in a separate
 * embeddable class.
 * </p>
 * <p>
 * Entity classes with composite keys (like those implementing {@link SerializableKey})
 * should extend this class rather than AbstractSimplePersistableEntity.
 * </p>
 * 
 * <p>
 * The default no-argument constructor is provided by Lombok's {@code @NoArgsConstructor}
 * annotation and is required by JPA for entity instantiation. This constructor
 * initializes an entity with no ID set, which must be assigned before persistence.
 * </p>
 *
 * @param <T> The type of the composite identifier used by entities extending this class,
 *           must implement {@link Serializable}
 */
@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Creates a default no-args constructor required by JPA
@AllArgsConstructor
@ToString
public abstract class AbstractComplexPersistableEntity<T extends Serializable> extends AbstractPersistableEntity<T> {

    /**
     * Primary key using identity generation strategy
     */
    @EqualsAndHashCode.Include
    @EmbeddedId
    @Column(name = "id")
    private T id;

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

}
