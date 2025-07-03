package vn.com.fecredit.app.service.impl.table;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

/**
 * Unified manager for all Join-related operations.
 * 
 * This class efficiently consolidates functionality that was previously spread across:
 * - JoinHandler: Basic join management
 * - JoinCreator: Creating joins based on field paths
 * - JoinCopier: Copying join structures between queries
 * - JoinStructure: Representing the structure of joins
 * - JoinInfo: Storing join metadata
 *
 * The consolidation eliminates duplicate code, improves caching,
 * and provides a single interface for all join-related operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JoinManager {

    // JPA EntityManager for database operations
    private final EntityManager entityManager;
    
    // Custom EntityManager that provides entity class resolution
    private final vn.com.fecredit.app.service.impl.table.EntityManager customEntityManager;
    
    // Repository factory for resolving entity classes
    private final RepositoryFactory repositoryFactory;
    
    // Cache for relationship paths to improve performance
    private final ConcurrentHashMap<String, String> relationshipPathCache = new ConcurrentHashMap<>();
    
    /**
     * Information about a relationship between entities
     */
    public static class RelationshipInfo {
        private final Class<?> sourceEntityClass;
        private final String propertyName;
        private final Class<?> relatedEntityClass;
        
        public RelationshipInfo(Class<?> sourceEntityClass, String propertyName, Class<?> relatedEntityClass) {
            this.sourceEntityClass = sourceEntityClass;
            this.propertyName = propertyName;
            this.relatedEntityClass = relatedEntityClass;
        }
        
        public Class<?> getSourceEntityClass() {
            return sourceEntityClass;
        }
        
        public String getPropertyName() {
            return propertyName;
        }
        
        public Class<?> getRelatedEntityClass() {
            return relatedEntityClass;
        }
    }
    
    /**
     * Represents a join relationship
     */
    public static class JoinInfo {
        private final String path;
        private final JoinType joinType;
        private final Class<?> targetClass;
        private final String alias;
        private final Class<?> sourceEntityClass;
        private final String propertyName;
        
        // Constructor for basic join info
        public JoinInfo(String path, JoinType joinType, Class<?> targetClass) {
            this.path = path;
            this.joinType = joinType;
            this.targetClass = targetClass;
            this.alias = null;
            this.sourceEntityClass = null;
            this.propertyName = null;
        }
        
        // Constructor for full join information
        public JoinInfo(String alias, String propertyName, Class<?> sourceEntityClass, 
                    Class<?> targetClass, JoinType joinType) {
            this.alias = alias;
            this.propertyName = propertyName;
            this.sourceEntityClass = sourceEntityClass;
            this.targetClass = targetClass;
            this.joinType = joinType;
            this.path = propertyName;
        }
        
        public String getPath() {
            return path;
        }
        
        public JoinType getJoinType() {
            return joinType;
        }
        
        public Class<?> getTargetClass() {
            return targetClass;
        }
        
        public String getAlias() {
            return alias;
        }
        
        public Class<?> getSourceEntityClass() {
            return sourceEntityClass;
        }
        
        public String getPropertyName() {
            return propertyName;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JoinInfo joinInfo = (JoinInfo) o;
            return (alias != null) ? 
                   java.util.Objects.equals(alias, joinInfo.alias) : 
                   java.util.Objects.equals(path, joinInfo.path);
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(alias != null ? alias : path);
        }
    }

    /**
     * Represents the structure of joins for a query
     */
    public static class JoinStructure {
        private final Map<String, JoinInfo> joinInfoMap = new HashMap<>();
        private final Class<?> rootEntityClass;
        private final Map<ObjectType, Class<?>> targetEntities = new HashMap<>();
        private final Map<Class<?>, List<RelationshipInfo>> relationshipsBySource = new HashMap<>();
        private final List<JoinInfo> joins = new ArrayList<>();
        
        public JoinStructure(Class<?> rootEntityClass) {
            this.rootEntityClass = rootEntityClass;
        }
        
        public void addJoin(String path, JoinType joinType, Class<?> targetClass) {
            joinInfoMap.put(path, new JoinInfo(path, joinType, targetClass));
        }
        
        public JoinInfo getJoinInfo(String path) {
            return joinInfoMap.get(path);
        }
        
        public boolean hasJoin(String path) {
            return joinInfoMap.containsKey(path);
        }
        
        public Map<String, JoinInfo> getJoinMap() {
            return joinInfoMap;
        }
        
        public Class<?> getRootEntityClass() {
            return rootEntityClass;
        }
        
        public void addTargetEntity(ObjectType objectType, Class<?> entityClass) {
            targetEntities.put(objectType, entityClass);
        }
        
        public Map<ObjectType, Class<?>> getTargetEntities() {
            return targetEntities;
        }
        
        public void addRelationship(Class<?> sourceEntityClass, String propertyName, Class<?> relatedEntityClass) {
            relationshipsBySource
                .computeIfAbsent(sourceEntityClass, k -> new ArrayList<>())
                .add(new RelationshipInfo(sourceEntityClass, propertyName, relatedEntityClass));
        }
        
        public List<RelationshipInfo> getRelationshipsFrom(Class<?> sourceEntityClass) {
            return relationshipsBySource.get(sourceEntityClass);
        }
        
        public void addJoinInfo(JoinInfo joinInfo) {
            if (!joins.contains(joinInfo)) {
                joins.add(joinInfo);
            }
        }
        
        public List<JoinInfo> getAllJoins() {
            return java.util.Collections.unmodifiableList(joins);
        }
    }

    /**
     * Creates joins from a search map and view columns
     */
    public Map<String, Join<?, ?>> createJoinsFromSearchMapAndViewColumns(
            Map<ObjectType, DataObject> searchMap,
            List<ColumnInfo> viewColumns,
            Class<?> currentEntityClass) {
        
        Map<String, Join<?, ?>> joins = new HashMap<>();
        if ((searchMap == null || searchMap.isEmpty()) && (viewColumns == null || viewColumns.isEmpty()) || currentEntityClass == null) {
            return joins;
        }

        // Create a CriteriaQuery to initialize the Root
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery();
        Root<?> root = query.from(currentEntityClass);

        // Process search map to create necessary joins
        if (searchMap != null && !searchMap.isEmpty()) {
            for (Map.Entry<ObjectType, DataObject> entry : searchMap.entrySet()) {
                String relationPath = getRelationshipPath(currentEntityClass, entry.getKey());
                if (relationPath != null) {
                    createJoinPathRecursively(root, relationPath, joins);
                }
            }
        }

        // Process view columns to create necessary joins for fetching related data
        if (viewColumns != null && !viewColumns.isEmpty()) {
            for (ColumnInfo column : viewColumns) {
                String fieldName = column.getFieldName();
                if (fieldName.contains(".")) {
                    createJoinPathRecursively(root, fieldName, joins);
                }
            }
        }

        return joins;
    }
    
    /**
     * Creates joins from a search map and view columns using an existing Root
     */
    public Map<String, Join<?, ?>> createJoinsFromSearchMapAndViewColumns(
            Map<ObjectType, DataObject> searchMap,
            List<ColumnInfo> viewColumns,
            Root<?> root) {
        
        Map<String, Join<?, ?>> joins = new HashMap<>();
        if ((searchMap == null || searchMap.isEmpty()) && (viewColumns == null || viewColumns.isEmpty()) || root == null) {
            return joins;
        }
        
        Class<?> currentEntityClass = root.getJavaType();

        // Process search map to create necessary joins
        if (searchMap != null && !searchMap.isEmpty()) {
            for (Map.Entry<ObjectType, DataObject> entry : searchMap.entrySet()) {
                String relationPath = getRelationshipPath(currentEntityClass, entry.getKey());
                if (relationPath != null) {
                    createJoinPathRecursively(root, relationPath, joins);
                }
            }
        }

        // Process view columns to create necessary joins for fetching related data
        if (viewColumns != null && !viewColumns.isEmpty()) {
            for (ColumnInfo column : viewColumns) {
                String fieldName = column.getFieldName();
                if (fieldName.contains(".")) {
                    createJoinPathRecursively(root, fieldName, joins);
                }
            }
        }

        return joins;
    }

    /**
     * Creates joins based on search criteria
     */
    public Map<String, Join<?, ?>> createJoinsForSearchCriteria(
            Root<?> root, Map<ObjectType, ?> searchCriteria) {
        Map<String, Join<?, ?>> joinMap = new HashMap<>();

        if (searchCriteria == null || searchCriteria.isEmpty()) {
            return joinMap;
        }

        Class<?> rootEntityClass = root.getJavaType();

        // Process each search criteria entry
        for (Map.Entry<ObjectType, ?> entry : searchCriteria.entrySet()) {
            ObjectType targetType = entry.getKey();

            // Skip if it's the same as the root entity type
            if (targetType.name().equals(rootEntityClass.getSimpleName())) {
                continue;
            }

            String relationshipPath = getRelationshipPath(rootEntityClass, targetType);
            if (relationshipPath != null) {
                try {
                    createNestedJoins(root, relationshipPath, joinMap, JoinType.INNER);
                } catch (Exception e) {
                    log.warn("Invalid join path for search criteria: {}", targetType.name().toLowerCase(), e);
                }
            } else {
                log.warn("Could not determine relationship path from {} to {}",
                    rootEntityClass.getSimpleName(), targetType.name());
            }
        }

        return joinMap;
    }

    /**
     * Creates nested joins with improved error handling for missing attributes
     */
    public <X, Y> Join<?, ?> createNestedJoins(
            From<X, Y> from,
            String path,
            Map<String, Join<?, ?>> joinMap,
            JoinType joinType) {

        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] pathParts = path.split("\\.");
        From<?, ?> currentFrom = from;
        StringBuilder currentPath = new StringBuilder();

        for (int i = 0; i < pathParts.length; i++) {
            String part = pathParts[i];

            // Build the current path for join cache key
            if (currentPath.length() > 0) {
                currentPath.append(".");
            }
            currentPath.append(part);
            String joinKey = currentPath.toString();

            // Check if we already have this join
            Join<?, ?> existingJoin = joinMap.get(joinKey);
            if (existingJoin != null) {
                currentFrom = existingJoin;
                continue;
            }

            // Try to find attribute in multiple ways
            String attributeName = findAttributeName(currentFrom.getJavaType(), part);

            if (attributeName != null) {
                try {
                    Join<?, ?> join = currentFrom.join(attributeName, joinType);
                    joinMap.put(joinKey, join);
                    currentFrom = join;
                } catch (IllegalArgumentException e) {
                    // Log but don't throw - we'll try alternative paths
                    log.warn("Could not find attribute '{}' in {}",
                        attributeName, currentFrom.getJavaType().getSimpleName());

                    // Try alternative paths for well-known relationships
                    Join<?, ?> alternativeJoin = createAlternativeJoin(
                            currentFrom, attributeName, joinKey, joinMap, joinType);

                    if (alternativeJoin != null) {
                        currentFrom = alternativeJoin;
                    } else {
                        // If we still can't create a join, return what we have so far
                        return (currentFrom instanceof Join) ? (Join<?, ?>) currentFrom : null;
                    }
                }
            } else {
                log.warn("Could not find attribute '{}' in {}",
                    part, currentFrom.getJavaType().getSimpleName());

                // Try best-effort alternative paths
                Join<?, ?> alternativeJoin = createAlternativeJoin(
                        currentFrom, part, joinKey, joinMap, joinType);

                if (alternativeJoin != null) {
                    currentFrom = alternativeJoin;
                } else {
                    // Return what we have so far
                    return (currentFrom instanceof Join) ? (Join<?, ?>) currentFrom : null;
                }
            }
        }

        return (currentFrom instanceof Join) ? (Join<?, ?>) currentFrom : null;
    }

    /**
     * Creates alternative join paths for known entity relationships
     */
    private <X, Y> Join<?, ?> createAlternativeJoin(
            From<X, Y> from,
            String attributeName,
            String joinKey,
            Map<String, Join<?, ?>> joinMap,
            JoinType joinType) {

        Class<?> fromClass = from.getJavaType();

        // Handle Event -> ParticipantEvent relationship (through EventLocation)
        if (fromClass.getSimpleName().equals("Event") &&
                attributeName.equals("participantEvents")) {
            try {
                // First join to event_locations
                Join<?, ?> locationsJoin;
                if (joinMap.containsKey("locations")) {
                    locationsJoin = joinMap.get("locations");
                } else {
                    locationsJoin = from.join("locations", joinType);
                    joinMap.put("locations", locationsJoin);
                }

                // Then join from event_locations to participant_events
                Join<?, ?> participantEventsJoin = locationsJoin.join("participantEvents", joinType);
                joinMap.put(joinKey, participantEventsJoin);
                return participantEventsJoin;
            } catch (Exception e) {
                log.warn("Failed to create alternative join for Event->ParticipantEvent: {}",
                    e.getMessage());
            }
        }

        // Handle Event -> Participant relationship (through EventLocation and ParticipantEvent)
        if (fromClass.getSimpleName().equals("Event") &&
                attributeName.equals("participant")) {
            try {
                // Get or create the participant events join
                Join<?, ?> participantEventsJoin;
                if (joinMap.containsKey("participantEvents")) {
                    participantEventsJoin = joinMap.get("participantEvents");
                } else {
                    participantEventsJoin = createAlternativeJoin(
                        from, "participantEvents", "participantEvents", joinMap, joinType);
                }

                if (participantEventsJoin != null) {
                    // Join from participant_events to participant
                    Join<?, ?> participantJoin = participantEventsJoin.join("participant", joinType);
                    joinMap.put(joinKey, participantJoin);
                    return participantJoin;
                }
            } catch (Exception e) {
                log.warn("Failed to create alternative join for Event->Participant: {}",
                    e.getMessage());
            }
        }

        return null;
    }

    /**
     * Finds the correct attribute name, handling plural/singular and case variations
     */
    private String findAttributeName(Class<?> entityClass, String attributeName) {
        try {
            ManagedType<?> type = entityManager.getMetamodel().managedType(entityClass);

            // Try exact match first
            try {
                Attribute<?, ?> attribute = type.getAttribute(attributeName);
                return attribute.getName();
            } catch (IllegalArgumentException e) {
                // Continue with other variations
            }

            // Try removing plural 's'
            if (attributeName.endsWith("s")) {
                String singular = attributeName.substring(0, attributeName.length() - 1);
                try {
                    Attribute<?, ?> attribute = type.getAttribute(singular);
                    return attribute.getName();
                } catch (IllegalArgumentException e) {
                    // Continue with other variations
                }
            }

            // Try adding plural 's'
            try {
                Attribute<?, ?> attribute = type.getAttribute(attributeName + "s");
                return attribute.getName();
            } catch (IllegalArgumentException e) {
                // Continue with other variations
            }

            return null;
        } catch (Exception e) {
            log.debug("Error finding attribute name: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets the relationship path between an entity class and an object type
     */
    public String getRelationshipPath(Class<?> entityClass, ObjectType targetObjectType) {
        String cacheKey = entityClass.getName() + "->" + targetObjectType.name();
        
        // Check cache first
        if (relationshipPathCache.containsKey(cacheKey)) {
            return relationshipPathCache.get(cacheKey);
        }
        
        try {
            // Try to find matching entity class for target object type
            Class<?> targetEntityClass = customEntityManager.resolveEntityClass(null, targetObjectType);
            if (targetEntityClass == null) {
                log.debug("No entity class found for object type: {}", targetObjectType);
                relationshipPathCache.put(cacheKey, null); // Cache negative result
                return null;
            }
            
            // Find relationship path
            String relationPath = findRelationshipPath(entityClass, targetEntityClass, new HashSet<>(), "");
            
            // If direct path not found, check for known relationships
            if (relationPath == null) {
                relationPath = findKnownRelationship(entityClass, targetEntityClass);
            }
            
            // If still not found, try bidirectional relationships
            if (relationPath == null) {
                relationPath = findBidirectionalPath(entityClass, targetEntityClass);
            }
            
            relationshipPathCache.put(cacheKey, relationPath);
            return relationPath;
        } catch (Exception e) {
            log.warn("Error finding relationship path between {} and {}: {}", 
                    entityClass.getSimpleName(), targetObjectType, e.getMessage());
            relationshipPathCache.put(cacheKey, null); // Cache negative result
            return null;
        }
    }
    
    /**
     * Look for known relationships between entity types
     */
    private String findKnownRelationship(Class<?> sourceClass, Class<?> targetClass) {
        String sourceName = sourceClass.getSimpleName().toLowerCase();
        String targetName = targetClass.getSimpleName().toLowerCase();
        
        Map<String, Map<String, String>> knownRelationships = getKnownRelationships();
        if (knownRelationships.containsKey(sourceName)) {
            Map<String, String> targetPaths = knownRelationships.get(sourceName);
            if (targetPaths.containsKey(targetName)) {
                return targetPaths.get(targetName);
            }
        }
        
        return null;
    }
    
    /**
     * Check bidirectional relationships by examining the target class for fields
     * with the source type
     */
    private String findBidirectionalPath(Class<?> sourceClass, Class<?> targetClass) {
        try {
            String sourceName = sourceClass.getSimpleName().toLowerCase();

            // Look for fields in the target entity that reference the source type
            for (jakarta.persistence.metamodel.Attribute<?, ?> attr :
                    entityManager.getMetamodel().entity(targetClass).getAttributes()) {
                if (attr.isAssociation() && isMatchingEntityType(attr, sourceClass)) {
                    // Found a back-reference - check for mappedBy fields on target side
                    return attr.getName() + "." + sourceName;
                }
            }
        } catch (Exception e) {
            // Ignore errors when checking bidirectional relationships
        }
        return null;
    }
    
    /**
     * Returns known relationships between entity types
     * This could be moved to a configuration file or database table
     */
    private Map<String, Map<String, String>> getKnownRelationships() {
        Map<String, Map<String, String>> relationships = new HashMap<>();

        // Event relationships
        Map<String, String> eventRelationships = new HashMap<>();
        eventRelationships.put("eventlocation", "locations");
        eventRelationships.put("participantevent", "participantEvents");
        eventRelationships.put("participant", "participantEvents.participant");
        eventRelationships.put("region", "locations.region");
        eventRelationships.put("province", "locations.region.provinces");
        relationships.put("event", eventRelationships);

        // Add more entity relationships here

        return relationships;
    }
    
    /**
     * More robust attribute type checking
     */
    private boolean isMatchingEntityType(jakarta.persistence.metamodel.Attribute<?, ?> attr, Class<?> targetClass) {
        try {
            Class<?> attrType = attr.getJavaType();
            return targetClass.isAssignableFrom(attrType) || attrType.isAssignableFrom(targetClass);
        } catch (Exception e) {
            return false;
        }
    }



    /**
     * Recursively finds the path between two entity classes
     */
    private String findRelationshipPath(Class<?> sourceClass, Class<?> targetClass, 
            Set<Class<?>> visited, String currentPath) {
        
        if (sourceClass.equals(targetClass)) {
            return currentPath;
        }
        
        if (visited.contains(sourceClass)) {
            return null; // Prevent cycles
        }
        
        visited.add(sourceClass);
        
        for (Field field : sourceClass.getDeclaredFields()) {
            // Check if field has relationship annotation
            if (field.isAnnotationPresent(OneToOne.class) || 
                field.isAnnotationPresent(OneToMany.class) || 
                field.isAnnotationPresent(ManyToOne.class) || 
                field.isAnnotationPresent(ManyToMany.class)) {
                
                Class<?> fieldType = field.getType();
                String nextPath = currentPath.isEmpty() ? field.getName() : currentPath + "." + field.getName();
                
                // Handle collection types
                if (List.class.isAssignableFrom(fieldType) || Set.class.isAssignableFrom(fieldType)) {
                    if (field.getGenericType() instanceof ParameterizedType) {
                        ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                        Class<?> genericType = (Class<?>) paramType.getActualTypeArguments()[0];
                        
                        String result = findRelationshipPath(genericType, targetClass, visited, nextPath);
                        if (result != null) {
                            return result;
                        }
                    }
                } else {
                    String result = findRelationshipPath(fieldType, targetClass, visited, nextPath);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        
        visited.remove(sourceClass);
        return null;
    }

    /**
     * Creates join paths recursively from a dot-notation path
     */
    public <T> Join<?, ?> createJoinPathRecursively(Root<T> root, String path, Map<String, Join<?, ?>> joinMap) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        // If the path doesn't contain a dot, it's a direct attribute - no join needed
        if (!path.contains(".")) {
            return null;
        }
        
        String[] pathSegments = path.split("\\.");
        From<?, ?> from = root;
        Join<?, ?> join = null;
        StringBuilder currentPath = new StringBuilder();
        
        // For paths like "locations.region.name", we only want to join "locations.region"
        // and leave "name" as a property access
        int lastSegmentIndex = pathSegments.length;
        
        // Check if the last segment is a basic attribute (not an entity)
        try {
            Class<?> rootClass = root.getJavaType();
            Class<?> currentClass = rootClass;
            
            for (int i = 0; i < pathSegments.length - 1; i++) {
                Field field = findField(currentClass, pathSegments[i]);
                if (field == null) {
                    log.debug("Could not find field '{}' in class {}", 
                        pathSegments[i], currentClass.getSimpleName());
                    break;
                }
                
                Class<?> fieldType = field.getType();
                
                // If it's a collection, get the generic type
                if (java.util.Collection.class.isAssignableFrom(fieldType)) {
                    java.lang.reflect.Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType paramType = (ParameterizedType) genericType;
                        java.lang.reflect.Type[] typeArgs = paramType.getActualTypeArguments();
                        if (typeArgs != null && typeArgs.length > 0) {
                            fieldType = (Class<?>) typeArgs[0];
                        }
                    }
                }
                
                currentClass = fieldType;
            }
            
            // Try to find the last segment as a field in the current class
            Field lastField = findField(currentClass, pathSegments[pathSegments.length - 1]);
            
            // If the last field exists and is not an entity or collection, don't join to it
            if (lastField != null) {
                Class<?> lastFieldType = lastField.getType();
                boolean isEntity = lastFieldType.isAnnotationPresent(jakarta.persistence.Entity.class);
                boolean isCollection = java.util.Collection.class.isAssignableFrom(lastFieldType);
                
                if (!isEntity && !isCollection) {
                    // Last segment is a basic attribute, don't include it in join
                    lastSegmentIndex = pathSegments.length - 1;
                    log.debug("Last segment '{}' is a basic attribute of type {}, not creating join for it", 
                        pathSegments[pathSegments.length - 1], lastFieldType.getSimpleName());
                } else {
                    log.debug("Last segment '{}' is {} {}, will be included in join path", 
                        pathSegments[pathSegments.length - 1],
                        isEntity ? "an entity" : "",
                        isCollection ? "a collection" : "");
                }
            } else {
                log.debug("Could not find field '{}' in class {}, treating as basic attribute", 
                    pathSegments[pathSegments.length - 1], currentClass.getSimpleName());
                lastSegmentIndex = pathSegments.length - 1;
            }
        } catch (Exception e) {
            // In case of any reflection errors, fall back to default behavior
            log.debug("Error analyzing path structure: {}", e.getMessage());
        }
        
        // Create joins for all segments except possibly the last one
        for (int i = 0; i < lastSegmentIndex; i++) {
            String segment = pathSegments[i];
            
            if (currentPath.length() > 0) {
                currentPath.append(".");
            }
            currentPath.append(segment);
            
            String currentPathStr = currentPath.toString();
            
            // Check if this join already exists
            if (joinMap.containsKey(currentPathStr)) {
                join = joinMap.get(currentPathStr);
                from = join;
                log.debug("Reusing existing join for path: {}", currentPathStr);
            } else {
                // Create a new join
                try {
                    join = from.join(segment, JoinType.LEFT);
                    joinMap.put(currentPathStr, join);
                    from = join;
                    log.debug("Created new join for path: {}", currentPathStr);
                } catch (IllegalArgumentException e) {
                    log.debug("Failed to create join for segment {} in path {}: {}", 
                        segment, path, e.getMessage());
                    break;
                }
            }
        }
        
        return join;
    }

    /**
     * Recursively copies all joins from the original From object to the new one,
     * preserving the exact join structure and attributes
     */
    public void copyJoinsRecursively(From<?, ?> originalFrom, From<?, ?> newFrom,
            Map<String, Join<?, ?>> joinMap, Set<String> processedJoins) {
        
        // Process all joins from the original From (Root or Join)
        Set<?> joinSet = originalFrom.getJoins();
        if (joinSet == null || joinSet.isEmpty()) {
            return;
        }

        for (Object obj : joinSet) {
            if (!(obj instanceof Join)) {
                continue;
            }
            
            Join<?, ?> originalJoin = (Join<?, ?>) obj;
            String joinKey = createJoinKey(originalJoin);

            // Skip if already processed to avoid infinite recursion
            if (processedJoins.add(joinKey)) {
                // Create new join with same attribute and join type
                Join<?, ?> newJoin = newFrom.join(
                        originalJoin.getAttribute().getName(),
                        originalJoin.getJoinType());
                joinMap.put(joinKey, newJoin);

                // Process nested joins recursively
                copyJoinsRecursively(originalJoin, newJoin, joinMap, processedJoins);
            }
        }
    }

    /**
     * Creates a copy of all joins from one root to another
     * 
     * @param originalRoot The original root with joins
     * @param newRoot The new root to copy joins to
     * @return A map of join paths to Join objects
     */
    public Map<String, Join<?, ?>> copyJoins(Root<?> originalRoot, Root<?> newRoot) {
        if (originalRoot == null || newRoot == null) {
            return new HashMap<>();
        }
        
        Map<String, Join<?, ?>> joinMap = new HashMap<>();
        Set<String> processedJoins = new HashSet<>();
        
        copyJoinsRecursively(originalRoot, newRoot, joinMap, processedJoins);
        
        return joinMap;
    }

    /**
     * Creates a unique join key that includes the full path to handle nested joins
     */
    public String createJoinKey(Join<?, ?> join) {
        List<String> pathParts = new ArrayList<>();
        pathParts.add(join.getAttribute().getName());

        From<?, ?> parent = join.getParent();
        while (parent instanceof Join) {
            Join<?, ?> parentJoin = (Join<?, ?>) parent;
            pathParts.add(0, parentJoin.getAttribute().getName());
            parent = parentJoin.getParent();
        }

        return String.join(".", pathParts);
    }

    /**
     * Analyzes the entity's JPA metamodel to identify all potential join fields
     */
    public Map<String, Class<?>> getJoinableFields(Class<?> entityClass) {
        Map<String, Class<?>> joinableFields = new HashMap<>();
        
        try {
            ManagedType<?> managedType = entityManager.getMetamodel().managedType(entityClass);
            
            for (Attribute<?, ?> attribute : managedType.getAttributes()) {
                if (attribute.isAssociation()) {
                    joinableFields.put(attribute.getName(), attribute.getJavaType());
                }
            }
        } catch (Exception e) {
            log.warn("Error analyzing metamodel for {}: {}", entityClass.getSimpleName(), e.getMessage());
            
            // Fall back to reflection-based analysis
            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(OneToOne.class) || 
                    field.isAnnotationPresent(OneToMany.class) || 
                    field.isAnnotationPresent(ManyToOne.class) || 
                    field.isAnnotationPresent(ManyToMany.class)) {
                    
                    Class<?> fieldType = field.getType();
                    
                    // Handle collection types
                    if (List.class.isAssignableFrom(fieldType) || Set.class.isAssignableFrom(fieldType)) {
                        if (field.getGenericType() instanceof ParameterizedType) {
                            ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                            Class<?> genericType = (Class<?>) paramType.getActualTypeArguments()[0];
                            joinableFields.put(field.getName(), genericType);
                        }
                    } else {
                        joinableFields.put(field.getName(), fieldType);
                    }
                }
            }
        }
        
        return joinableFields;
    }

    /**
     * Clears all caches
     */
    public void clearCaches() {
        relationshipPathCache.clear();
    }

    /**
     * Resolves a path expression from the provided join map
     * 
     * @param <X> Type of the root entity
     * @param fieldPath The dot-notation path to resolve
     * @param root The root entity
     * @param joinMap The map of join paths to Join objects
     * @return The resolved Path, or null if not resolvable
     */
    public <X> Path<?> resolvePathFromJoins(String fieldPath, Root<X> root, Map<String, Join<?, ?>> joinMap) {
        if (fieldPath == null || fieldPath.isEmpty()) {
            return null;
        }
        
        // Direct field of root (no dots)
        if (!fieldPath.contains(".")) {
            try {
                return root.get(fieldPath);
            } catch (IllegalArgumentException e) {
                log.debug("Field {} is not directly accessible from root entity {}", 
                        fieldPath, root.getJavaType().getSimpleName());
                return null;
            }
        }
        
        // Get the last path segment which is the attribute name
        String[] pathSegments = fieldPath.split("\\.");
        String attributeName = pathSegments[pathSegments.length - 1];
        
        // Calculate the join path without the last segment
        String[] joinPathSegments = new String[pathSegments.length - 1];
        System.arraycopy(pathSegments, 0, joinPathSegments, 0, joinPathSegments.length);
        String joinPath = String.join(".", joinPathSegments);
        
        // Find the appropriate join
        Join<?, ?> join = joinMap.get(joinPath);
        if (join != null) {
            try {
                // Get the attribute from the join
                return join.get(attributeName);
            } catch (IllegalArgumentException e) {
                log.debug("Attribute {} not found in join {}", attributeName, joinPath);
            }
        }
        
        // If we can't find the exact join, try to find the longest matching prefix
        String longestMatch = findLongestMatchingPrefix(joinPath, joinMap.keySet());
        if (longestMatch != null) {
            join = joinMap.get(longestMatch);
            
            // Calculate the remaining path segments
            String remainingPath = joinPath.substring(longestMatch.length());
            if (remainingPath.startsWith(".")) {
                remainingPath = remainingPath.substring(1);
            }
            
            // Try to navigate from the join to the target attribute
            try {
                From<?, ?> from = join;
                for (String segment : remainingPath.split("\\.")) {
                    if (!segment.isEmpty()) {
                        from = from.join(segment, JoinType.LEFT);
                    }
                }
                return from.get(attributeName);
            } catch (IllegalArgumentException e) {
                log.debug("Failed to navigate path {} from join {}", remainingPath, longestMatch);
            }
        }
        
        // As a last resort, try to create the entire path
        try {
            From<?, ?> from = root;
            for (int i = 0; i < pathSegments.length - 1; i++) {
                from = from.join(pathSegments[i], JoinType.LEFT);
            }
            return from.get(attributeName);
        } catch (Exception e) {
            log.debug("Failed to create path for {}: {}", fieldPath, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Finds the longest matching prefix in a set of strings
     * 
     * @param path The path to match against
     * @param candidates The set of candidate strings
     * @return The longest matching prefix, or null if none found
     */
    private String findLongestMatchingPrefix(String path, Set<String> candidates) {
        String longestMatch = null;
        int maxLength = -1;
        
        for (String candidate : candidates) {
            if (path.startsWith(candidate) && candidate.length() > maxLength) {
                maxLength = candidate.length();
                longestMatch = candidate;
            }
        }
        
        return longestMatch;
    }
    
    /**
     * Creates joins for all view columns in a query from JoinCreator
     */
    public void createJoinsForViewColumns(
        List<ColumnInfo> viewColumns,
        Class<?> currentEntityClass,
        Root<?> root,
        Map<String, Join<?, ?>> joins,
        Map<String, Join<?, ?>> joinCache
    ) {
        if (viewColumns == null || viewColumns.isEmpty()) {
            return;
        }

        for (ColumnInfo column : viewColumns) {
            String fieldName = column.getFieldName();
            String[] fieldParts = fieldName.split("\\.");
            if (fieldParts.length > 1) {
                Path<?> currentPath = root;
                Class<?> currentClass = currentEntityClass;

                // Process all parts except the last one (which is the property)
                for (int i = 0; i < fieldParts.length - 1; i++) {
                    String joinField = fieldParts[i];
                    Field field = findField(currentClass, joinField);
                    if (field != null && isRelationshipField(field) && currentPath != null) {
                        String joinKey = currentPath.getModel().getBindableJavaType().getName() + "." + joinField;
                        Join<?, ?> join = joinCache.get(joinKey);

                        if (join == null) {
                            try {
                                if (currentPath instanceof Root<?>) {
                                    join = ((Root<?>) currentPath).join(joinField, JoinType.INNER);
                                } else if (currentPath instanceof Join<?, ?>) {
                                    join = ((Join<?, ?>) currentPath).join(joinField, JoinType.INNER);
                                }
                                joinCache.put(joinKey, join);
                                joins.put(joinField, join);
                            } catch (IllegalArgumentException e) {
                                log.warn("Could not create join for field: {} in path: {}", joinField, fieldName, e);
                                break;
                            }
                        }
                        currentPath = join;
                        currentClass = getRelatedEntityClass(field);
                    } else {
                        log.warn("Field {} is not a relationship in entity {}", joinField, currentClass.getSimpleName());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Find a field in a class by name
     */
    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Check if a field represents a relationship
     */
    private boolean isRelationshipField(Field field) {
        return field != null && (
            field.isAnnotationPresent(OneToOne.class) ||
                field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToOne.class) ||
                field.isAnnotationPresent(ManyToMany.class)
        );
    }

    /**
     * Get the class of the related entity for a relationship field
     */
    private Class<?> getRelatedEntityClass(Field field) {
        if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
            return field.getType();
        } else if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
        return null;
    }
    

}
