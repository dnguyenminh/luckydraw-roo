package vn.com.fecredit.app.service.impl.table;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ObjectType;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinHandler {

    private final EntityManager entityManager;
    private final EntityHandler entityHandler;

    // Cache for relationship paths between entity types
    private final ConcurrentHashMap<String, String> relationshipPathCache = new ConcurrentHashMap<>();

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
                    createNestedJoins(root, relationshipPath, joinMap, JoinType.LEFT);
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
     * Creates a series of joins for a nested path like "locations.region.provinces"
     */
    public <X, Y> Join<?, ?> createNestedJoins(
            From<X, Y> from, 
            String path, 
            Map<String, Join<?, ?>> joinMap, 
            JoinType joinType) {
        
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
            
            // Try variations of attribute names to handle common naming discrepancies
            String attributeName = findAttributeName(currentFrom.getJavaType(), part);
            if (attributeName != null) {
                Join<?, ?> join = currentFrom.join(attributeName, joinType);
                joinMap.put(joinKey, join);
                currentFrom = join;
            } else {
                throw new IllegalArgumentException("Could not find attribute '" + part + 
                    "' in " + currentFrom.getJavaType().getSimpleName());
            }
        }
        
        return (Join<?, ?>) currentFrom;
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
            
            // Additional variations could be added here
            
            return null;
        } catch (Exception e) {
            log.debug("Error finding attribute name: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets the relationship path between two entity types, with caching
     */
    public String getRelationshipPath(Class<?> sourceClass, ObjectType targetType) {
        String cacheKey = sourceClass.getName() + ":" + targetType.name();
        
        return relationshipPathCache.computeIfAbsent(cacheKey, key -> {
            try {
                // Get target entity class
                Class<?> targetClass = entityHandler.resolveEntityClass(null, targetType);
                if (targetClass == null) {
                    return null;
                }
                
                // Handle relationships based on common naming patterns
                String sourceName = sourceClass.getSimpleName().toLowerCase();
                String targetName = targetClass.getSimpleName().toLowerCase();
                
                // Direct relationship from source to target (singular)
                String directPath = targetName.toLowerCase();
                if (hasAttribute(sourceClass, directPath)) {
                    return directPath;
                }
                
                // Direct relationship from source to target (plural)
                String pluralPath = targetName.toLowerCase() + "s";
                if (hasAttribute(sourceClass, pluralPath)) {
                    return pluralPath;
                }
                
                // Map common relationships based on domain knowledge
                Map<String, Map<String, String>> knownRelationships = getKnownRelationships();
                if (knownRelationships.containsKey(sourceName)) {
                    Map<String, String> targetPaths = knownRelationships.get(sourceName);
                    if (targetPaths.containsKey(targetName)) {
                        return targetPaths.get(targetName);
                    }
                }
                
                // If all else fails, check for common bidirectional relationship patterns
                for (Attribute<?, ?> attr : entityManager.getMetamodel().entity(sourceClass).getAttributes()) {
                    if (attr.isCollection() && isEntityCollection(attr, targetClass)) {
                        return attr.getName();
                    } else if (attr.isAssociation() && attr.getJavaType().equals(targetClass)) {
                        return attr.getName();
                    }
                }
                
                return null;
            } catch (Exception e) {
                log.warn("Error finding relationship path: {}", e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Checks if an attribute is a collection of the target entity type
     */
    private boolean isEntityCollection(Attribute<?, ?> attr, Class<?> targetClass) {
        try {
            if (attr.isCollection()) {
                jakarta.persistence.metamodel.PluralAttribute<?, ?, ?> pluralAttr = 
                    (jakarta.persistence.metamodel.PluralAttribute<?, ?, ?>) attr;
                return pluralAttr.getElementType().getJavaType().equals(targetClass);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Checks if the entity class has an attribute with the given name
     */
    private boolean hasAttribute(Class<?> entityClass, String attributeName) {
        try {
            entityManager.getMetamodel().entity(entityClass).getAttribute(attributeName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
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
     * Clears the relationship path cache - useful for testing
     */
    public void clearCache() {
        relationshipPathCache.clear();
    }
}
