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

    // Improved caching with stronger key generation
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
            
            // Additional variations could be added here
            
            return null;
        } catch (Exception e) {
            log.debug("Error finding attribute name: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Enhanced relationship path discovery
     */
    public String getRelationshipPath(Class<?> sourceClass, ObjectType targetType) {
        if (sourceClass == null || targetType == null) {
            return null;
        }
        
        String cacheKey = sourceClass.getName() + ":" + targetType.name();
        
        return relationshipPathCache.computeIfAbsent(cacheKey, key -> {
            try {
                // Get target entity class
                Class<?> targetClass = entityHandler.resolveEntityClass(null, targetType);
                if (targetClass == null) {
                    return null;
                }
                
                // Try direct field first
                String sourceName = sourceClass.getSimpleName().toLowerCase();
                String targetName = targetClass.getSimpleName().toLowerCase();
                
                // Check common variations
                String[] possiblePaths = {
                    targetName,                // direct singular (event)
                    targetName + "s",          // direct plural (events)
                    targetName + "List",       // common list suffix (eventList)
                    targetName + "Collection", // collection suffix (eventCollection)
                    targetName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(), // snake case
                    targetName.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase()  // kebab case
                };
                
                for (String path : possiblePaths) {
                    if (hasAttribute(sourceClass, path)) {
                        return path;
                    }
                }
                
                // Check for known relationships
                Map<String, Map<String, String>> knownRelationships = getKnownRelationships();
                if (knownRelationships.containsKey(sourceName)) {
                    Map<String, String> targetPaths = knownRelationships.get(sourceName);
                    if (targetPaths.containsKey(targetName)) {
                        return targetPaths.get(targetName);
                    }
                }
                
                // Try scanning for matching target types in collections
                for (jakarta.persistence.metamodel.Attribute<?, ?> attr : 
                        entityManager.getMetamodel().entity(sourceClass).getAttributes()) {
                    if (attr.isCollection() && isEntityCollection(attr, targetClass)) {
                        return attr.getName();
                    } else if (attr.isAssociation() && isMatchingEntityType(attr, targetClass)) {
                        return attr.getName();
                    }
                }
                
                // If still not found, check bidirectional relationships from target to source
                return findBidirectionalPath(sourceClass, targetClass);
            } catch (Exception e) {
                log.warn("Error finding relationship path: {}", e.getMessage());
                return null;
            }
        });
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
}
