package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import vn.com.fecredit.app.entity.base.SerializableKey;

/**
 * Base repository interface for all entity repositories.
 * Extends both JpaRepository and JpaSpecificationExecutor to provide
 * standard CRUD operations and specification-based querying.
 *
 * @param <T> entity type
 */
@NoRepositoryBean
public interface ComplexObjectRepository<T, U extends SerializableKey> extends JpaRepository<T, U>, JpaSpecificationExecutor<T> {
    // Common repository methods can be added here
}
