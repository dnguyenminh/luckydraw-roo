package vn.com.fecredit.app.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;

import java.util.List;
import java.util.Optional;

public interface AbstractService<T extends AbstractStatusAwareEntity> {
    T save(T entity);
    T update(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
    Page<T> findAll(Pageable pageable);
    void deleteById(Long id);
    List<T> findByStatus(CommonStatus status);
    T activate(Long id);
    T deactivate(Long id);
}
