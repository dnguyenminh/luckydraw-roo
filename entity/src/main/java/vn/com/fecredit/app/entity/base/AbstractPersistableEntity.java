package vn.com.fecredit.app.entity.base;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Base entity class that contains the ID and version fields.
 * All entities should extend this class.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class AbstractPersistableEntity {

    /**
     * Primary key using identity generation strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * Version field for optimistic locking
     */
    @Version
    @Column(name = "version")
    private Long version;
}
