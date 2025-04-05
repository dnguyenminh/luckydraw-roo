package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Configuration;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface ConfigurationRepository extends SimpleObjectRepository<Configuration> {
    Optional<Configuration> findByKey(String key);
    List<Configuration> findByStatus(CommonStatus status);
    List<Configuration> findByKeyContainingIgnoreCase(String searchTerm);
}
