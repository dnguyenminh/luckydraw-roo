package vn.com.fecredit.app.service.factory.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.factory.RelatedTablesFactory;

/**
 * Implementation of the related tables factory.
 * Dynamically discovers relationships between entities using reflection.
 */
@Slf4j
@Component
public class RelatedTablesFactoryImpl implements RelatedTablesFactory {

    // Cache for storing discovered entity relationships
    private final Map<Class<?>, List<String>> entityRelatedTablesCache = new ConcurrentHashMap<>();

    // Cache for storing entity class to table name mapping
    private final Map<Class<?>, String> entityTableNameCache = new ConcurrentHashMap<>();

    // Package containing all entity classes
    private static final String ENTITY_PACKAGE = "vn.com.fecredit.app.entity";

    @PostConstruct
    public void initialize() {
        log.info("Initializing RelatedTablesFactoryImpl - Dynamic relationship discovery");
    }

    @Override
    public boolean hasRelatedTables(Object entity) {
        if (entity == null) {
            return false;
        }

        Class<?> entityClass = getEntityClass(entity);
        List<String> relatedTables = getRelatedTablesForClass(entityClass);
        return relatedTables != null && !relatedTables.isEmpty();
    }

    @Override
    public List<String> getRelatedTables(Object entity) {
        if (entity == null) {
            return List.of();
        }

        Class<?> entityClass = getEntityClass(entity);
        return getRelatedTablesForClass(entityClass);
    }

    @Override
    public List<Class<?>> getRelatedEntityClasses(Object entity) {
        if (entity == null) {
            return List.of();
        }

        Class<?> entityClass = getEntityClass(entity);
        return discoverRelatedEntityClasses(entityClass);
    }

    /**
     * Get related tables for a specific entity class
     * 
     * @param entityClass the entity class
     * @return list of related table names
     */
    private List<String> getRelatedTablesForClass(Class<?> entityClass) {
        // Check cache first
        if (entityRelatedTablesCache.containsKey(entityClass)) {
            return entityRelatedTablesCache.get(entityClass);
        }

        // Discover related tables through reflection
        List<String> relatedTables = discoverRelatedTables(entityClass);

        // Cache the result
        entityRelatedTablesCache.put(entityClass, relatedTables);

        log.debug("Discovered related tables for {}: {}", entityClass.getSimpleName(), relatedTables);
        return relatedTables;
    }

    /**
     * Discover related tables through reflection
     * 
     * @param entityClass the entity class to analyze
     * @return list of related table names
     */
    private List<String> discoverRelatedTables(Class<?> entityClass) {
        List<String> relatedTables = new ArrayList<>();

        // Get all fields from class and superclasses
        for (Field field : getAllFields(entityClass)) {
            // Skip fields that are not relationships
            if (!isRelationshipField(field)) {
                continue;
            }

            // Get the table name for this relationship
            String tableName = getTableNameForRelationship(field, entityClass);
            if (tableName != null && !tableName.isEmpty()) {
                relatedTables.add(tableName);
                log.trace("Added related table '{}' for entity {}", tableName, entityClass.getSimpleName());
            }
        }

        return relatedTables;
    }

    /**
     * Discover related entity classes through reflection
     * 
     * @param entityClass the entity class to analyze
     * @return list of related entity classes
     */
    private List<Class<?>> discoverRelatedEntityClasses(Class<?> entityClass) {
        List<Class<?>> relatedClasses = new ArrayList<>();

        // Get all fields from class and superclasses
        for (Field field : getAllFields(entityClass)) {
            // Skip fields that are not relationships
            if (!isRelationshipField(field)) {
                continue;
            }

            Class<?> relatedClass = null;

            // For ManyToOne and OneToOne, field type is the related entity class directly
            if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                relatedClass = field.getType();
            } 
            // For OneToMany and ManyToMany, extract from collection generic parameters
            else if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
                // First check annotation's targetEntity attribute
                if (field.isAnnotationPresent(OneToMany.class)) {
                    OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                    if (oneToMany != null && oneToMany.targetEntity() != void.class) {
                        relatedClass = oneToMany.targetEntity();
                    }
                } else if (field.isAnnotationPresent(ManyToMany.class)) {
                    ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
                    if (manyToMany != null && manyToMany.targetEntity() != void.class) {
                        relatedClass = manyToMany.targetEntity();
                    }
                }
                
                // If targetEntity not specified, try to extract from generic type parameters
                if (relatedClass == null) {
                    relatedClass = extractGenericType(field);
                }
                
                // Fall back to name-based inference only if necessary
                if (relatedClass == null) {
                    String fieldName = field.getName();
                    String singularName = getSingularName(fieldName);
                    relatedClass = tryToLoadClass(ENTITY_PACKAGE + "." + capitalize(singularName));
                    
                    if (relatedClass == null) {
                        log.warn("Could not determine target entity type for relationship field: {}", field.getName());
                    }
                }
            }

            if (relatedClass != null && !relatedClass.equals(entityClass)) {
                relatedClasses.add(relatedClass);
                log.trace("Added related entity class '{}' for entity {}", 
                    relatedClass.getSimpleName(), entityClass.getSimpleName());
            }
        }

        return relatedClasses;
    }

    /**
     * Extract generic type parameter from a collection field
     * 
     * @param field the field to analyze
     * @return the generic type parameter class or null if not found
     */
    private Class<?> extractGenericType(Field field) {
        try {
            if (field.getGenericType() instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType paramType = 
                    (java.lang.reflect.ParameterizedType) field.getGenericType();
                
                java.lang.reflect.Type[] typeArguments = paramType.getActualTypeArguments();
                if (typeArguments.length > 0) {
                    java.lang.reflect.Type typeArg = typeArguments[0];
                    
                    // Handle different types of Type objects
                    if (typeArg instanceof Class) {
                        return (Class<?>) typeArg;
                    } else if (typeArg instanceof java.lang.reflect.WildcardType) {
                        java.lang.reflect.WildcardType wildcardType = (java.lang.reflect.WildcardType) typeArg;
                        java.lang.reflect.Type[] upperBounds = wildcardType.getUpperBounds();
                        if (upperBounds.length > 0 && upperBounds[0] instanceof Class) {
                            return (Class<?>) upperBounds[0];
                        }
                    } else if (typeArg instanceof java.lang.reflect.ParameterizedType) {
                        java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) typeArg;
                        return (Class<?>) parameterizedType.getRawType();
                    } else if (typeArg instanceof java.lang.reflect.TypeVariable) {
                        // TypeVariable is more complex to resolve and might require class hierarchy traversal
                        log.debug("TypeVariable generic parameter found for field {}, cannot extract concrete type", field.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error extracting generic type for field {}: {}", field.getName(), e.getMessage());
            log.debug("Exception details:", e);
        }
        
        return null;
    }

    /**
     * Get all fields from class and its superclasses
     * 
     * @param clazz the class to analyze
     * @return list of all fields
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        // Traverse class hierarchy to get all fields
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * Check if a field represents a relationship
     * 
     * @param field the field to check
     * @return true if it's a relationship field
     */
    private boolean isRelationshipField(Field field) {
        return field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToOne.class) ||
                field.isAnnotationPresent(ManyToMany.class) ||
                field.isAnnotationPresent(OneToOne.class);
    }

    /**
     * Get table name for a relationship field
     * 
     * @param field      the relationship field
     * @param ownerClass the entity class that owns this field
     * @return the table name for this relationship
     */
    private String getTableNameForRelationship(Field field, Class<?> ownerClass) {
        try {
            // For ManyToMany relationships
            if (field.isAnnotationPresent(ManyToMany.class)) {
                // Check if the relationship has a JoinTable annotation
                if (field.isAnnotationPresent(JoinTable.class)) {
                    JoinTable joinTable = field.getAnnotation(JoinTable.class);
                    return joinTable.name();
                }

                // If no JoinTable specified, use naming convention: owner_field
                return getTableName(ownerClass).toLowerCase() + "_" + field.getName().toLowerCase();
            }

            // For ManyToOne relationships
            else if (field.isAnnotationPresent(ManyToOne.class)) {
                // The table name is likely in the entity class being referenced
                Class<?> targetEntity = field.getType();
                return getTableName(targetEntity);
            }

            // For OneToMany relationships
            else if (field.isAnnotationPresent(OneToMany.class)) {
                OneToMany oneToMany = field.getAnnotation(OneToMany.class);

                // First try to get the target entity from the annotation
                Class<?> targetEntity;
                try {
                    targetEntity = oneToMany.targetEntity();
                    if (targetEntity == void.class) {
                        // If targetEntity not specified, try to infer from generic type
                        // This is simplified - in a real implementation we would need to
                        // analyze the generic parameter type, which is more complex
                        String fieldName = field.getName();
                        String singularName = getSingularName(fieldName);
                        // This is a simplified approach - a real implementation would need
                        // to handle package discovery and class loading
                        targetEntity = tryToLoadClass(ENTITY_PACKAGE + "." + capitalize(singularName));
                    }
                } catch (Exception e) {
                    log.warn("Failed to determine target entity for OneToMany relationship: {}", field.getName());
                    targetEntity = null;
                }

                // If we found the target entity, get its table name
                if (targetEntity != null) {
                    return getTableName(targetEntity);
                }

                // Fall back to a naming convention
                return getSingularName(field.getName()).toLowerCase() + "s";
            }

            // For OneToOne relationships
            else if (field.isAnnotationPresent(OneToOne.class)) {
                // Check for JoinColumn to see if this side owns the relationship
                if (field.isAnnotationPresent(JoinColumn.class)) {
                    // This side likely owns the relationship
                    return getTableName(field.getType());
                } else {
                    // Otherwise, the other side likely owns the relationship
                    // Get the table name of the target entity
                    return getTableName(field.getType());
                }
            }
        } catch (Exception e) {
            log.warn("Error determining table name for relationship field {}: {}",
                    field.getName(), e.getMessage());
            log.debug("Exception details:", e);
        }

        return null;
    }

    /**
     * Get table name for an entity class
     * 
     * @param entityClass the entity class
     * @return the table name
     */
    private String getTableName(Class<?> entityClass) {
        // Check cache first
        if (entityTableNameCache.containsKey(entityClass)) {
            return entityTableNameCache.get(entityClass);
        }

        // Check for @Table annotation
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            String tableName = table.name();
            if (tableName != null && !tableName.isEmpty()) {
                entityTableNameCache.put(entityClass, tableName);
                return tableName;
            }
        }

        // Fall back to entity name pluralized (simple convention)
        String tableName = entityClass.getSimpleName().toLowerCase() + "s";
        entityTableNameCache.put(entityClass, tableName);
        return tableName;
    }

    /**
     * Try to convert a plural field name to singular form
     * This is a simplified implementation - a real version would use a proper
     * singularization library
     * 
     * @param fieldName the potentially plural field name
     * @return the singular form
     */
    private String getSingularName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }

        // Very simplified singularization - just handle common cases
        if (fieldName.endsWith("ies")) {
            return fieldName.substring(0, fieldName.length() - 3) + "y";
        } else if (fieldName.endsWith("s") && !fieldName.endsWith("ss")) {
            return fieldName.substring(0, fieldName.length() - 1);
        }

        return fieldName;
    }

    /**
     * Capitalize the first letter of a string
     * 
     * @param str the string
     * @return capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Try to load a class by name
     * 
     * @param className the class name
     * @return the class or null if not found
     */
    private Class<?> tryToLoadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.debug("Could not find class: {}", className);
            return null;
        }
    }

    /**
     * Get the actual entity class, handling proxies if needed
     * 
     * @param entity the entity object
     * @return the entity class
     */
    private Class<?> getEntityClass(Object entity) {
        Class<?> entityClass = entity.getClass();
        // Handle proxy objects if needed
        if (entityClass.getName().contains("$")) {
            entityClass = entityClass.getSuperclass();
        }
        return entityClass;
    }
}
