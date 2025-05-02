package vn.com.fecredit.app.service.factory.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.Configuration;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.RewardEvent;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.repository.AuditLogRepository;
import vn.com.fecredit.app.repository.BlacklistedTokenRepository;
import vn.com.fecredit.app.repository.ConfigurationRepository;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.ParticipantEventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.repository.PermissionRepository;
import vn.com.fecredit.app.repository.ProvinceRepository;
import vn.com.fecredit.app.repository.RegionRepository;
import vn.com.fecredit.app.repository.RewardEventRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

/**
 * Implementation of the repository factory.
 * Maps ObjectType enums to entity classes and repositories.
 */
@Slf4j
@Component
public class RepositoryFactoryImpl implements RepositoryFactory {

    private final ApplicationContext applicationContext;
    private final Map<ObjectType, Class<?>> entityClassMap;
    private final Map<Class<?>, Class<? extends JpaRepository<?, ?>>> repositoryClassMap;
    private final Map<ObjectType, String> tableNameMap;
    private final Map<String, ObjectType> entityNameToObjectTypeMap;

    public RepositoryFactoryImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        // Initialize entity class mappings
        this.entityClassMap = new HashMap<>();
        this.repositoryClassMap = new HashMap<>();
        this.tableNameMap = new HashMap<>();
        this.entityNameToObjectTypeMap = new HashMap<>();

        initializeMappings();
    }

    @PostConstruct
    public void logAvailableBeans() {
        log.info("Available beans in application context:");
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            if (beanName.toLowerCase().contains("repository")) {
                log.info("Repository bean found: {}", beanName);
            }
        }

        // Verify repository beans existence
        for (Class<?> entityClass : repositoryClassMap.keySet()) {
            Class<? extends JpaRepository<?, ?>> repositoryClass = repositoryClassMap.get(entityClass);
            try {
                Object bean = applicationContext.getBean(repositoryClass);
                log.info("Successfully looked up repository bean for entity: {} -> {}",
                        entityClass.getSimpleName(), bean.getClass().getName());
            } catch (Exception e) {
                log.warn("Failed to lookup repository bean for entity: {} -> {}. Error: {}",
                        entityClass.getSimpleName(), repositoryClass.getName(), e.getMessage());
            }
        }
    }

    private void initializeMappings() {
        // Entity class to ObjectType mapping
        mapEntity(ObjectType.User, User.class, UserRepository.class, "users");
        mapEntity(ObjectType.Role, Role.class, RoleRepository.class, "roles"); // Fix: Change from "role" to "roles"
        mapEntity(ObjectType.Permission, Permission.class, PermissionRepository.class, "permissions");
        mapEntity(ObjectType.Event, Event.class, EventRepository.class, "events");
        mapEntity(ObjectType.EventLocation, EventLocation.class, EventLocationRepository.class, "event_locations");
        mapEntity(ObjectType.GoldenHour, GoldenHour.class, GoldenHourRepository.class, "golden_hours");
        mapEntity(ObjectType.Region, Region.class, RegionRepository.class, "regions");
        mapEntity(ObjectType.Province, Province.class, ProvinceRepository.class, "provinces");
        mapEntity(ObjectType.Reward, Reward.class, RewardRepository.class, "rewards");
        mapEntity(ObjectType.RewardEvent, RewardEvent.class, RewardEventRepository.class, "rewards");
        mapEntity(ObjectType.Participant, Participant.class, ParticipantRepository.class, "participants");
        mapEntity(ObjectType.ParticipantEvent, ParticipantEvent.class, ParticipantEventRepository.class,
                "participant_events");
        mapEntity(ObjectType.SpinHistory, SpinHistory.class, SpinHistoryRepository.class, "spin_histories");
        mapEntity(ObjectType.AuditLog, AuditLog.class, AuditLogRepository.class, "audit_logs");
        mapEntity(ObjectType.BlacklistedToken, BlacklistedToken.class, BlacklistedTokenRepository.class,
                "blacklisted_tokens");
        mapEntity(ObjectType.Configuration, Configuration.class, ConfigurationRepository.class, "configurations");

        // Also map by simple name for backward compatibility
        for (ObjectType type : ObjectType.values()) {
            if (entityClassMap.containsKey(type)) {
                Class<?> entityClass = entityClassMap.get(type);
                entityNameToObjectTypeMap.put(entityClass.getSimpleName(), type);
            }
        }
    }

    private <T, R extends JpaRepository<T, ?>> void mapEntity(
            ObjectType objectType,
            Class<T> entityClass,
            Class<R> repositoryClass,
            String tableName) {
        entityClassMap.put(objectType, entityClass);
        repositoryClassMap.put(entityClass, repositoryClass);
        tableNameMap.put(objectType, tableName);
        entityNameToObjectTypeMap.put(entityClass.getSimpleName(), objectType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> getEntityClass(ObjectType objectType) {
        if (entityClassMap.containsKey(objectType)) {
            return (Class<T>) entityClassMap.get(objectType);
        }
        log.error("Unsupported object type: {}", objectType);
        throw new IllegalArgumentException("Unsupported object type: " + objectType);
    }

    /**
     * Gets the ObjectType for a given entity name.
     *
     * @param entityName the simple name of the entity class
     * @return the corresponding ObjectType
     * @throws IllegalArgumentException if no mapping exists for the entity name
     */
    public ObjectType getObjectTypeForEntityName(String entityName) {
        if (entityNameToObjectTypeMap.containsKey(entityName)) {
            return entityNameToObjectTypeMap.get(entityName);
        }
        log.error("Unsupported entity name: {}", entityName);
        throw new IllegalArgumentException("Unsupported entity name: " + entityName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R extends JpaRepository<T, ?>> R getRepositoryForClass(Class<T> entityClass) {
        if (repositoryClassMap.containsKey(entityClass)) {
            Class<? extends JpaRepository<?, ?>> repositoryClass = repositoryClassMap.get(entityClass);
            try {
                return (R) applicationContext.getBean(repositoryClass);
            } catch (Exception e) {
                log.error("Failed to get repository bean for {}: {}", entityClass.getName(), e.getMessage());
                throw new IllegalStateException("Failed to get repository bean for " + entityClass.getName(), e);
            }
        }
        log.error("No repository found for entity class: {}", entityClass.getName());
        throw new IllegalArgumentException("No repository found for entity class: " + entityClass.getName());
    }

    @Override
    public String getTableNameForObjectType(ObjectType objectType) {
        if (tableNameMap.containsKey(objectType)) {
            return tableNameMap.get(objectType);
        }
        log.error("No table name mapping found for object type: {}", objectType);
        throw new IllegalArgumentException("No table name mapping found for object type: " + objectType);
    }
}
