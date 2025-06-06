package vn.com.fecredit.app.service.impl.table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import vn.com.fecredit.app.service.dto.*;
import vn.com.fecredit.app.service.factory.RelatedTablesFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles conversion between entities and DTOs and provides utility methods for entity operations.
 * 
 * @deprecated This class has been partially replaced by the enhanced EntityManager class.
 * It is maintained for backward compatibility but will be removed once the refactoring is complete.
 * Use the more efficient EntityManager class for new code.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EntityConverter {
    
    /**
     * @deprecated Use EntityManager's implementation instead.
     */
    @Deprecated
    private static final String DEPRECATION_MESSAGE = "This method has been moved to EntityManager. Use EntityManager instead.";

    private final RelatedTablesFactory relatedTablesFactory;    /**
     * Check if a class contains a field with the given name
     * Searches through the class hierarchy (including superclasses)
     *
     * @param clazz     The class to check
     * @param fieldName The name of the field to look for
     * @return true if the field exists, false otherwise
     * @deprecated Use {@link vn.com.fecredit.app.service.impl.table.EntityManager#containsField(Class, String)} instead.
     */
    @Deprecated
    public static boolean containField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return false;
        }

        // Check all fields in this class
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }

        // Check superclass recursively
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            return containField(superClass, fieldName);
        }

        return false;
    }    /**
     * Get entity class from ObjectType
     * 
     * @param objectType The object type enum
     * @return The corresponding entity class or null if not found
     * @deprecated Use {@link vn.com.fecredit.app.service.impl.table.EntityManager#findEntityClass(String, ObjectType)} instead.
     */
    @Deprecated
    public static Class<?> getEntityClassFromObjectType(ObjectType objectType) {
        try {
            return Class.forName("vn.com.fecredit.app.entity." + objectType.name());
        } catch (ClassNotFoundException e) {
            try {
                // Try with lowercase first letter
                String className = objectType.name();
                className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
                return Class.forName("vn.com.fecredit.app.entity." + className);
            } catch (ClassNotFoundException ex) {
                return null;
            }
        }
    }

    /**
     * Convert database tuples to table rows
     */
    public List<TableRow> convertTuplesToRows(List<jakarta.persistence.Tuple> tuples, 
            List<ColumnInfo> viewColumns) {
        // Use a map to deduplicate by ID
        Map<Object, TableRow> uniqueRows = new LinkedHashMap<>();

        // Create set of requested field names for validation
        Set<String> requestedFieldNames = viewColumns.stream()
                .map(ColumnInfo::getFieldName)
                .collect(Collectors.toSet());

        // Always include ID in requested fields (for internal processing)
        boolean idRequested = requestedFieldNames.contains("id");

        for (jakarta.persistence.Tuple tuple : tuples) {
            Object id = tuple.get("id");

            // Skip if ID is null or already processed
            if (id == null || uniqueRows.containsKey(id)) {
                continue;
            }

            TableRow row = new TableRow();
            Map<String, Object> data = new HashMap<>();

            // We need to strictly include only the requested fields
            for (ColumnInfo column : viewColumns) {
                String fieldName = column.getFieldName();
                try {
                    Object value = tuple.get(fieldName);
                    if (value != null) {
                        data.put(fieldName, value);
                    }
                } catch (IllegalArgumentException e) {
                    log.debug("Field not found in result: {}", fieldName);
                }
            }

            // Add ID only if it was requested
            if (idRequested && !data.containsKey("id")) {
                data.put("id", id);
            }

            // Always include viewId for internal table processing
            data.put("viewId", id.hashCode());

            row.setData(data);
            uniqueRows.put(id, row);
        }

        // Return the list of unique rows
        return new ArrayList<>(uniqueRows.values());
    }

    /**
     * Converts a tuple to a table row
     * @param tuple The tuple to convert
     * @param fieldNameMapping Optional mapping from alias to original field name
     * @return The converted table row
     */
    public TableRow convertTupleToTableRow(Tuple tuple, Map<String, String> fieldNameMapping) {
        Map<String, Object> data = new HashMap<>();
        
        for (TupleElement<?> element : tuple.getElements()) {
            String alias = element.getAlias();
            Object value = tuple.get(alias);
            
            // Use the original field name if available in the mapping
            String fieldName = fieldNameMapping != null && fieldNameMapping.containsKey(alias) ? 
                              fieldNameMapping.get(alias) : alias;
            
            data.put(fieldName, value);
        }
        
        TableRow tableRow = new TableRow();
        tableRow.setData(data);
        return tableRow;
    }

    /**
     * Convert an entity to a table row using reflection
     */
    public <T> TableRow convertEntityToTableRow(T entity) {
        Map<String, Object> data = new HashMap<>();

        if (entity == null) {
            return new TableRow();
        }

        log.debug("Processing entity of type: {}", entity.getClass().getName());
        // Extract data using reflection
        Object entityId = extractEntityData(entity, data);

        // Add viewId based on entity's ID
        if (entityId != null) {
            int viewId = entityId.hashCode();
            data.put("viewId", viewId);
            log.debug("Added viewId: {} based on entity ID: {}", viewId, entityId);
        }

        // Check if the entity has related tables
        if (relatedTablesFactory.hasRelatedTables(entity)) {
            TabTableRow tabRow = new TabTableRow(data);

            // Add related tables from the factory
            List<Class<?>> relatedEntities = relatedTablesFactory.getRelatedEntityClasses(entity);
            for (Class<?> entityClass : relatedEntities) {
                tabRow.addRelatedTable(entityClass.getSimpleName());
            }

            return tabRow;
        } else {
            TableRow row = new TableRow();
            row.setData(data);
            return row;
        }
    }

    /**
     * Extract entity data using reflection
     * 
     * @return The entity ID
     */
    private <T> Object extractEntityData(T entity, Map<String, Object> data) {
        Method[] methods = entity.getClass().getMethods();
        Object entityId = null;

        for (Method method : methods) {
            String methodName = method.getName();

            // Skip if method is a getter for an entity type
            if (isEntityGetter(method)) {
                continue;
            }

            // Process getter methods
            if (methodName.startsWith("get") &&
                    !methodName.equals("getClass") &&
                    method.getParameterCount() == 0) {

                // Skip temporaryAttributes
                if (methodName.equals("getTemporaryAttributes")) {
                    continue;
                }

                try {
                    // Extract property name from getter
                    String propertyName = methodName.substring(3);
                    propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);

                    // Skip known entity relationships
                    if (isKnownRelationship(propertyName)) {
                        continue;
                    }

                    // Invoke the getter to get the value
                    Object value = method.invoke(entity);

                    // Save the ID for generating viewId
                    if (propertyName.equals("id")) {
                        entityId = value;
                    }

                    // Add property and value to the data map
                    data.put(propertyName, value);
                } catch (Exception e) {
                    log.warn("Failed to extract property via method {}: {}", methodName, e.getMessage());
                }
            }
            // Handle boolean getters
            else if ((methodName.startsWith("is") || methodName.startsWith("has")) &&
                    method.getParameterCount() == 0 &&
                    (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                
                try {
                    // Extract property name
                    String propertyName = methodName.startsWith("is") ? 
                            methodName.substring(2) : methodName.substring(3);
                    propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);

                    // Invoke to get the value
                    Object value = method.invoke(entity);
                    data.put(propertyName, value);
                } catch (Exception e) {
                    log.warn("Failed to extract boolean property via {}: {}", methodName, e.getMessage());
                }
            }
        }

        // Remove any entity objects that may have slipped through
        removeEntityObjects(data);

        return entityId;
    }

    /**
     * Remove any entity objects from the data map
     */
    private void removeEntityObjects(Map<String, Object> data) {
        List<String> keysToRemove = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            if (value != null && isEntityObject(value)) {
                keysToRemove.add(entry.getKey());
            }
        }

        for (String key : keysToRemove) {
            data.remove(key);
        }
    }

    /**
     * Check if a method is a getter for an entity type
     */
    private boolean isEntityGetter(Method method) {
        // Basic checks for getter pattern
        if (method.getParameterCount() > 0 || 
            !method.getName().startsWith("get") || 
            method.getName().equals("getClass")) {
            return false;
        }

        Class<?> returnType = method.getReturnType();

        // Check for entity annotation
        if (returnType.isAnnotationPresent(jakarta.persistence.Entity.class)) {
            return true;
        }

        // Check for collection of entities
        if (Collection.class.isAssignableFrom(returnType)) {
            try {
                java.lang.reflect.Type genericType = method.getGenericReturnType();
                if (genericType instanceof java.lang.reflect.ParameterizedType) {
                    java.lang.reflect.ParameterizedType paramType = 
                            (java.lang.reflect.ParameterizedType) genericType;
                    java.lang.reflect.Type[] typeArgs = paramType.getActualTypeArguments();
                    
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        Class<?> itemType = (Class<?>) typeArgs[0];
                        if (itemType.isAnnotationPresent(jakarta.persistence.Entity.class)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore errors in generic type analysis
            }
        }

        // Check known entity names by convention
        String propertyName = method.getName().substring(3).toLowerCase();
        return isKnownRelationship(propertyName);
    }
    
    /**
     * Check if a property name is a known relationship field
     */
    private boolean isKnownRelationship(String propertyName) {
        return propertyName.equals("user") || 
               propertyName.equals("role") ||
               propertyName.equals("event") || 
               propertyName.equals("participant") ||
               propertyName.equals("reward") ||
               propertyName.equals("permission");
    }

    /**
     * Check if an object represents a JPA entity
     */
    private boolean isEntityObject(Object obj) {
        // Check for entity annotation
        if (obj.getClass().isAnnotationPresent(jakarta.persistence.Entity.class)) {
            return true;
        }

        // Check for getId method (common in entities)
        try {
            Method getId = obj.getClass().getMethod("getId");
            if (getId != null) {
                return true;
            }
        } catch (NoSuchMethodException e) {
            // Not an entity or doesn't follow standard pattern
        }

        return false;
    }

    /**
     * Filter a row's data properties based on view columns
     */
    public void filterRowByViewColumns(TableRow row, List<ColumnInfo> viewColumns) {
        if (row == null || row.getData() == null || viewColumns == null || viewColumns.isEmpty()) {
            return;
        }

        // Create a map of field names from view columns
        Map<String, ColumnInfo> viewColumnMap = createViewColumnMap(viewColumns);

        // Find keys to remove (not in view columns)
        Set<String> keysToRemove = findKeysToRemove(row.getData().keySet(), viewColumnMap);
        row.getData().keySet().removeAll(keysToRemove);

        // Create ordered result if needed
        if (!viewColumns.isEmpty()) {
            reorderDataByColumns(row, viewColumns, viewColumnMap);
        }
    }

    /**
     * Create a map of field names from view columns
     */
    private Map<String, ColumnInfo> createViewColumnMap(List<ColumnInfo> viewColumns) {
        Map<String, ColumnInfo> viewColumnMap = new HashMap<>();
        
        for (ColumnInfo columnInfo : viewColumns) {
            String fieldName = columnInfo.getFieldName();
            viewColumnMap.put(fieldName, columnInfo);

            // Handle wildcard patterns
            if (fieldName.endsWith(".*")) {
                String prefix = fieldName.substring(0, fieldName.length() - 2);
                viewColumnMap.put(prefix + ".*", columnInfo);
            }
        }
        
        return viewColumnMap;
    }

    /**
     * Find keys to remove (not in view columns)
     */
    private Set<String> findKeysToRemove(Set<String> dataKeys, Map<String, ColumnInfo> viewColumnMap) {
        return dataKeys.stream()
            .filter(key -> {
                // Always keep id and viewId
                if ("id".equals(key) || "viewId".equals(key)) {
                    return false;
                }
                
                // Keep if exact match in columns
                if (viewColumnMap.containsKey(key)) {
                    return false;
                }
                
                // Check wildcard patterns
                return !viewColumnMap.entrySet().stream().anyMatch(entry -> {
                    String pattern = entry.getKey();
                    return pattern.endsWith(".*") && 
                           key.startsWith(pattern.substring(0, pattern.length() - 2) + ".");
                });
            })
            .collect(Collectors.toSet());
    }

    /**
     * Reorder data based on column order
     */
    private void reorderDataByColumns(
            TableRow row, 
            List<ColumnInfo> viewColumns,
            Map<String, ColumnInfo> viewColumnMap) {
            
        Map<String, Object> orderedData = new LinkedHashMap<>();

        // Always include id and viewId first if present
        if (row.getData().containsKey("id")) {
            orderedData.put("id", row.getData().get("id"));
        }
        if (row.getData().containsKey("viewId")) {
            orderedData.put("viewId", row.getData().get("viewId"));
        }

        // Add fields in order from view columns
        for (ColumnInfo columnInfo : viewColumns) {
            String fieldName = columnInfo.getFieldName();

            // Handle exact match
            if (row.getData().containsKey(fieldName)) {
                orderedData.put(fieldName, row.getData().get(fieldName));
            }

            // Handle wildcard patterns
            if (fieldName.endsWith(".*")) {
                String prefix = fieldName.substring(0, fieldName.length() - 2);
                
                // Find matching fields sorted alphabetically
                List<String> matchingFields = row.getData().keySet().stream()
                        .filter(k -> k.startsWith(prefix + "."))
                        .sorted()
                        .collect(Collectors.toList());

                for (String matchingField : matchingFields) {
                    orderedData.put(matchingField, row.getData().get(matchingField));
                }
            }
        }

        // Replace with ordered data
        row.setData(orderedData);
    }

    /**
     * Get the type of the ID field for an entity class
     * 
     * @param entityClass The entity class
     * @return The class type of the ID field (typically Long or Integer)
     */
    public <T> Class<?> getIdType(Class<T> entityClass) {
        try {
            // Try to find the id field by name
            for (Field field : getAllFields(entityClass)) {
                if ("id".equals(field.getName())) {
                    return field.getType();
                }
            }
            
            // If not found by name, check for field with @Id annotation
            for (Field field : getAllFields(entityClass)) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    return field.getType();
                }
            }
            
            // Fall back to Long as default ID type
            log.warn("Could not determine ID type for {}, using Long as default", entityClass.getSimpleName());
            return Long.class;
        } catch (Exception e) {
            log.error("Error getting ID type for {}", entityClass.getSimpleName(), e);
            return Long.class;
        }
    }

    /**
     * Get all fields from a class and its superclasses
     * 
     * @param clazz The class to get fields from
     * @return List of all fields including inherited ones
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        
        return fields;
    }

    /**
     * Convert an entity object to a DataObject for search criteria
     */
    public <T> DataObject convertToDataObject(T entity) {
        if (entity == null) {
            return null;
        }
        
        // Create new DataObject
        DataObject dataObject = new DataObject();
        
        try {
            // Try to determine the ObjectType from entity class name
            String entityClassName = entity.getClass().getSimpleName();
            try {
                ObjectType objectType = ObjectType.valueOf(entityClassName);
                dataObject.setObjectType(objectType);
            } catch (IllegalArgumentException e) {
                log.warn("Could not convert entity class name to ObjectType: {}", entityClassName);
                // Instead of using setObjectTypeName which doesn't exist, we'll just log the warning
                // The object type will remain null in this case
            }
            
            // Use convertEntityToTableRow to get the data
            TableRow tableRow = convertEntityToTableRow(entity);
            dataObject.setData(tableRow);
            
        } catch (Exception e) {
            log.error("Error converting entity to DataObject", e);
        }
        
        return dataObject;
    }
}
