package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.io.Serializable;

/**
 * Base repository interface for all entity repositories.
 * Provides common CRUD operations and specification-based querying.
 *
 * @param <T> Entity type that extends AbstractStatusAwareEntity
 */
@NoRepositoryBean
public interface AbstractRepository<T extends AbstractStatusAwareEntity<ID>, ID extends Serializable>
    extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    // Common repository methods can be added here
}
