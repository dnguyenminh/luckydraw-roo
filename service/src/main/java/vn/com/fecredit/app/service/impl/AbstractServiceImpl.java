package vn.com.fecredit.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.service.AbstractService;

import java.util.List;
import java.util.Optional;

// Remove the @Slf4j annotation as we're manually creating a logger
public abstract class AbstractServiceImpl<T extends AbstractStatusAwareEntity> implements AbstractService<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private final JpaRepository<T, Long> repository;
    
    public AbstractServiceImpl(JpaRepository<T, Long> repository) {
        this.repository = repository;
    }
    
    @Override
    public List<T> findAll() {
        return repository.findAll();
    }
    
    @Override
    public Optional<T> findById(Long id) {
        return repository.findById(id);
    }
    
    @Override
    public T save(T entity) {
        return repository.save(entity);
    }
    
    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}