package vn.com.fecredit.app.service.base;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

@Slf4j
@Transactional
public abstract class AbstractServiceImpl<T extends AbstractStatusAwareEntity> implements AbstractService<T> {
    
    protected final JpaRepository<T, Long> repository;
    
    protected AbstractServiceImpl(JpaRepository<T, Long> repository) {
        this.repository = repository;
    }

    @Override
    public T save(T entity) {
        log.debug("Saving entity: {}", entity);
        return repository.save(entity);
    }

    @Override
    public T update(T entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Entity must have an ID to be updated");
        }
        log.debug("Updating entity: {}", entity);
        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public void deleteById(Long id) {
        T entity = findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
        repository.delete(entity);
    }

    @Override
    public T activate(Long id) {
        T entity = findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
        entity.setStatus(CommonStatus.ACTIVE);
        return repository.save(entity);
    }

    @Override
    public T deactivate(Long id) {
        T entity = findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
        entity.setStatus(CommonStatus.INACTIVE);
        return repository.save(entity);
    }
}
