package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Common interface for services dealing with status-aware entities.
 *
 * @param <T> The entity type, which must extend AbstractStatusAwareEntity
 */
public interface AbstractService<T extends AbstractStatusAwareEntity<U>, U extends Serializable> {
    
    List<T> findAll();
    
    Optional<T> findById(U id);
    
    T save(T entity);
    
    void deleteById(U id);
}