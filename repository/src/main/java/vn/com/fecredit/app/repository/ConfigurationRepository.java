package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Configuration;
import vn.com.fecredit.app.entity.CommonStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
    Optional<Configuration> findByKey(String key);
    List<Configuration> findByStatus(CommonStatus status);
    List<Configuration> findByKeyContainingIgnoreCase(String searchTerm);
}
