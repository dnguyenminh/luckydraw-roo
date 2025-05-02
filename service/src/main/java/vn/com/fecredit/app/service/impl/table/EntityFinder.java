package vn.com.fecredit.app.service.impl.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.com.fecredit.app.service.dto.ColumnInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

/**
 * Responsible for finding entity classes and related metadata
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntityFinder {

    private final RepositoryFactory repositoryFactory;
    private static final String ENTITY_PACKAGE = "vn.com.fecredit.app.entity";

    /**
     * Find the entity class based on entity name or object type
     * 
     * @param entityName The entity name (can be null)
     * @param objectType The object type (can be null)
     * @return The entity class, or null if not found
     */
    public Class<?> findEntityClass(String entityName, ObjectType objectType) {
        // Try to get entity class from ObjectType first
        if (objectType != null) {
            try {
                Class<?> entityClass = repositoryFactory.getEntityClass(objectType);
                log.debug("Found entity class for object type {}: {}", objectType, entityClass.getName());
                return entityClass;
            } catch (IllegalArgumentException e) {
                log.warn("Could not find entity class for object type: {}", objectType);
            }
        }

        // Fall back to entity name if ObjectType was null or not found
        if (entityName != null) {
            try {
                // Try to map entity name to ObjectType enum
                ObjectType mappedObjectType = ObjectType.valueOf(entityName);
                Class<?> entityClass = repositoryFactory.getEntityClass(mappedObjectType);
                log.debug("Found entity class for entity name {}: {}", entityName, entityClass.getName());
                return entityClass;
            } catch (IllegalArgumentException e) {
                // Try direct class lookup by name as last resort
                try {
                    Class<?> entityClass = Class.forName(ENTITY_PACKAGE + "." + entityName);
                    log.debug("Found entity class by direct lookup: {}", entityClass.getName());
                    return entityClass;
                } catch (ClassNotFoundException ex) {
                    log.warn("Could not find entity class for entity name: {}", entityName);
                }
            }
        }

        return null;
    }

    /**
     * Get the table name for an entity class
     * 
     * @param entityClass The entity class
     * @return The table name
     */
    public String getTableName(Class<?> entityClass) {
        if (entityClass == null) {
            return "unknown";
        }

        // First try to get table name from @Table annotation
        if (entityClass.isAnnotationPresent(jakarta.persistence.Table.class)) {
            String tableName = entityClass.getAnnotation(jakarta.persistence.Table.class).name();
            if (tableName != null && !tableName.isEmpty()) {
                log.debug("Found table name from @Table annotation: {}", tableName);
                return tableName;
            }
        }

        // Fall back to default naming convention (entity name -> lowercase + 's')
        String tableName = entityClass.getSimpleName().toLowerCase() + "s";
        log.debug("Using default table name: {}", tableName);
        return tableName;
    }
    
    /**
     * Find primary key fields for an entity class
     * 
     * @param entityClass The entity class to analyze
     * @return List of field names that are part of the primary key
     */
    public List<String> findPrimaryKeyFields(Class<?> entityClass) {
        List<String> pkFields = new ArrayList<>();

        if (entityClass == null) {
            return List.of("id"); // Default fallback
        }

        try {
            // Check for @IdClass annotation (composite key)
            if (entityClass.isAnnotationPresent(jakarta.persistence.IdClass.class)) {
                Class<?> idClass = entityClass.getAnnotation(jakarta.persistence.IdClass.class).value();
                for (Field field : idClass.getDeclaredFields()) {
                    pkFields.add(field.getName());
                }
                log.debug("Found composite key fields from @IdClass: {}", pkFields);
            }

            // Look for fields with @Id annotation
            for (Field field : getAllFields(entityClass)) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    pkFields.add(field.getName());
                    log.debug("Found @Id annotated field: {}", field.getName());
                }
            }

            // If no PK fields were found, default to 'id'
            if (pkFields.isEmpty()) {
                pkFields.add("id");
                log.debug("No primary key fields found, defaulting to 'id'");
            }
        } catch (Exception e) {
            log.warn("Error finding primary key fields for class {}: {}", 
                entityClass.getName(), e.getMessage());
            pkFields.add("id"); // Default to 'id' on error
        }

        return pkFields;
    }

    /**
     * Get all fields from class and all its superclasses
     */
    public List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * Get default columns for an entity class
     * 
     * @param entityClass The entity class to analyze
     * @return Map of field names to column info
     */
    public Map<String, ColumnInfo> getDefaultColumns(Class<?> entityClass) {
        Map<String, ColumnInfo> columns = new HashMap<>();

        try {
            List<Field> fields = getAllFields(entityClass);
            List<String> pkFields = findPrimaryKeyFields(entityClass);

            for (Field field : fields) {
                // Skip static and transient fields
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                // Create column info
                ColumnInfo columnInfo = ColumnInfo.builder()
                    .fieldName(field.getName())
                    .fieldType(field.getType().getSimpleName())
                    .editable(!pkFields.contains(field.getName())) // Primary key fields are not editable
                    .build();

                columns.put(field.getName(), columnInfo);
            }
        } catch (Exception e) {
            log.error("Error getting default columns for {}: {}", entityClass.getName(), e.getMessage());
        }

        return columns;
    }
}
