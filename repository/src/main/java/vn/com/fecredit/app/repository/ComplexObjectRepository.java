package vn.com.fecredit.app.repository;

import org.springframework.data.repository.NoRepositoryBean;

import vn.com.fecredit.app.entity.base.AbstractPersistableEntity;
import vn.com.fecredit.app.entity.base.SerializableKey;

/**
 * Base repository interface for all entity repositories.
 * Extends both JpaRepository and JpaSpecificationExecutor to provide
 * standard CRUD operations and specification-based querying.
 *
 * @param <T> entity type
 */
@NoRepositoryBean
public interface ComplexObjectRepository<T extends AbstractPersistableEntity<U>, U extends SerializableKey> extends AbstractRepository<T, U> {
    // Common repository methods can be added here
}
