package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Configuration;
import vn.com.fecredit.app.service.base.AbstractService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ConfigurationService extends AbstractService<Configuration> {
    Optional<Configuration> findByKey(String key);
    Configuration saveOrUpdate(String key, String value, String description);
    String getValue(String key);
    String getValue(String key, String defaultValue);
    Integer getIntValue(String key, Integer defaultValue);
    Boolean getBooleanValue(String key, Boolean defaultValue);
    Map<String, String> getAllValues();
    List<Configuration> findByKeyContaining(String searchTerm);
    void deleteByKey(String key);
}
