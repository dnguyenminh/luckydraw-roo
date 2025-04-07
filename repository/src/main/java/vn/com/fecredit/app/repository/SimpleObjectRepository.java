package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for all entity repositories.
 * Extends both JpaRepository and JpaSpecificationExecutor to provide
 * standard CRUD operations and specification-based querying.
 *
 * @param <T> entity type
 */
@NoRepositoryBean
public interface SimpleObjectRepository<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    // Common repository methods can be added here
}
