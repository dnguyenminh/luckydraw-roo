package vn.com.fecredit.app.service.impl.table;

import lombok.Getter;
import vn.com.fecredit.app.service.dto.ObjectType;

import java.util.*;

/**
 * Represents a join structure for a query, including relationships and join paths
 */
public class JoinStructure {
    
    @Getter
    private final Class<?> rootEntityClass;
    
    @Getter
    private final Map<ObjectType, Class<?>> targetEntities;
    
    private final Map<Class<?>, List<RelationshipInfo>> relationshipsBySource;
    
    private final List<JoinInfo> joins;
    
    public JoinStructure(Class<?> rootEntityClass) {
        this.rootEntityClass = rootEntityClass;
        this.targetEntities = new HashMap<>();
        this.relationshipsBySource = new HashMap<>();
        this.joins = new ArrayList<>();
    }
    
    /**
     * Add a target entity to the join structure
     */
    public void addTargetEntity(ObjectType objectType, Class<?> entityClass) {
        targetEntities.put(objectType, entityClass);
    }
    
    /**
     * Add a relationship between entities
     */
    public void addRelationship(Class<?> sourceEntityClass, String propertyName, Class<?> relatedEntityClass) {
        relationshipsBySource
            .computeIfAbsent(sourceEntityClass, k -> new ArrayList<>())
            .add(new RelationshipInfo(sourceEntityClass, propertyName, relatedEntityClass));
    }
    
    /**
     * Add a join to the structure
     */
    public void addJoin(JoinInfo joinInfo) {
        if (!joins.contains(joinInfo)) {
            joins.add(joinInfo);
        }
    }
    
    /**
     * Get relationships from a source entity class
     */
    public List<RelationshipInfo> getRelationshipsFrom(Class<?> sourceEntityClass) {
        return relationshipsBySource.get(sourceEntityClass);
    }
    
    /**
     * Get all joins in the structure
     */
    public List<JoinInfo> getAllJoins() {
        return Collections.unmodifiableList(joins);
    }
}
