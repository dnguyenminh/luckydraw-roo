package vn.com.fecredit.app.service.impl.table;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class ColumnInfoProvider {

    private final EntityManager entityManager;
    private final EntityHandler entityHandler;

    // Cache for column info by class
    private final ConcurrentHashMap<Class<?>, Map<String, ColumnInfo>> columnInfoCache = new ConcurrentHashMap<>();

    // Cache for column info by object type
    private final ConcurrentHashMap<ObjectType, Map<String, ColumnInfo>> objectTypeColumnInfoCache = new ConcurrentHashMap<>();

    /**
     * Gets column information for an entity class with caching
     */
    public Map<String, ColumnInfo> getColumnInfo(Class<?> entityClass) {
        if (entityClass == null) {
            return new HashMap<>();
        }

        return columnInfoCache.computeIfAbsent(entityClass, clazz -> {
            Map<String, ColumnInfo> columns = new HashMap<>();

            try {
                // Get entity metadata from JPA
                EntityType<?> entityType = entityManager.getMetamodel().entity(clazz);

                // Process each attribute
                for (Attribute<?, ?> attribute : entityType.getAttributes()) {
                    String fieldName = attribute.getName();

                    // Use builder pattern for ColumnInfo
                    ColumnInfo columnInfo = ColumnInfo.builder()
                            .fieldName(fieldName)
                            .build();
                    columns.put(fieldName, columnInfo);
                }
            } catch (Exception e) {
                log.warn("Error getting column info for {}: {}",
                        entityClass.getName(), e.getMessage());

                // Fall back to reflection
                Field[] fields = entityClass.getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();

                    // Use builder pattern for ColumnInfo
                    ColumnInfo columnInfo = ColumnInfo.builder()
                            .fieldName(fieldName)
                            .build();
                    columns.put(fieldName, columnInfo);
                }
            }

            return columns;
        });
    }

    /**
     * Gets column information for an object type with caching
     */
    public Map<String, ColumnInfo> getColumnInfo(ObjectType objectType, TableFetchRequest request) {
        if (objectType == null) {
            return new HashMap<>();
        }

        return objectTypeColumnInfoCache.computeIfAbsent(objectType, type -> {
            try {
                Class<?> entityClass = entityHandler.resolveEntityClass(null, type);

                if (entityClass != null) {
                    return getColumnInfo(entityClass);
                }
            } catch (Exception e) {
                log.warn("Error getting column info for object type {}: {}",
                        objectType, e.getMessage());
            }

            return new HashMap<>();
        });
    }

    // /**
    //  * Formats a camelCase string as Title Case
    //  * e.g., "firstName" -> "First Name"
    //  */
    // private String formatAsTitleCase(String fieldName) {
    //     if (fieldName == null || fieldName.isEmpty()) {
    //         return fieldName;
    //     }

    //     // Insert space before capital letters and capitalize first letter
    //     StringBuilder result = new StringBuilder();
    //     result.append(Character.toUpperCase(fieldName.charAt(0)));

    //     for (int i = 1; i < fieldName.length(); i++) {
    //         char c = fieldName.charAt(i);
    //         if (Character.isUpperCase(c)) {
    //             result.append(' ');
    //         }
    //         result.append(c);
    //     }

    //     return result.toString();
    // }

    // /**
    //  * Finds the getter method for a field
    //  */
    // private Method findGetterMethod(Class<?> entityClass, String fieldName) {
    //     if (fieldName == null || fieldName.isEmpty()) {
    //         return null;
    //     }

    //     // First letter to upper case
    //     String capitalizedField = Character.toUpperCase(fieldName.charAt(0)) +
    //             (fieldName.length() > 1 ? fieldName.substring(1) : "");

    //     // Try getter methods
    //     try {
    //         return entityClass.getMethod("get" + capitalizedField);
    //     } catch (NoSuchMethodException e) {
    //         try {
    //             return entityClass.getMethod("is" + capitalizedField);
    //         } catch (NoSuchMethodException ex) {
    //             return null;
    //         }
    //     }
    // }

    /**
     * Clears all caches - useful for testing
     */
    public void clearCaches() {
        columnInfoCache.clear();
        objectTypeColumnInfoCache.clear();
    }
}
