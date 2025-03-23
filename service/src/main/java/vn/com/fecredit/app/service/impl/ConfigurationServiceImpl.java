package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.Configuration;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.repository.ConfigurationRepository;
import vn.com.fecredit.app.service.ConfigurationService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ConfigurationServiceImpl extends AbstractServiceImpl<Configuration> implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository) {
        super(configurationRepository);
        this.configurationRepository = configurationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Configuration> findByStatus(CommonStatus status) {
        return configurationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Configuration> findByKey(String key) {
        return configurationRepository.findByKey(key);
    }

    @Override
    @Transactional
    public Configuration saveOrUpdate(String key, String value, String description) {
        Optional<Configuration> existingConfig = configurationRepository.findByKey(key);
        
        if (existingConfig.isPresent()) {
            Configuration config = existingConfig.get();
            config.setValue(value);
            if (description != null && !description.isEmpty()) {
                config.setDescription(description);
            }
            return configurationRepository.save(config);
        } else {
            Configuration newConfig = Configuration.builder()
                .key(key)
                .value(value)
                .description(description)
                .status(CommonStatus.ACTIVE)
                .build();
            return configurationRepository.save(newConfig);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getValue(String key) {
        return configurationRepository.findByKey(key)
            .map(Configuration::getValue)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public String getValue(String key, String defaultValue) {
        return configurationRepository.findByKey(key)
            .map(Configuration::getValue)
            .orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getIntValue(String key, Integer defaultValue) {
        return configurationRepository.findByKey(key)
            .map(Configuration::getValue)
            .map(value -> {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    log.warn("Configuration value for key {} is not a valid integer: {}", key, value);
                    return defaultValue;
                }
            })
            .orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        return configurationRepository.findByKey(key)
            .map(Configuration::getValue)
            .map(value -> {
                if ("true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value)) {
                    return true;
                } else if ("false".equalsIgnoreCase(value) || "0".equals(value) || "no".equalsIgnoreCase(value)) {
                    return false;
                } else {
                    log.warn("Configuration value for key {} is not a valid boolean: {}", key, value);
                    return defaultValue;
                }
            })
            .orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getAllValues() {
        List<Configuration> configs = configurationRepository.findByStatus(CommonStatus.ACTIVE);
        return configs.stream()
            .collect(Collectors.toMap(Configuration::getKey, Configuration::getValue, (v1, v2) -> v2, HashMap::new));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Configuration> findByKeyContaining(String searchTerm) {
        return configurationRepository.findByKeyContainingIgnoreCase(searchTerm);
    }

    @Override
    @Transactional
    public void deleteByKey(String key) {
        configurationRepository.findByKey(key).ifPresent(config -> {
            config.setStatus(CommonStatus.INACTIVE);
            configurationRepository.save(config);
        });
    }
}
