package vn.com.fecredit.app.controller.util;

import jakarta.persistence.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableRow;

/**
 * Utility class for working with JPA entities in tests
 */
public class EntityUtils {

    /**
     * Converts an entity to a Map containing only primitive and simple properties
     * (not related entities)
     *
     * @param entity The entity to convert
     * @return A Map with only simple entity properties
     */
    public static Map<String, Object> entityToMap(Object entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();

        // Add the ID by default - this is almost always present and important
        try {
            Field idField = getFieldByName(entity.getClass(), "id");
            if (idField != null) {
                idField.setAccessible(true);
                result.put("id", idField.get(entity));
            }
        } catch (Exception e) {
            // Ignore if ID field doesn't exist
        }

        // Iterate through all fields
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            try {
                // Skip static, transient fields and collections/complex objects
                if (Modifier.isStatic(field.getModifiers()) ||
                        Modifier.isTransient(field.getModifiers()) ||
                        Collection.class.isAssignableFrom(field.getType()) ||
                        field.getType().isAnnotationPresent(Entity.class) ||
                        (field.get(entity) != null && field.get(entity).getClass().isAnnotationPresent(Entity.class))) {
                    continue;
                }

                // Include only primitive types, wrappers, and simple value objects
                Object value = field.get(entity);
                if (value != null &&
                        (field.getType().isPrimitive() ||
                                isWrapper(field.getType()) ||
                                value instanceof String ||
                                value instanceof Number ||
                                value instanceof Boolean ||
                                value instanceof java.util.Date ||
                                value instanceof java.time.temporal.Temporal ||
                                value instanceof Enum)) {

                    result.put(field.getName(), value);
                }
            } catch (Exception e) {
                // Skip fields that can't be accessed
            }
        }

        // Alternative method using getters for better compatibility with some entities
        Method[] methods = entity.getClass().getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("get") && !methodName.equals("getClass") && method.getParameterCount() == 0) {
                try {
                    String propertyName = methodName.substring(3);
                    propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
                    
                    // Skip if we already have this property from field reflection
                    if (result.containsKey(propertyName)) {
                        continue;
                    }
                    
                    Object value = method.invoke(entity);
                    
                    // Apply the same filtering as for fields
                    if (value != null && 
                        !(value instanceof Collection) && 
                        !(value.getClass().isAnnotationPresent(Entity.class)) &&
                        (isWrapper(value.getClass()) || 
                         value instanceof String ||
                         value instanceof Number ||
                         value instanceof Boolean ||
                         value instanceof java.util.Date ||
                         value instanceof java.time.temporal.Temporal ||
                         value instanceof Enum)) {
                        result.put(propertyName, value);
                    }
                } catch (Exception e) {
                    // Ignore exceptions during reflection
                }
            }
        }

        return result;
    }

    /**
     * Convert entities to search criteria map for use in TableFetchRequest
     * 
     * @param entities Map of ObjectType to entity object pairs
     * @return Map of ObjectType to DataObject for use as search criteria
     */
    public static Map<ObjectType, DataObject> entitiesToSearchCriteria(Map<ObjectType, Object> entities) {
        Map<ObjectType, DataObject> searchCriteria = new HashMap<>();
        
        if (entities != null) {
            for (Map.Entry<ObjectType, Object> entry : entities.entrySet()) {
                ObjectType objectType = entry.getKey();
                Object entity = entry.getValue();
                
                if (objectType != null && entity != null) {
                    // Convert entity to map of properties
                    Map<String, Object> entityParams = entityToMap(entity);
                    
                    // Create TableRow with entity data
                    TableRow tableRow = new TableRow();
                    tableRow.setData(entityParams);
                    
                    // Create DataObject with the TableRow
                    DataObject dataObject = new DataObject();
                    dataObject.setObjectType(objectType);
                    dataObject.setData(tableRow);
                    
                    // Add to search criteria map
                    searchCriteria.put(objectType, dataObject);
                }
            }
        }
        
        return searchCriteria;
    }

    private static boolean isWrapper(Class<?> clazz) {
        return clazz == Boolean.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class ||
                clazz == Short.class ||
                clazz == Byte.class ||
                clazz == Character.class;
    }

    private static Field getFieldByName(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getFieldByName(superClass, fieldName);
            }
            return null;
        }
    }
}
