package vn.com.fecredit.app.repository;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;

import vn.com.fecredit.app.entity.base.AbstractPersistableEntity;

/**
 * Base repository interface for all entity repositories.
 * Extends both JpaRepository and JpaSpecificationExecutor to provide
 * standard CRUD operations and specification-based querying.
 *
 * @param <T> entity type
 */
@NoRepositoryBean
public interface SimpleObjectRepository<T extends AbstractPersistableEntity<U>, U extends Serializable> extends AbstractRepository<T, U> {
    // Common repository methods can be added here
}
