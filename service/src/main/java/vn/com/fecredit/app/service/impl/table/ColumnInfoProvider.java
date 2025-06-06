package vn.com.fecredit.app.service.impl.table;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.FieldType;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class ColumnInfoProvider {    private final EntityManager entityManager;
    private final vn.com.fecredit.app.service.impl.table.EntityManager customEntityManager;

    // Add expiration to caches with 1-hour timeout
    private final Map<Class<?>, CachedValue<Map<String, ColumnInfo>>> columnInfoCache = new ConcurrentHashMap<>();
    private final Map<ObjectType, CachedValue<Map<String, ColumnInfo>>> objectTypeColumnInfoCache = new ConcurrentHashMap<>();

    /**
     * Gets column information for an entity class with improved caching
     */
    public Map<String, ColumnInfo> getColumnInfo(Class<?> entityClass) {
        if (entityClass == null) {
            return new HashMap<>();
        }

        CachedValue<Map<String, ColumnInfo>> cached = columnInfoCache.get(entityClass);
        if (cached != null && !cached.isExpired()) {
            return cached.getValue();
        }

        Map<String, ColumnInfo> columns = buildColumnInfo(entityClass);
        columnInfoCache.put(entityClass, new CachedValue<>(columns, 1, TimeUnit.HOURS));
        return columns;
    }

    /**
     * Determine field type name based on Java class
     */
    public FieldType determineFieldType(Class<?> javaType) {
        if (javaType == null) {
            return FieldType.STRING;
        }

        if (Number.class.isAssignableFrom(javaType) ||
            javaType == int.class ||
            javaType == long.class ||
            javaType == double.class ||
            javaType == float.class) {
            return FieldType.NUMBER;
        }

        if (java.util.Date.class.isAssignableFrom(javaType) ||
            java.time.temporal.Temporal.class.isAssignableFrom(javaType)) {
            return FieldType.DATETIME;
        }

        if (Boolean.class.isAssignableFrom(javaType) || javaType == boolean.class) {
            return FieldType.BOOLEAN;
        }

        return FieldType.STRING;
    }

    /**
     * Build column info with optimized processing and fewer exceptions
     */
    private Map<String, ColumnInfo> buildColumnInfo(Class<?> entityClass) {
        Map<String, ColumnInfo> columns = new HashMap<>();

        try {
            // Get entity metadata from JPA
            EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);

            // Process each attribute
            for (Attribute<?, ?> attribute : entityType.getAttributes()) {
                String fieldName = attribute.getName();
                String displayName = getDisplayName(entityClass, fieldName);

                columns.put(fieldName, ColumnInfo.builder()
                    .fieldName(fieldName)
                    .displayName(displayName)
                    .fieldType(determineFieldType(attribute.getJavaType()))
                    .build());
            }
        } catch (Exception e) {
            log.warn("Error getting column info from JPA metamodel for {}: {}",
                entityClass.getName(), e.getMessage());

            // Fall back to reflection without exceptions
            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isSynthetic()) continue; // Skip synthetic fields

                String fieldName = field.getName();
                String displayName = getDisplayName(entityClass, fieldName);

                columns.put(fieldName, ColumnInfo.builder()
                    .fieldName(fieldName)
                    .displayName(displayName)
                    .fieldType(determineFieldType(field.getType()))
                    .build());
            }
        }

        return columns;
    }

    /**
     * Gets column information for an object type with caching
     */
    public Map<String, ColumnInfo> getColumnInfo(ObjectType objectType, TableFetchRequest request) {
        if (objectType == null) {
            return new HashMap<>();
        }

        CachedValue<Map<String, ColumnInfo>> cached = objectTypeColumnInfoCache.get(objectType);
        if (cached != null && !cached.isExpired()) {
            return cached.getValue();
        }        Map<String, ColumnInfo> columns = new HashMap<>();
        try {
            Class<?> entityClass = customEntityManager.resolveEntityClass(null, objectType);

            if (entityClass != null) {
                columns = getColumnInfo(entityClass);
                columns.forEach((key, columnInfo) -> {
                    columnInfo.setObjectType(objectType);
                });
            }
        } catch (Exception e) {
            log.warn("Error getting column info for object type {}: {}", objectType, e.getMessage());
        }

        objectTypeColumnInfoCache.put(objectType, new CachedValue<>(columns, 1, TimeUnit.HOURS));
        return columns;
    }

    /**
     * Custom cache value with expiration support
     */
    private static class CachedValue<T> {
        private final T value;
        private final long expirationTime;

        public CachedValue(T value, long duration, TimeUnit unit) {
            this.value = value;
            this.expirationTime = System.currentTimeMillis() + unit.toMillis(duration);
        }

        public T getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    /**
     * Clears all caches - useful for testing
     */
    public void clearCaches() {
        columnInfoCache.clear();
        objectTypeColumnInfoCache.clear();
    }

    /**
     * Gets display name for a field
     */
    private String getDisplayName(Class<?> entityClass, String fieldName) {
        try {
            Field field = entityClass.getDeclaredField(fieldName);
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
                return columnAnnotation.name();
            }
        } catch (NoSuchFieldException e) {
            log.debug("Field {} not found in class {}", fieldName, entityClass.getName());
        }
        return formatAsTitleCase(fieldName);
    }

    /**
     * Formats a camelCase string as Title Case
     * e.g., "firstName" -> "First Name"
     */
    private String formatAsTitleCase(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }

        // Insert space before capital letters and capitalize first letter
        StringBuilder result = new StringBuilder();
        result.append(Character.toUpperCase(fieldName.charAt(0)));

        for (int i = 1; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append(' ');
            }
            result.append(c);
        }

        return result.toString();
    }
}
