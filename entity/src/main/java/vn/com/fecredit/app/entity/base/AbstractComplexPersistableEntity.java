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
 * Base class for all persistable entities in the system.
 * Provides standard fields like ID and version for JPA entity persistence.
 * <p>
 * All domain entities should extend this class directly or indirectly.
 * </p>
 *
 * <p>
 * The default no-argument constructor is provided by Lombok's
 * {@code @NoArgsConstructor}
 * annotation and is required for JPA entity instantiation.
 * </p>
 */
@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Creates a default no-args constructor for JPA
@AllArgsConstructor
@ToString
public abstract class AbstractComplexPersistableEntity<T extends Serializable> extends AbstractPersistableEntity<T> {

    /**
     * Primary key using identity generation strategy
     */
    @EmbeddedId
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private T id;

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

}
