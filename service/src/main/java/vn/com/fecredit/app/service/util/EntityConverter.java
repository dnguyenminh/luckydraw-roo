package vn.com.fecredit.app.service.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableRow;

/**
 * Utility class to convert entities to DataObject instances
 * for use in TableFetchRequest/Response objects
 */
@Component
@Slf4j
public class EntityConverter {

//    private static final Logger log = LoggerFactory.getLogger(EntityConverter.class);

    /**
     * Generic method to convert any entity to a DataObject using reflection
     *
     * @param entity The entity to convert
     * @return A DataObject representing the entity
     */
    public <T> DataObject convertToDataObject(T entity) {
        if (entity == null) {
            return null;
        }

        // Determine the ObjectType from the entity class name
        ObjectType objectType = determineObjectType(entity);

        DataObject dataObject = new DataObject();
        dataObject.setObjectType(objectType);

        TableRow tableRow = new TableRow();
        Map<String, Object> data = extractEntityData(entity);

        tableRow.setData(data);
        dataObject.setData(tableRow);

        return dataObject;
    }

    /**
     * Generic method to convert an entity to a DataObject with explicit ObjectType
     *
     * @param entity     The entity to convert
     * @param objectType The type of the entity
     * @return A DataObject representing the entity
     */
    public <T> DataObject convertToDataObject(T entity, ObjectType objectType) {
        if (entity == null) {
            return null;
        }

        DataObject dataObject = new DataObject();
        dataObject.setObjectType(objectType);

        TableRow tableRow = new TableRow();
        Map<String, Object> data = extractEntityData(entity);

        tableRow.setData(data);
        dataObject.setData(tableRow);

        return dataObject;
    }

    /**
     * Determine ObjectType from entity class name
     *
     * @param entity The entity object
     * @return The matching ObjectType enum value
     */
    private <T> ObjectType determineObjectType(T entity) {
        String className = entity.getClass().getSimpleName();

        // Try to match class name directly with ObjectType enum
        try {
            return ObjectType.valueOf(className);
        } catch (IllegalArgumentException e) {
            // If direct match fails, try other matching strategies
            for (ObjectType type : ObjectType.values()) {
                if (className.equalsIgnoreCase(type.name()) ||
                    className.equalsIgnoreCase(type + "Entity")) {
                    return type;
                }
            }

            // Default fallback
            log.warn("Could not determine ObjectType for entity class: {}, using generic type", className);
            return ObjectType.valueOf(className.replaceAll("Entity$", ""));
        }
    }

    /**
     * Extract entity data into a map using reflection, handling getter methods and fields
     *
     * @param entity The entity to extract data from
     * @return A map containing the entity's properties and values
     */
    private <T> Map<String, Object> extractEntityData(T entity) {
        Map<String, Object> data = new HashMap<>();

        try {
            // First try to use getter methods for better encapsulation
            extractFromGetters(entity, data);

            // Then fall back to fields for any properties we might have missed
            extractFromFields(entity, data);

        } catch (Exception e) {
            log.error("Error extracting entity data: {}", e.getMessage());
        }

        return data;
    }

    /**
     * Extract data using getter methods
     */
    private <T> void extractFromGetters(T entity, Map<String, Object> data) {
        Method[] methods = entity.getClass().getMethods();

        for (Method method : methods) {
            String methodName = method.getName();

            // Skip non-getter methods
            if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
                continue;
            }

            // Skip Object class methods
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            // Skip methods with parameters
            if (method.getParameterCount() > 0) {
                continue;
            }

            try {
                // Convert method name to property name
                String propertyName;
                if (methodName.startsWith("get")) {
                    propertyName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
                } else { // starts with "is"
                    propertyName = methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
                }

                // Skip "class" property from getClass() method
                if ("class".equals(propertyName)) {
                    continue;
                }

                // Get the property value
                Object value = method.invoke(entity);

                // Add non-null values, handling special types
                if (value != null) {
                    processValueForDataMap(propertyName, value, data);
                }
            } catch (Exception e) {
                log.debug("Could not extract property from getter: {}", methodName);
            }
        }
    }

    /**
     * Extract data by accessing fields directly
     */
    private <T> void extractFromFields(T entity, Map<String, Object> data) {
        // Get all declared fields from the class and its superclasses
        Class<?> currentClass = entity.getClass();
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();

                // Skip fields that already have values from getters
                if (data.containsKey(fieldName)) {
                    continue;
                }

                try {
                    Object value = field.get(entity);

                    // Add non-null values, handling special types
                    if (value != null) {
                        processValueForDataMap(fieldName, value, data);
                    }
                } catch (Exception e) {
                    log.debug("Could not extract value from field: {}", fieldName);
                }
            }

            // Move up to the superclass
            currentClass = currentClass.getSuperclass();
        }
    }

    /**
     * Process a value for inclusion in the data map, handling special types
     */
    private void processValueForDataMap(String key, Object value, Map<String, Object> data) {
        // Handle primitive types directly
        if (value instanceof String ||
            value instanceof Number ||
            value instanceof Boolean) {
            data.put(key, value);
            return;
        }

        // Handle enums by converting to string
        if (value instanceof Enum) {
            data.put(key, value.toString());
            return;
        }

        // Handle dates
        if (value instanceof java.util.Date) {
            data.put(key, value);
            return;
        }

        // Handle java.time classes
        if (value instanceof java.time.temporal.Temporal) {
            data.put(key, value.toString());
            return;
        }

        // For complex types, try to get the ID if it's an entity
        try {
            Method idGetter = value.getClass().getMethod("getId");
            Object id = idGetter.invoke(value);
            if (id != null) {
                data.put(key + "Id", id);

                // Also try to get a display name/code for reference
                try {
                    Method nameGetter = null;
                    for (String methodName : new String[]{"getName", "getCode", "getTitle"}) {
                        try {
                            nameGetter = value.getClass().getMethod(methodName);
                            break;
                        } catch (NoSuchMethodException e) {
                            // Try the next method name
                        }
                    }

                    if (nameGetter != null) {
                        Object name = nameGetter.invoke(value);
                        if (name != null) {
                            data.put(key + "Name", name.toString());
                        }
                    }
                } catch (Exception e) {
                    // Ignore if we can't get a name
                }
            }
        } catch (Exception e) {
            // If it's not an entity with an ID, ignore it
            log.debug("Skipping complex property without ID: {}", key);
        }
    }

    /**
     * Get the ID type of the entity class
     *
     * @param currentEntityClass The entity class
     * @return The ID type
     */
    public Type getIdType(Class<?> currentEntityClass) {
        try {
            Map<TypeVariable<?>, Type> typeVarToActualType =
                TypeUtils.getTypeArguments(currentEntityClass, currentEntityClass.getSuperclass());
            Type returnType = currentEntityClass.getMethod("getId").getGenericReturnType();
            if (returnType instanceof TypeVariable) {
                return typeVarToActualType.get(returnType);
            } else {
                return returnType;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean containField(Class<?> entityClass, String fieldName) {
        if (null == fieldName || null == entityClass) {
            return false;
        }
        String getFieldName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            entityClass.getMethod(getFieldName);
            log.debug("{} contain field {}", entityClass.getName(), fieldName);
            return true;
        } catch (NoSuchMethodException e) {
            log.debug("{0} not contain field {1}".formatted(entityClass.getName(), fieldName), e);
            return false;
        }

    }

}
