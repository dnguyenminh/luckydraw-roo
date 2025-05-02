package vn.com.fecredit.app.entity.base;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all persistable entities in the system.
 * Provides standard fields like ID and version for JPA entity persistence.
 * <p>
 * All domain entities should extend this class directly or indirectly.
 * </p>
 *
 * <p>
 * The default no-argument constructor is provided by Lombok's {@code @NoArgsConstructor}
 * annotation and is required by JPA for entity instantiation. This constructor creates
 * an entity with default values and null ID, which gets populated during persistence.
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
@ToString
public abstract class AbstractPersistableEntity<T extends Serializable> extends AbstractStatusAwareEntity<T> implements PersistableEntity<T> {

    /**
     * Version field for optimistic locking
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Sets the version number for optimistic locking.
     *
     * @param version The version number to set
     * @return This entity for method chaining
     */
    @Override
    public PersistableEntity<T> setVersion(Long version) {
        this.version = version;
        return this;
    }
}
