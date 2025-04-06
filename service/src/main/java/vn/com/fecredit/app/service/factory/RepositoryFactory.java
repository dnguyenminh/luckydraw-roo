package vn.com.fecredit.app.service.factory;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
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
public class RepositoryFactory {

    private final ApplicationContext applicationContext;
    private final Map<Class<? extends AbstractStatusAwareEntity>, Class<? extends JpaRepository<? extends AbstractStatusAwareEntity, Long>>> entityToRepositoryMap = new HashMap<>();
    private final Map<ObjectType, Class<? extends AbstractStatusAwareEntity>> objectTypeToEntityCLassMap = new HashMap<>();

    @Autowired
    public RepositoryFactory(ApplicationContext applicationContext) {
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

        // Initialize object type to entity class mapping
        objectTypeToEntityCLassMap.put(ObjectType.User, User.class);
        objectTypeToEntityCLassMap.put(ObjectType.Role, Role.class);
        objectTypeToEntityCLassMap.put(ObjectType.Event, Event.class);
        objectTypeToEntityCLassMap.put(ObjectType.Region, Region.class);
        objectTypeToEntityCLassMap.put(ObjectType.Province, Province.class);
        objectTypeToEntityCLassMap.put(ObjectType.EventLocation, EventLocation.class);
        objectTypeToEntityCLassMap.put(ObjectType.Reward, Reward.class);
        objectTypeToEntityCLassMap.put(ObjectType.GoldenHour, GoldenHour.class);
        objectTypeToEntityCLassMap.put(ObjectType.SpinHistory, SpinHistory.class);
        objectTypeToEntityCLassMap.put(ObjectType.ParticipantEvent, ParticipantEvent.class);
        objectTypeToEntityCLassMap.put(ObjectType.AuditLog, AuditLog.class);

    }

    /**
     * Get repository for the specified entity class
     * 
     * @param <T>         entity type
     * @param entityClass class of the entity
     * @return repository for the entity
     * @throws IllegalArgumentException if no repository found
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractStatusAwareEntity> SimpleObjectRepository<T> getRepositoryForClass(Class<T> entityClass) {
        if (!entityToRepositoryMap.containsKey(entityClass)) {
            log.error("No repository found for entity class: {}", entityClass.getName());
            throw new IllegalArgumentException("Unsupported entity class: " + entityClass.getName());
        }

        Class<?> repositoryClass = entityToRepositoryMap.get(entityClass);
        return (SimpleObjectRepository<T>) applicationContext.getBean(repositoryClass);
    }

    /**
     * Get repository based on object type
     * 
     * @param <T>        entity type
     * @param objectType type of object to get repository for
     * @return repository for the object type
     * @throws IllegalArgumentException if object type is not supported
     */
    public <T extends AbstractStatusAwareEntity> SimpleObjectRepository<T> getRepository(ObjectType objectType) {
        Class<T> entityClass = getEntityClass(objectType);
        return getRepositoryForClass(entityClass);
    }

    /**
     * Convert a string class name to the actual Class object
     * 
     * @param className the fully qualified class name
     * @return the Class object representing the specified class name
     * @throws ClassNotFoundException if the class cannot be found
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractStatusAwareEntity> Class<T> getEntityClassFromString(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("Unsupported object type: {}", className);
            throw new IllegalArgumentException("Unsupported object type: " + className);
        }
    }

    /**
     * Get entity class for the specified object type
     * 
     * @param <T>        entity type
     * @param objectType type of object
     * @return entity class
     * @throws IllegalArgumentException if object type is not supported
     */
    public <T extends AbstractStatusAwareEntity> Class<T> getEntityClass(ObjectType objectType) {
        return getEntityClassFromString("vn.com.fecredit.app." + objectType.toString());
    }

    /**
     * Get table name for entity
     * 
     * @param objectType type of object
     * @return table name (usually plural form of object name)
     */
    public String getTableNameForObjectType(ObjectType objectType) {
        return objectType.name() + "s"; // Simple pluralization
    }
}
