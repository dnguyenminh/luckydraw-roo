package vn.com.fecredit.app.service.factory;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.com.fecredit.app.service.dto.ObjectType;

/**
 * Factory interface for getting repositories and entity classes based on ObjectType.
 */
public interface RepositoryFactory {

    /**
     * Gets the entity class for the given object type
     * 
     * @param <T> the entity type
     * @param objectType the object type
     * @return the entity class
     */
    <T> Class<T> getEntityClass(ObjectType objectType);

    /**
     * Gets the repository for the given entity class
     * 
     * @param <T> the entity type
     * @param <R> the repository type
     * @param entityClass the entity class
     * @return the repository
     */
    <T, R extends JpaRepository<T, ?>> R getRepositoryForClass(Class<T> entityClass);

    /**
     * Gets the table name for the given object type
     * 
     * @param objectType the object type
     * @return the table name
     */
    String getTableNameForObjectType(ObjectType objectType);
}
