package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import vn.com.fecredit.app.entity.base.AbstractPersistableEntity;

import java.io.Serializable;

/**
 * Base repository interface for all entity repositories.
 * Extends both JpaRepository and JpaSpecificationExecutor to provide
 * standard CRUD operations and specification-based querying.
 *
 * @param <T> entity type
 */
@NoRepositoryBean
public interface SimpleObjectRepository<T extends AbstractPersistableEntity, U extends Serializable> extends AbstractRepository<T, U> {
    // Common repository methods can be added here
}
