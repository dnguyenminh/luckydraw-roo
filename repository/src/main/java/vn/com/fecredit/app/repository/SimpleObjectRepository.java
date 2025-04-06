package vn.com.fecredit.app.repository;

import org.springframework.data.repository.NoRepositoryBean;

import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

/**
 * Base repository interface for all entity repositories.
 * Provides common CRUD operations and specification-based querying.
 * 
 * @param <T> Entity type that extends AbstractStatusAwareEntity
 */
@NoRepositoryBean
public interface SimpleObjectRepository<T extends AbstractStatusAwareEntity> 
    extends AbstractRepository<T, Long> {
    
    // Common repository methods can be added here
}
