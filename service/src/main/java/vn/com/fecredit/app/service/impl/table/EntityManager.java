package vn.com.fecredit.app.service.impl.table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.factory.RelatedTablesFactory;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

/**
 * Unified class that manages all entity-related operations.
 * 
 * This class efficiently consolidates functionality that was previously spread across:
 * - EntityFinder: Finding entity classes and metadata
 * - EntityHandler: Managing entity class resolution and table names
 * - EntityConverter: Converting between entities and DTOs
 *
 * The consolidation eliminates duplicate code, improves caching,
 * and provides a single interface for all entity-related operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EntityManager {

    private final jakarta.persistence.EntityManager jpaEntityManager;
    private final RepositoryFactory repositoryFactory;
    private final RelatedTablesFactory relatedTablesFactory;
    
    // Centralized caches for better performance
    private final ConcurrentHashMap<ObjectType, Class<?>> entityClassCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, String> tableNameCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, List<String>> primaryKeyFieldsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> fieldExistenceCache = new ConcurrentHashMap<>();
    
    // Standardized package locations to search for entities
    private static final String ENTITY_PACKAGE = "vn.com.fecredit.app.entity";
    private static final String[] ADDITIONAL_ENTITY_PACKAGES = {
        "vn.com.fecredit.app.domain",
        "vn.com.fecredit.app.model",
        "vn.com.fecredit.core.entity"
    };

    /**
     * Unified method to find entity class based on entity name or object type.
     * This method consolidates the functionality from EntityFinder and EntityConverter.
     *
     * @param entityName The entity name (can be null)
     * @param objectType The object type (can be null)
     * @return The entity class, or null if not found
     */
    public Class<?> findEntityClass(String entityName, ObjectType objectType) {
        // First try using ObjectType with cache
        if (objectType != null) {
            Class<?> cachedClass = entityClassCache.get(objectType);
            if (cachedClass != null) {
                return cachedClass;
            }

            try {
                // Try to get from repository factory first
                Class<?> entityClass = repositoryFactory.getEntityClass(objectType);
                if (entityClass != null) {
                    entityClassCache.put(objectType, entityClass);
                    log.debug("Found entity class for object type {} from repository: {}", 
                            objectType, entityClass.getName());
                    return entityClass;
                }
            } catch (Exception e) {
                // Continue to other methods if this fails
                log.debug("Could not find entity class for object type {} in repository: {}", 
                        objectType, e.getMessage());
            }
            
            // Try standard class name resolution with various formats
            Class<?> entityClass = findEntityClassByObjectType(objectType);
            if (entityClass != null) {
                entityClassCache.put(objectType, entityClass);
                return entityClass;
            }
        }

        // Fall back to entity name if ObjectType was null or not found
        if (entityName != null) {
            try {
                // Try to map entity name to ObjectType enum
                ObjectType mappedObjectType = ObjectType.valueOf(entityName);
                return findEntityClass(null, mappedObjectType); // Recursive call with the mapped type
            } catch (IllegalArgumentException e) {
                // Try direct class lookup by name as last resort
                try {
                    Class<?> entityClass = Class.forName(ENTITY_PACKAGE + "." + entityName);
                    log.debug("Found entity class by direct lookup: {}", entityClass.getName());
                    return entityClass;
                } catch (ClassNotFoundException ex) {
                    // Try with first letter lowercase
                    String lowerCaseFirstChar = Character.toLowerCase(entityName.charAt(0)) + 
                            entityName.substring(1);
                    try {
                        Class<?> entityClass = Class.forName(ENTITY_PACKAGE + "." + lowerCaseFirstChar);
                        log.debug("Found entity class by direct lookup with lowercase: {}", 
                                entityClass.getName());
                        return entityClass;
                    } catch (ClassNotFoundException exc) {
                        log.warn("Could not find entity class for entity name: {}", entityName);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Helper method to try finding entity class by ObjectType with multiple strategies
     * 
     * @param objectType The object type enum
     * @return The corresponding entity class or null if not found
     */
    private Class<?> findEntityClassByObjectType(ObjectType objectType) {
        // Try standard name first
        try {
            Class<?> entityClass = Class.forName(ENTITY_PACKAGE + "." + objectType.name());
            log.debug("Found entity class for {} at {}", objectType, entityClass.getName());
            return entityClass;
        } catch (ClassNotFoundException e) {
            // Continue to next strategy
        }
        
        // Try with lowercase first letter
        try {
            String className = objectType.name();
            className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
            Class<?> entityClass = Class.forName(ENTITY_PACKAGE + "." + className);
            log.debug("Found entity class for {} at {} (lowercase first char)", 
                    objectType, entityClass.getName());
            return entityClass;
        } catch (ClassNotFoundException e) {
            // Continue to next strategy
        }
        
        // Try in additional packages
        for (String packageName : ADDITIONAL_ENTITY_PACKAGES) {
            try {
                Class<?> entityClass = Class.forName(packageName + "." + objectType.name());
                log.debug("Found entity class for {} at {}", objectType, entityClass.getName());
                return entityClass;
            } catch (ClassNotFoundException e) {
                // Try lowercase in this package
                try {
                    String className = objectType.name();
                    className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
                    Class<?> entityClass = Class.forName(packageName + "." + className);
                    log.debug("Found entity class for {} at {} (lowercase first char)", 
                            objectType, entityClass.getName());
                    return entityClass;
                } catch (ClassNotFoundException ex) {
                    // Continue to next package
                }
            }
        }
        
        log.warn("Could not find entity class for object type: {}", objectType);
        return null;
    }

    /**
     * Simplified method to resolve entity class from ObjectType with cache.
     * 
     * @param defaultClass Fallback class if the object type cannot be resolved
     * @param objectType Object type to resolve
     * @return The resolved entity class or the default class
     */
    public Class<?> resolveEntityClass(Class<?> defaultClass, ObjectType objectType) {
        if (objectType == null) {
            return defaultClass;
        }
        
        Class<?> entityClass = findEntityClass(null, objectType);
        return entityClass != null ? entityClass : defaultClass;
    }

    /**
     * Gets the table name for an entity class with caching.
     * This method consolidates table name resolution from EntityHandler and EntityFinder.
     *
     * @param entityClass The entity class to get the table name for
     * @return The table name or null if entity class is null
     */
    public String getTableName(Class<?> entityClass) {
        if (entityClass == null) {
            return null;
        }
        
        return tableNameCache.computeIfAbsent(entityClass, clazz -> {
            // First try to get the table name from @Table annotation
            Table tableAnnotation = clazz.getAnnotation(Table.class);
            if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
                log.debug("Found table name from @Table annotation for {}: {}", 
                        clazz.getSimpleName(), tableAnnotation.name());
                return tableAnnotation.name();
            }
            
            // Then try to get it from JPA metamodel
            try {
                EntityType<?> entityType = jpaEntityManager.getMetamodel().entity(clazz);
                String tableName = entityType.getName();
                
                // Check if the name already ends with 's' to avoid double pluralization
                if (!tableName.toLowerCase().endsWith("s")) {
                    tableName = tableName + "s";
                }
                
                log.debug("Found table name from JPA metamodel for {}: {}", 
                        clazz.getSimpleName(), tableName.toLowerCase());
                return tableName.toLowerCase();
            } catch (Exception e) {
                // Fall back to class simple name with plural 's'
                String simpleName = clazz.getSimpleName();
                String tableName = simpleName.toLowerCase();
                
                // Check if the name already ends with 's' to avoid double pluralization
                if (!tableName.endsWith("s")) {
                    tableName = tableName + "s";
                }
                
                log.debug("Using default table name for {}: {}", 
                        clazz.getSimpleName(), tableName);
                return tableName;
            }
        });
    }
    
    /**
     * Find primary key fields for an entity class with caching.
     * This method consolidates functionality from EntityFinder.
     *
     * @param entityClass The entity class to analyze
     * @return List of field names that are part of the primary key
     */
    public List<String> findPrimaryKeyFields(Class<?> entityClass) {
        if (entityClass == null) {
            return new ArrayList<>();
        }
        
        // Use cache for better performance
        return primaryKeyFieldsCache.computeIfAbsent(entityClass, clazz -> {
            List<String> pkFields = new ArrayList<>();
            
            // Check all fields for @Id annotation
            List<Field> allFields = new ArrayList<>();
            Class<?> currentClass = clazz;
            while (currentClass != null && currentClass != Object.class) {
                allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
                currentClass = currentClass.getSuperclass();
            }
    
            for (Field field : allFields) {
                if (field.isAnnotationPresent(Id.class)) {
                    pkFields.add(field.getName());
                    log.debug("Found @Id field in {}: {}", 
                            clazz.getSimpleName(), field.getName());
                }
            }
    
            // If no @Id fields found, assume "id" is the primary key
            if (pkFields.isEmpty()) {
                pkFields.add("id");
                log.debug("No @Id fields found in {}, using default 'id'", 
                        clazz.getSimpleName());
            }
    
            return pkFields;
        });
    }

    // Method determineEntityClassName removed, functionality merged into findEntityClassByObjectType
    
    /**
     * Check if a class contains a field with the given name, with caching.
     * This method consolidates functionality from EntityConverter.
     *
     * @param clazz The class to check
     * @param fieldName The name of the field to find
     * @return true if the field exists, false otherwise
     */
    public boolean containsField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return false;
        }
        
        // Create cache key
        String cacheKey = clazz.getName() + ":" + fieldName;
        
        // Check cache first to avoid recursive calls
        Boolean cached = fieldExistenceCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Check all fields in this class
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                fieldExistenceCache.put(cacheKey, true);
                return true;
            }
        }

        // Check superclass recursively
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            boolean exists = containsField(superClass, fieldName);
            fieldExistenceCache.put(cacheKey, exists);
            return exists;
        }

        fieldExistenceCache.put(cacheKey, false);
        return false;
    }

    /**
     * Convert database tuples to table rows
     * (from EntityConverter)
     */
    public List<TableRow> convertTuplesToRows(List<Tuple> tuples, List<ColumnInfo> viewColumns) {
        // Use a map to deduplicate by ID
        Map<Object, TableRow> uniqueRows = new LinkedHashMap<>();

        // Create set of requested field names for validation
        Set<String> requestedFieldNames = viewColumns.stream()
                .map(ColumnInfo::getFieldName)
                .collect(Collectors.toSet());

        // Always include ID in requested fields (for internal processing)
        boolean idRequested = requestedFieldNames.contains("id");

        for (Tuple tuple : tuples) {
            Object id = tuple.get("id");

            // Skip if ID is null or already processed
            if (id == null || uniqueRows.containsKey(id)) {
                continue;
            }

            TableRow row = new TableRow();
            Map<String, Object> data = new LinkedHashMap<>();
            
            // Process each tuple element
            for (TupleElement<?> element : tuple.getElements()) {
                String alias = element.getAlias();
                Object value = tuple.get(alias);
                
                // Only include fields that were requested in viewColumns
                if (idRequested || !alias.equals("id")) {
                    data.put(alias, value);
                }
            }
            
            // Set row data (no separate key field in TableRow)
            row.setData(data);
            
            // Make sure ID is included in the data
            if (id != null && !data.containsKey("id")) {
                data.put("id", id.toString());
            }
            
            uniqueRows.put(id, row);
        }
        
        return new ArrayList<>(uniqueRows.values());
    }
    
    /**
     * Clears all caches - useful for testing
     * (from EntityHandler)
     */
    public void clearCaches() {
        entityClassCache.clear();
        tableNameCache.clear();
    }

    /**
     * Converts entity objects to table rows
     * (from EntityConverter)
     */
    public <T> TableRow convertEntityToTableRow(T entity, List<ColumnInfo> viewColumns) {
        if (entity == null) {
            return null;
        }

        TableRow row = new TableRow();
        Map<String, Object> data = new LinkedHashMap<>();

        // Get the entity class and its ID
        Class<?> entityClass = entity.getClass();
        Object id = extractIdValue(entity);

        // Get related tables if available
        boolean hasRelated = relatedTablesFactory.hasRelatedTables(entity);
        List<String> relatedTables = hasRelated ? relatedTablesFactory.getRelatedTables(entity) : null;

        // Fill in data from available columns
        for (ColumnInfo column : viewColumns) {
            String fieldName = column.getFieldName();
            try {
                // Extract value using reflection
                Object value = extractFieldValue(entity, fieldName);
                data.put(fieldName, value);
            } catch (Exception e) {
                log.warn("Could not extract field {} from entity {}: {}", 
                    fieldName, entityClass.getSimpleName(), e.getMessage());
                data.put(fieldName, null);
            }
        }

        // Set row data (no separate key field in TableRow)
        row.setData(data);
        
        // Make sure ID is included in the data
        if (id != null && !data.containsKey("id")) {
            data.put("id", id.toString());
        }
        
        // Add related tables information to the data if available
        if (hasRelated && relatedTables != null && !relatedTables.isEmpty()) {
            data.put("relatedTables", relatedTables);
        }

        return row;
    }

    /**
     * Extract ID value from an entity using reflection
     */
    private Object extractIdValue(Object entity) {
        try {
            // Try getter method first
            try {
                Method getIdMethod = entity.getClass().getMethod("getId");
                return getIdMethod.invoke(entity);
            } catch (NoSuchMethodException e) {
                // Try direct field access
                try {
                    Field idField = findFieldInClassHierarchy(entity.getClass(), "id");
                    if (idField != null) {
                        idField.setAccessible(true);
                        return idField.get(entity);
                    }
                } catch (Exception ex) {
                    // Fall back to searching for @Id annotation
                    Field idField = findIdAnnotatedField(entity.getClass());
                    if (idField != null) {
                        idField.setAccessible(true);
                        return idField.get(entity);
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Could not extract ID from entity {}: {}", 
                entity.getClass().getSimpleName(), ex.getMessage());
        }
        return null;
    }

    /**
     * Extract field value using reflection, handling nested paths
     */
    private Object extractFieldValue(Object entity, String fieldName) {
        if (entity == null || fieldName == null) {
            return null;
        }

        try {
            // Handle nested fields with dot notation
            if (fieldName.contains(".")) {
                String[] parts = fieldName.split("\\.", 2);
                String currentField = parts[0];
                String remainingPath = parts[1];

                Object nestedObject = extractFieldValue(entity, currentField);
                if (nestedObject == null) {
                    return null;
                }

                return extractFieldValue(nestedObject, remainingPath);
            }

            // Try getter method first
            String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                Method getterMethod = entity.getClass().getMethod(getterName);
                return getterMethod.invoke(entity);
            } catch (NoSuchMethodException e) {
                // Boolean getter might use "is" prefix
                if (fieldName.startsWith("is")) {
                    try {
                        Method isMethod = entity.getClass().getMethod(fieldName);
                        return isMethod.invoke(entity);
                    } catch (NoSuchMethodException ex) {
                        // Continue to field access
                    }
                } else {
                    try {
                        Method isMethod = entity.getClass().getMethod("is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
                        return isMethod.invoke(entity);
                    } catch (NoSuchMethodException ex) {
                        // Continue to field access
                    }
                }

                // Try direct field access
                Field field = findFieldInClassHierarchy(entity.getClass(), fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    return field.get(entity);
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract field {} from entity {}: {}", 
                fieldName, entity.getClass().getSimpleName(), e.getMessage());
        }

        return null;
    }

    /**
     * Find a field in class hierarchy (including superclasses)
     */
    private Field findFieldInClassHierarchy(Class<?> clazz, String fieldName) {
        if (clazz == null || clazz == Object.class) {
            return null;
        }

        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return findFieldInClassHierarchy(clazz.getSuperclass(), fieldName);
        }
    }

    /**
     * Find a field annotated with @Id in class hierarchy
     */
    private Field findIdAnnotatedField(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return null;
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }

        return findIdAnnotatedField(clazz.getSuperclass());
    }
    
    /**
     * Get the type of the ID field for an entity class with caching
     * This method consolidates functionality from EntityConverter.
     * 
     * @param entityClass The entity class
     * @return The class type of the ID field (typically Long or Integer)
     */
    public <T> Class<?> getIdType(Class<T> entityClass) {
        if (entityClass == null) {
            return Long.class; // Default fallback
        }
        
        try {
            // Try to find the id field by name
            Field idField = findFieldInClassHierarchy(entityClass, "id");
            if (idField != null) {
                return idField.getType();
            }
            
            // If not found by name, check for field with @Id annotation
            Field annotatedIdField = findIdAnnotatedField(entityClass);
            if (annotatedIdField != null) {
                return annotatedIdField.getType();
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
     * Get all fields from a class including its superclasses
     * 
     * @param clazz The class to get fields from
     * @return List of all fields including inherited ones
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
     * Convert an entity object to a DataObject for search criteria
     * This method consolidates functionality from EntityConverter.
     */
    public <T> vn.com.fecredit.app.service.dto.DataObject convertToDataObject(T entity) {
        if (entity == null) {
            return null;
        }
        
        // Create new DataObject
        vn.com.fecredit.app.service.dto.DataObject dataObject = new vn.com.fecredit.app.service.dto.DataObject();
        
        try {
            // Try to determine the ObjectType from entity class name
            String entityClassName = entity.getClass().getSimpleName();
            try {
                ObjectType objectType = ObjectType.valueOf(entityClassName);
                dataObject.setObjectType(objectType);
            } catch (IllegalArgumentException e) {
                log.warn("Could not convert entity class name to ObjectType: {}", entityClassName);
                // The object type will remain null in this case
            }
            
            // Convert entity to TableRow 
            TableRow tableRow = convertEntityToTableRow(entity, new ArrayList<>());
            dataObject.setData(tableRow);
            
        } catch (Exception e) {
            log.error("Error converting entity to DataObject", e);
        }
        
        return dataObject;
    }
}
