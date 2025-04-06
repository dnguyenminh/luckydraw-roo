package vn.com.fecredit.app.service.factory;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.repository.AuditLogRepository;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.ParticipantEventRepository;
import vn.com.fecredit.app.repository.ProvinceRepository;
import vn.com.fecredit.app.repository.RegionRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.RoleRepository;
import vn.com.fecredit.app.repository.SimpleObjectRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;
import vn.com.fecredit.app.repository.UserRepository;
import vn.com.fecredit.app.service.dto.ObjectType;

/**
 * Factory for retrieving entity repositories based on entity type.
 * Implements the Factory Method pattern to provide appropriate repositories.
 */
@Component
@Slf4j
public class RepositoryFactorySibling {

    private final ApplicationContext applicationContext;
    // Use raw types for these maps to properly handle the class relationships
    private final Map<Class<?>, Class<?>> entityToRepositoryMap = new HashMap<>();
    private final Map<ObjectType, String> objectTypeToEntityClassMap = new HashMap<>();

    @Autowired
    public RepositoryFactorySibling(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        // Initialize entity to repository mapping
        entityToRepositoryMap.put(User.class, UserRepository.class);
        entityToRepositoryMap.put(Role.class, RoleRepository.class);
        entityToRepositoryMap.put(Event.class, EventRepository.class);
        entityToRepositoryMap.put(Region.class, RegionRepository.class);
        entityToRepositoryMap.put(Province.class, ProvinceRepository.class);
        entityToRepositoryMap.put(EventLocation.class, EventLocationRepository.class);
        entityToRepositoryMap.put(Reward.class, RewardRepository.class);
        entityToRepositoryMap.put(GoldenHour.class, GoldenHourRepository.class);
        entityToRepositoryMap.put(SpinHistory.class, SpinHistoryRepository.class);
        entityToRepositoryMap.put(ParticipantEvent.class, ParticipantEventRepository.class);
        entityToRepositoryMap.put(AuditLog.class, AuditLogRepository.class);

        // Initialize object type to entity class mapping with fully qualified class names
        objectTypeToEntityClassMap.put(ObjectType.User, "vn.com.fecredit.app.entity.User");
        objectTypeToEntityClassMap.put(ObjectType.Role, "vn.com.fecredit.app.entity.Role");
        objectTypeToEntityClassMap.put(ObjectType.Event, "vn.com.fecredit.app.entity.Event");
        objectTypeToEntityClassMap.put(ObjectType.Region, "vn.com.fecredit.app.entity.Region");
        objectTypeToEntityClassMap.put(ObjectType.Province, "vn.com.fecredit.app.entity.Province");
        objectTypeToEntityClassMap.put(ObjectType.EventLocation, "vn.com.fecredit.app.entity.EventLocation");
        objectTypeToEntityClassMap.put(ObjectType.Reward, "vn.com.fecredit.app.entity.Reward");
        objectTypeToEntityClassMap.put(ObjectType.GoldenHour, "vn.com.fecredit.app.entity.GoldenHour");
        objectTypeToEntityClassMap.put(ObjectType.SpinHistory, "vn.com.fecredit.app.entity.SpinHistory");
        objectTypeToEntityClassMap.put(ObjectType.ParticipantEvent, "vn.com.fecredit.app.entity.ParticipantEvent");
        objectTypeToEntityClassMap.put(ObjectType.AuditLog, "vn.com.fecredit.app.entity.AuditLog");
    }

    /**
     * Get repository for the specified entity class
     * 
     * @param entityClass class of the entity
     * @return repository for the entity
     * @throws IllegalArgumentException if no repository found
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractStatusAwareEntity> SimpleObjectRepository<T> getRepositoryForClass(Class<T> entityClass) {
        Class<?> repositoryClass = entityToRepositoryMap.get(entityClass);
        if (repositoryClass == null) {
            log.error("No repository found for entity class: {}", entityClass.getName());
            throw new IllegalArgumentException("Unsupported entity class: " + entityClass.getName());
        }

        return (SimpleObjectRepository<T>) applicationContext.getBean(repositoryClass);
    }

    /**
     * Get entity class for the specified object type
     * 
     * @param objectType type of object
     * @return entity class
     * @throws IllegalArgumentException if object type is not supported
     */
    public <T extends AbstractStatusAwareEntity> Class<T> getEntityClass(ObjectType objectType) {
        String className = objectTypeToEntityClassMap.get(objectType);
        if (className == null) {
            log.error("No entity class mapping found for object type: {}", objectType);
            throw new IllegalArgumentException("Unsupported object type: " + objectType);
        }
        return getEntityClassFromString(className);
    }

    /**
     * Convert a string class name to the actual Class object
     * 
     * @param className the fully qualified class name
     * @return the Class object representing the specified class name
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractStatusAwareEntity> Class<T> getEntityClassFromString(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("Failed to load entity class: {}", className, e);
            throw new IllegalArgumentException("Entity class not found: " + className, e);
        }
    }

    /**
     * Get table name for entity
     * 
     * @param objectType type of object
     * @return table name (usually plural form of object name)
     */
    public String getTableNameForObjectType(ObjectType objectType) {
        return objectType.name().toLowerCase() + "s"; // Simple pluralization with lowercase
    }
}
