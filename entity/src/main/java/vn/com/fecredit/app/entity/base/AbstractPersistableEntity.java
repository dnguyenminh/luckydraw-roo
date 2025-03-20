package vn.com.fecredit.app.entity.base;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "version"})
public abstract class AbstractPersistableEntity implements Persistable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    public boolean isPersisted() {
        return id != null;
    }

    public boolean isModified() {
        return version != null && version > 0;
    }

    /**
     * Creates a proxy reference with just the ID
     * @return a new instance with only the ID set
     */
    public AbstractPersistableEntity toReference() {
        try {
            AbstractPersistableEntity reference = getClass().getDeclaredConstructor().newInstance();
            reference.setId(this.getId());
            return reference;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity reference", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPersistableEntity)) return false;
        AbstractPersistableEntity that = (AbstractPersistableEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
