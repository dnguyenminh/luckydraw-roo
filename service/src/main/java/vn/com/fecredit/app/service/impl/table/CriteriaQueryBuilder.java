package vn.com.fecredit.app.service.impl.table;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.service.dto.*;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CriteriaQueryBuilder {

    private final EntityManager entityManager;
    private final PredicateBuilder predicateBuilder;
    private final RepositoryFactory repositoryFactory;

    // Add a field to store mapping between SQL-safe aliases and original field names
    private final Map<String, String> aliasToFieldNameMapping = new HashMap<>();

    public CriteriaQuery<Tuple> buildCriteriaQuery(TableFetchRequest request, Class<?> rootEntityClass) {
        try {
            log.info("Building criteria query for {} with {} view columns", 
                    rootEntityClass.getSimpleName(),
                    request.getViewColumns() != null ? request.getViewColumns().size() : 0);
            
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> query = cb.createTupleQuery();
            Root<?> root = query.from(rootEntityClass);

            // Create joins map to track all joins
            Map<String, Join<?, ?>> joins = new HashMap<>();

            // First handle view column joins to get the basic structure
            if (request.getViewColumns() != null) {
                createViewColumnJoins(request.getViewColumns(), root, joins);
            }

            // Then handle search map joins to augment the structure
            if (request.getSearch() != null) {
                createSearchMapJoins(request.getSearch(), rootEntityClass, root, joins);
            }

            // Create selections based on viewColumns
            List<Selection<?>> selections = createSelections(request.getViewColumns(), root, joins);
            if (selections.isEmpty()) {
                // Instead of returning null, create a default selection with ID
                log.warn("No valid columns specified in viewColumns, using ID as default");
                selections.add(root.get("id").alias("id"));
            }
            query.multiselect(selections);

            // Apply filters and search criteria
            List<Predicate> predicates = predicateBuilder.buildPredicates(request, cb, root);
            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            // Apply sorting
            List<Order> orders = buildOrderClauses(request.getSorts(), cb, root, joins);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }

            query.distinct(true);
            log.info("Successfully built criteria query for {}", rootEntityClass.getSimpleName());
            return query;
        } catch (Exception e) {
            log.error("Error building criteria query for {}: {}", 
                     rootEntityClass.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    private void createViewColumnJoins(List<ColumnInfo> viewColumns, Root<?> root, Map<String, Join<?, ?>> joins) {
        for (ColumnInfo column : viewColumns) {
            String[] parts = column.getFieldName().split("\\.");
            if (parts.length > 1) {
                From<?, ?> currentFrom = root;
                StringBuilder joinPath = new StringBuilder();

                for (int i = 0; i < parts.length - 1; i++) {
                    String part = parts[i];
                    if (joinPath.length() > 0) {
                        joinPath.append(".");
                    }
                    joinPath.append(part);

                    String joinKey = joinPath.toString();
                    if (!joins.containsKey(joinKey)) {
                        Join<?, ?> join = currentFrom.join(part, JoinType.LEFT);
                        joins.put(joinKey, join);
                        currentFrom = join;
                        log.debug("Created join for view column: {} -> {}", joinKey, part);
                    } else {
                        currentFrom = joins.get(joinKey);
                    }
                }
            }
        }
    }

    private void createSearchMapJoins(Map<ObjectType, DataObject> search, Class<?> rootEntityClass, Root<?> root,
            Map<String, Join<?, ?>> joins) {
        for (Map.Entry<ObjectType, DataObject> entry : search.entrySet()) {
            String relationshipPath = getRelationshipPath(rootEntityClass, entry.getKey());
            if (relationshipPath != null) {
                String[] parts = relationshipPath.split("\\.");
                From<?, ?> currentFrom = root;
                StringBuilder joinPath = new StringBuilder();

                for (String part : parts) {
                    if (joinPath.length() > 0) {
                        joinPath.append(".");
                    }
                    joinPath.append(part);

                    String joinKey = joinPath.toString();
                    if (!joins.containsKey(joinKey)) {
                        Join<?, ?> join = currentFrom.join(part, JoinType.INNER);
                        joins.put(joinKey, join);
                        currentFrom = join;
                        log.debug("Created join for search type {}: {} -> {}", entry.getKey(), joinKey, part);
                    } else {
                        // If join already exists from view columns (LEFT), upgrade to INNER if needed
                        Join<?, ?> existingJoin = joins.get(joinKey);
                        if (existingJoin.getJoinType() == JoinType.LEFT) {
                            joins.put(joinKey, currentFrom.join(part, JoinType.INNER));
                        }
                        currentFrom = joins.get(joinKey);
                    }
                }
            }
        }
    }

    private List<Selection<?>> createSelections(List<ColumnInfo> viewColumns, Root<?> root,
            Map<String, Join<?, ?>> joins) {
        List<Selection<?>> selections = new ArrayList<>();
        if (viewColumns == null || viewColumns.isEmpty()) {
            return selections;
        }
        
        // Create a mapping for field name to SQL-safe alias
        Map<String, String> fieldToAliasMapping = new HashMap<>();

        Set<String> usedAliases = new HashSet<>();
        // Add standard ID field if not already included
        boolean hasIdColumn = viewColumns.stream()
                .anyMatch(col -> "id".equals(col.getFieldName()));
        if (!hasIdColumn) {
            selections.add(root.get("id").alias("id"));
            usedAliases.add("id");
        }

        for (ColumnInfo column : viewColumns) {
            String fieldName = column.getFieldName();
            // Create SQL-safe alias (replace dots with underscores)
            String sqlSafeAlias = fieldName.replace(".", "_");
            
            // Store the mapping
            fieldToAliasMapping.put(fieldName, sqlSafeAlias);
            
            try {
                String[] parts = fieldName.split("\\.");
                
                if (parts.length == 1) {
                    // Direct field on root entity (e.g., "id", "name", "status")
                    Path<?> path = root.get(parts[0]);
                    Selection<?> selection = path.alias(sqlSafeAlias);
                    selections.add(selection);
                    usedAliases.add(sqlSafeAlias);
                    log.debug("Added direct field selection: {} as {}", fieldName, sqlSafeAlias);
                } else {
                    // Nested field requiring joins (e.g., "locations.region.name")
                    From<?, ?> current = root;
                    Path<?> path = null;
                    boolean joinPathComplete = true;
                    
                    // Build path from all join parts
                    for (int i = 0; i < parts.length - 1; i++) {
                        // String joinPart = parts[i];
                        String joinKey = String.join(".", java.util.Arrays.copyOfRange(parts, 0, i + 1));
                        
                        Join<?, ?> join = joins.get(joinKey);
                        if (join == null) {
                            log.warn("Join not found for path: {}", joinKey);
                            joinPathComplete = false;
                            break;
                        }
                        current = join;
                    }
                    
                    // If we successfully traversed the joins, add the final attribute
                    if (joinPathComplete && current != null) {
                        String finalAttribute = parts[parts.length - 1];
                        try {
                            path = current.get(finalAttribute);
                            Selection<?> selection = path.alias(sqlSafeAlias);
                            selections.add(selection);
                            usedAliases.add(sqlSafeAlias);
                            log.debug("Added nested field selection: {} as {}", fieldName, sqlSafeAlias);
                        } catch (IllegalArgumentException e) {
                            log.error("Cannot get attribute '{}' from join {}: {}", 
                                finalAttribute, current, e.getMessage());
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                log.error("Invalid field path: {} - {}", fieldName, e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error processing field {}: {}", fieldName, e.getMessage(), e);
            }
        }

        // If no selections were added, add ID as a fallback
        if (selections.isEmpty()) {
            log.warn("No valid columns selected, adding ID as fallback");
            selections.add(root.get("id").alias("id"));
        }
        
        // Store the mapping in a ThreadLocal or somewhere it can be accessed by ResponseBuilder
        log.info("Field to alias mapping: {}", fieldToAliasMapping);
        
        // Log all selections for debugging
        log.info("Created {} selections: {}", selections.size(), 
                 selections.stream().map(Selection::getAlias).collect(Collectors.joining(", ")));

        return selections;
    }

    private List<Order> buildOrderClauses(List<SortRequest> sorts, CriteriaBuilder cb, Root<?> root,
            Map<String, Join<?, ?>> joins) {
        List<Order> orders = new ArrayList<>();
        if (sorts == null || sorts.isEmpty()) {
            return orders;
        }

        for (SortRequest sort : sorts) {
            String[] parts = sort.getField().split("\\.");
            Path<?> path;

            if (parts.length == 1) {
                path = root.get(parts[0]);
                orders.add(sort.getSortType() == SortType.ASCENDING ? cb.asc(path) : cb.desc(path));
            } else {
                From<?, ?> current = root;
                for (int i = 0; i < parts.length - 1; i++) {
                    String joinKey = String.join(".", java.util.Arrays.copyOfRange(parts, 0, i + 1));
                    current = joins.get(joinKey);
                    if (current == null) {
                        break;
                    }
                }
                if (current != null) {
                    path = current.get(parts[parts.length - 1]);
                    orders.add(sort.getSortType() == SortType.ASCENDING ? cb.asc(path) : cb.desc(path));
                }
            }
        }

        return orders;
    }

    /**
     * Dynamically discovers the relationship path between two entity classes.
     * Uses JPA metamodel to find direct relationships and recursively builds paths
     * for indirect relationships.
     * 
     * @param sourceClass The source entity class
     * @param targetType The target entity type to find a path to
     * @return The path expression as a dot-separated string, or null if no path exists
     */
    private String getRelationshipPath(Class<?> sourceClass, ObjectType targetType) {
        // Get target class
        Class<?> targetClass = repositoryFactory.getEntityClass(targetType);
        if (targetClass == null) {
            log.warn("Could not resolve target class for ObjectType: {}", targetType);
            return null;
        }
        
        // First check for direct relationships using JPA metamodel
        String directPath = findDirectRelationship(sourceClass, targetClass);
        if (directPath != null) {
            return directPath;
        }
        
        // If no direct relationship, try to find an indirect path (limited to 2 levels deep for performance)
        return findIndirectRelationship(sourceClass, targetClass, 0, 2);
    }
    
    /**
     * Finds a direct relationship between source and target classes using JPA metamodel
     */
    private String findDirectRelationship(Class<?> sourceClass, Class<?> targetClass) {
        try {
            jakarta.persistence.metamodel.EntityType<?> entityType = entityManager.getMetamodel().entity(sourceClass);
            
            // Search all attributes for a matching relationship
            for (jakarta.persistence.metamodel.Attribute<?, ?> attr : entityType.getAttributes()) {
                if (attr.isCollection()) {
                    // Handle collection relationships (OneToMany, ManyToMany)
                    jakarta.persistence.metamodel.PluralAttribute<?, ?, ?> pluralAttr = 
                        (jakarta.persistence.metamodel.PluralAttribute<?, ?, ?>) attr;
                    if (pluralAttr.getElementType().getJavaType().equals(targetClass)) {
                        return attr.getName();
                    }
                } else if (attr.isAssociation() && attr.getJavaType().equals(targetClass)) {
                    // Handle singular relationships (ManyToOne, OneToOne)
                    return attr.getName();
                }
            }
        } catch (Exception e) {
            log.debug("Error checking direct relationship from {} to {}: {}", 
                sourceClass.getSimpleName(), targetClass.getSimpleName(), e.getMessage());
        }
        return null;
    }
    
    /**
     * Recursively searches for an indirect relationship between classes
     * @param depth Current recursion depth
     * @param maxDepth Maximum recursion depth to prevent infinite loops
     */
    private String findIndirectRelationship(Class<?> sourceClass, Class<?> targetClass, int depth, int maxDepth) {
        if (depth >= maxDepth) {
            return null; // Prevent excessive recursion
        }
        
        try {
            jakarta.persistence.metamodel.EntityType<?> entityType = entityManager.getMetamodel().entity(sourceClass);
            
            // For each relationship in the source class
            for (jakarta.persistence.metamodel.Attribute<?, ?> attr : entityType.getAttributes()) {
                if (!attr.isAssociation()) {
                    continue; // Skip non-relationship attributes
                }
                
                Class<?> relatedClass;
                if (attr.isCollection()) {
                    // Get the element type for collections
                    jakarta.persistence.metamodel.PluralAttribute<?, ?, ?> pluralAttr = 
                        (jakarta.persistence.metamodel.PluralAttribute<?, ?, ?>) attr;
                    relatedClass = pluralAttr.getElementType().getJavaType();
                } else {
                    // Get the attribute type for singular associations
                    relatedClass = attr.getJavaType();
                }
                
                // Check if this related class has a direct relationship to the target
                String nextLevelPath = findDirectRelationship(relatedClass, targetClass);
                if (nextLevelPath != null) {
                    // Found a path: [current_attribute].[next_level_path]
                    return attr.getName() + "." + nextLevelPath;
                }
                
                // If not found, try one level deeper (if we're not at max depth)
                if (depth + 1 < maxDepth) {
                    String deeperPath = findIndirectRelationship(relatedClass, targetClass, depth + 1, maxDepth);
                    if (deeperPath != null) {
                        // Found a deeper path
                        return attr.getName() + "." + deeperPath;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error finding indirect relationship at depth {} from {} to {}: {}", 
                depth, sourceClass.getSimpleName(), targetClass.getSimpleName(), e.getMessage());
        }
        
        return null;
    }

    /**
     * Get the mapping between SQL-safe aliases and original field names
     * @return The mapping of aliases to field names
     */
    public Map<String, String> getAliasToFieldNameMapping() {
        return Collections.unmodifiableMap(aliasToFieldNameMapping);
    }
}