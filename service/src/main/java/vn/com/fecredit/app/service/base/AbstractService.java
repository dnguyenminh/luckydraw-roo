package vn.com.fecredit.app.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface AbstractService<T extends AbstractStatusAwareEntity<U>, U extends Serializable> {
    T save(T entity);
    T update(T entity);
    Optional<T> findById(U id);
    List<T> findAll();
    Page<T> findAll(Pageable pageable);
    void deleteById(U id);
    List<T> findByStatus(CommonStatus status);
    T activate(U id);
    T deactivate(U id);
}
