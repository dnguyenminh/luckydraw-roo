package vn.com.fecredit.app.entity.base;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Creates a default no-args constructor for JPA
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
