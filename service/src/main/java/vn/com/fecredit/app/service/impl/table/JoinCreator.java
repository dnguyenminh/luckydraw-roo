package vn.com.fecredit.app.service.impl.table;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JoinCreator {
    private static final Logger log = LoggerFactory.getLogger(JoinCreator.class);
    private final RepositoryFactory repositoryFactory;
    private final EntityManager entityManager;

    public JoinCreator(RepositoryFactory repositoryFactory, EntityManager entityManager) {
        this.repositoryFactory = repositoryFactory;
        this.entityManager = entityManager;
    }

    public Map<String, Join<?, ?>> createJoinsFromSearchMapAndViewColumns(
        Map<ObjectType, DataObject> searchMap,
        List<ColumnInfo> viewColumns,
        Class<?> currentEntityClass
    ) {
        Map<String, Join<?, ?>> joins = new HashMap<>();
        if ((searchMap == null || searchMap.isEmpty()) && (viewColumns == null || viewColumns.isEmpty()) || currentEntityClass == null) {
            return joins;
        }

        // Create a CriteriaQuery to initialize the Root
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery();
        Root<?> root = query.from(currentEntityClass);

        // Track created joins to avoid duplicates
        Map<String, Join<?, ?>> joinCache = new HashMap<>();
        // Process searchMap
        createJoinsRecursively(searchMap, viewColumns, currentEntityClass, root, joins, joinCache);
        // Process viewColumns separately if needed
        createJoinsForViewColumns(viewColumns, currentEntityClass, root, joins, joinCache);

        return joins;
    }

    private void createJoinsRecursively(
        Map<ObjectType, DataObject> searchMap,
        List<ColumnInfo> viewColumns,
        Class<?> currentEntityClass,
        Path<?> currentPath,
        Map<String, Join<?, ?>> joins,
        Map<String, Join<?, ?>> joinCache
    ) {
        if ((searchMap == null || searchMap.isEmpty()) && (viewColumns == null || viewColumns.isEmpty()) || currentEntityClass == null) {
            return;
        }

        // Get all fields of the current entity class
        for (Field field : currentEntityClass.getDeclaredFields()) {
            if (isRelationshipField(field)) {
                Class<?> relatedClass = getRelatedEntityClass(field);
                ObjectType relatedObjectType = findMatchingObjectType(relatedClass, searchMap);

                // Check if the field is relevant to searchMap or viewColumns
                boolean isRelevant = (relatedObjectType != null && searchMap != null && searchMap.containsKey(relatedObjectType)) ||
                    (viewColumns != null && viewColumns.stream().anyMatch(vc -> vc.getFieldName().startsWith(field.getName() + ".")));

                if (isRelevant) {
                    try {
                        String joinField = field.getName();
                        // Create a unique key for the join
                        String joinKey = currentPath.getModel().getBindableJavaType().getName() + "." + joinField;

                        // Check if the join already exists
                        Join<?, ?> join = joinCache.get(joinKey);
                        if (join == null) {
                            // Create new join
                            if (currentPath instanceof Root<?>) {
                                join = ((Root<?>) currentPath).join(joinField, JoinType.INNER);
                            } else if (currentPath instanceof Join<?, ?>) {
                                join = ((Join<?, ?>) currentPath).join(joinField, JoinType.INNER);
                            } else {
                                log.warn("Cannot create join from path type: {}", currentPath.getClass().getName());
                                continue;
                            }
                            joinCache.put(joinKey, join);
                            joins.put(joinField, join);
                        }

                        // Remove the processed type from searchMap to avoid reprocessing
                        Map<ObjectType, DataObject> remainingSearchMap = searchMap != null ? new HashMap<>(searchMap) : new HashMap<>();
                        if (relatedObjectType != null) {
                            remainingSearchMap.remove(relatedObjectType);
                        }

                        // Recursively process joins for the related entity
                        createJoinsRecursively(remainingSearchMap, viewColumns, relatedClass, join, joins, joinCache);
                    } catch (IllegalArgumentException e) {
                        log.warn("Could not create join for field: {} in entity: {}",
                            field.getName(), currentEntityClass.getSimpleName(), e);
                    }
                }
            }
        }
    }

    private void createJoinsForViewColumns(
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

    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private boolean isRelationshipField(Field field) {
        return field != null && (
            field.isAnnotationPresent(OneToOne.class) ||
                field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToOne.class) ||
                field.isAnnotationPresent(ManyToMany.class)
        );
    }

    private Class<?> getRelatedEntityClass(Field field) {
        if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
            return field.getType();
        } else if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
        return null;
    }

    private ObjectType findMatchingObjectType(Class<?> relatedClass, Map<ObjectType, DataObject> searchMap) {
        if (searchMap == null) {
            return null;
        }
        return searchMap.keySet().stream()
            .filter(objectType -> {
                Class<?> entityClass = repositoryFactory.getEntityClass(objectType);
                return entityClass != null && entityClass.equals(relatedClass);
            })
            .findFirst()
            .orElse(null);
    }
}
