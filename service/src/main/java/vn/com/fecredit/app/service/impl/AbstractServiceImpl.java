package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;
import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.service.AbstractService;

// Remove the @Slf4j annotation as we're manually creating a logger
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractServiceImpl<T extends AbstractStatusAwareEntity<U>, U extends Serializable>
        implements AbstractService<T, U> {

    // @Builder.Default
    // protected final Logger log = LoggerFactory.getLogger(getClass());

    private final JpaRepository<T, U> repository;

    // public AbstractServiceImpl(JpaRepository<T, Long> repository) {
    // this.repository = repository;
    // }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<T> findById(U id) {
        return repository.findById(id);
    }

    @Override
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    public void deleteById(U id) {
        repository.deleteById(id);
    }
}
