package vn.com.fecredit.app.service.factory;

import java.util.List;

/**
 * Factory interface for getting related tables information.
 */
public interface RelatedTablesFactory {

    /**
     * Checks if an entity has related tables
     * 
     * @param entity the entity to check
     * @return true if the entity has related tables, false otherwise
     */
    boolean hasRelatedTables(Object entity);

    /**
     * Gets the names of tables related to an entity
     * 
     * @param entity the entity
     * @return list of related table names
     */
    List<String> getRelatedTables(Object entity);
    
    /**
     * Gets the entity classes that are related to this entity
     * 
     * @param entity the entity
     * @return list of related entity classes
     */
    List<Class<?>> getRelatedEntityClasses(Object entity);
}