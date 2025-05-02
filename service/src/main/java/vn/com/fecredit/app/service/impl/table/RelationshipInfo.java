package vn.com.fecredit.app.service.impl.table;

import lombok.Getter;

/**
 * Information about a relationship between entities
 */
@Getter
public class RelationshipInfo {
    private final Class<?> sourceEntityClass;
    private final String propertyName;
    private final Class<?> relatedEntityClass;
    
    public RelationshipInfo(Class<?> sourceEntityClass, String propertyName, Class<?> relatedEntityClass) {
        this.sourceEntityClass = sourceEntityClass;
        this.propertyName = propertyName;
        this.relatedEntityClass = relatedEntityClass;
    }
}
