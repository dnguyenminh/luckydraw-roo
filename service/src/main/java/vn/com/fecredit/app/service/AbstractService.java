package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.List;
import java.util.Optional;

/**
 * Common interface for services dealing with status-aware entities.
 *
 * @param <T> The entity type, which must extend AbstractStatusAwareEntity
 */
public interface AbstractService<T extends AbstractStatusAwareEntity> {
    
    List<T> findAll();
    
    Optional<T> findById(Long id);
    
    T save(T entity);
    
    void deleteById(Long id);
}