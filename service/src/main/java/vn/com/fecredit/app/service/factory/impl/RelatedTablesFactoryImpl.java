package vn.com.fecredit.app.service.factory.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.service.factory.RelatedTablesFactory;

/**
 * Implementation of the related tables factory.
 * Defines which tables are related to each entity.
 */
@Slf4j
@Component
public class RelatedTablesFactoryImpl implements RelatedTablesFactory {
    
    private final Map<Class<?>, List<String>> entityRelatedTablesMap;
    
    public RelatedTablesFactoryImpl() {
        // Initialize mapping of entity classes to their related tables
        this.entityRelatedTablesMap = new HashMap<>();
        
        // User related tables
        List<String> userRelatedTables = new ArrayList<>();
        userRelatedTables.add("user_role");
        userRelatedTables.add("audit_log");
        entityRelatedTablesMap.put(User.class, userRelatedTables);
        
        // Event related tables
        List<String> eventRelatedTables = new ArrayList<>();
        eventRelatedTables.add("event_location");
        eventRelatedTables.add("golden_hour");
        eventRelatedTables.add("reward");
        eventRelatedTables.add("participant_event");
        entityRelatedTablesMap.put(Event.class, eventRelatedTables);
        
        // Add more entities and their related tables as needed
    }

    @Override
    public boolean hasRelatedTables(Object entity) {
        if (entity == null) {
            return false;
        }
        
        Class<?> entityClass = getEntityClass(entity);
        return entityRelatedTablesMap.containsKey(entityClass);
    }

    @Override
    public List<String> getRelatedTables(Object entity) {
        if (entity == null) {
            return List.of();
        }
        
        Class<?> entityClass = getEntityClass(entity);
        if (entityRelatedTablesMap.containsKey(entityClass)) {
            return entityRelatedTablesMap.get(entityClass);
        }
        
        log.debug("No related tables found for entity class: {}", entityClass.getName());
        return List.of();
    }
    
    private Class<?> getEntityClass(Object entity) {
        Class<?> entityClass = entity.getClass();
        // Handle proxy objects if needed
        if (entityClass.getName().contains("$")) {
            entityClass = entityClass.getSuperclass();
        }
        return entityClass;
    }
}
