package vn.com.fecredit.app.service.impl.table;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ObjectType;

@Slf4j
@Component
@RequiredArgsConstructor
public class EntityHandler {

    private final EntityManager entityManager;
    
    // Cache for entity classes by ObjectType
    private final ConcurrentHashMap<ObjectType, Class<?>> entityClassCache = new ConcurrentHashMap<>();
    
    // Cache for table names by entity class
    private final ConcurrentHashMap<Class<?>, String> tableNameCache = new ConcurrentHashMap<>();

    /**
     * Resolves an entity class from ObjectType with caching
     */
    public Class<?> resolveEntityClass(Class<?> defaultClass, ObjectType objectType) {
        if (objectType == null) {
            return defaultClass;
        }
        
        return entityClassCache.computeIfAbsent(objectType, type -> {
            try {
                // Try to find entity class based on ObjectType name
                String entityName = determineEntityClassName(type);
                log.debug("Looking for entity class: {}", entityName);
                Class<?> entityClass = Class.forName(entityName);
                log.debug("Found entity class for object type {}: {}", type, entityClass.getName());
                return entityClass;
            } catch (ClassNotFoundException e) {
                log.warn("Could not find entity class for object type: {}", type);
                return defaultClass;
            }
        });
    }

    /**
     * Gets the table name for an entity class with caching
     */
    public String getTableName(Class<?> entityClass) {
        if (entityClass == null) {
            return null;
        }
        
        return tableNameCache.computeIfAbsent(entityClass, clazz -> {
            // First try to get the table name from @Table annotation
            Table tableAnnotation = clazz.getAnnotation(Table.class);
            if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
                return tableAnnotation.name();
            }
            
            // Then try to get it from JPA metamodel
            try {
                EntityType<?> entityType = entityManager.getMetamodel().entity(clazz);
                return entityType.getName().toLowerCase() + "s"; // Default pluralization
            } catch (Exception e) {
                // Fall back to class simple name with plural 's'
                return clazz.getSimpleName().toLowerCase() + "s";
            }
        });
    }
    
    /**
     * Improves package scanning for entity classes
     */
    private String determineEntityClassName(ObjectType objectType) {
        // Add more potential packages and scan them in priority order
        String[] possiblePackages = {
            "vn.com.fecredit.app.entity",
            "vn.com.fecredit.app.domain",
            "vn.com.fecredit.app.model",
            "vn.com.fecredit.core.entity"
        };
        
        for (String pkg : possiblePackages) {
            String fullClassName = pkg + "." + objectType.name();
            try {
                Class.forName(fullClassName);
                return fullClassName;
            } catch (ClassNotFoundException e) {
                // Try next package
            }
        }
        
        // Default to entity package
        return "vn.com.fecredit.app.entity." + objectType.name();
    }
    
    /**
     * Clears all caches - useful for testing
     */
    public void clearCaches() {
        entityClassCache.clear();
        tableNameCache.clear();
    }
}
