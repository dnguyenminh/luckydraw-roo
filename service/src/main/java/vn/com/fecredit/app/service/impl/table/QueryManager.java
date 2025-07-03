package vn.com.fecredit.app.service.impl.table;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.SortRequest;
import vn.com.fecredit.app.service.dto.SortType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;

/**
 * Consolidated class for all Query-related operations.
 * This class combines functionality from:
 * - QueryExecutor
 * - QueryHandler
 * - QueryBuilder
 * - CriteriaQueryBuilder
 * - CountQueryExecutor
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QueryManager {

    private final EntityManager entityManager;
    private final PaginationHelper paginationHelper;
    private final PredicateManager predicateManager;
    private final JoinManager joinManager;

    /**
     * Builds a criteria query with all necessary filters, joins, and selections
     * 
     * @param request The table fetch request containing filters and other parameters
     * @param rootEntityClass The entity class to build the query from
     * @return The built criteria query
     */
    public CriteriaQuery<Tuple> buildCriteriaQuery(TableFetchRequest request, Class<?> rootEntityClass) {
        if (request == null || rootEntityClass == null) {
            log.error("Cannot build criteria query with null request or root entity class");
            return null;
        }

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> query = cb.createTupleQuery();

            // Create root
            Root<?> root = query.from(rootEntityClass);

            // Create joins from search map and view columns
            Map<String, Join<?, ?>> joinMap = joinManager.createJoinsFromSearchMapAndViewColumns(
                    request.getSearch(), request.getViewColumns(), root);

            // Get columns and create selections
            List<Selection<?>> selections = createSelections(request, root, joinMap);
            if (!selections.isEmpty()) {
                query.multiselect(selections);
            } else {
                // Default to ID selection if no columns specified
                Path<?> idPath = root.get("id");
                query.multiselect(idPath.alias("id"));
            }

            // Apply predicates
            List<Predicate> predicates = predicateManager.buildPredicates(request, cb, root);
            if (!predicates.isEmpty()) {
                query.where(predicates.toArray(new Predicate[0]));
            }

            // Apply ordering
            List<Order> orders = createOrdering(request, cb, root, joinMap);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }

            // Set distinct if needed
            query.distinct(true);

            return query;

        } catch (Exception e) {
            log.error("Error building criteria query: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Executes a query with pagination applied
     * 
     * @param query The criteria query to execute
     * @param request The table fetch request containing pagination info
     * @return List of tuple results with pagination applied
     */
    public List<Tuple> executeQueryWithPagination(CriteriaQuery<Tuple> query, TableFetchRequest request) {
        try {
            Pageable pageable = paginationHelper.createPageable(request);

            // Ensure the query is properly using distinct to avoid duplicate results
            query.distinct(true);

            // Validate pagination parameters
            int firstResult = (int) pageable.getOffset();
            int maxResults = pageable.getPageSize();

            if (firstResult < 0) {
                firstResult = 0;
                log.warn("Negative first result detected, defaulting to 0");
            }

            if (maxResults <= 0) {
                maxResults = 10; // Default to 10 if max results is invalid
                log.warn("Invalid max results detected, defaulting to 10");
            }

            // Get a typed query and apply pagination
            TypedQuery<Tuple> typedQuery = entityManager.createQuery(query)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults);

            // Execute the query with timeout
            typedQuery.setHint("jakarta.persistence.query.timeout", 30000); // 30 seconds timeout
            List<Tuple> results = typedQuery.getResultList();

            // Optional: Deduplicate by ID if needed
            if (request.getObjectType() != null && request.getObjectType() == ObjectType.Event) {
                results = deduplicateResults(results);
            }

            log.info("Query executed successfully, returning {} results", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error executing paginated query: {}", e.getMessage(), e);
            return Collections.emptyList(); // Return empty list instead of null
        }
    }
    
    /**
     * Counts the total number of records that match the criteria
     *
     * @param query The data query whose structure should be used for counting
     * @return The total count of matching records
     */
    public long countTotalRecords(CriteriaQuery<Tuple> query) {
        try {
            log.debug("Creating count query from original query");
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

            // Get the root from original query and create corresponding root for count query
            Root<?> originalRoot = query.getRoots().iterator().next();
            Root<?> countRoot = countQuery.from(originalRoot.getJavaType());
            
            // Copy all joins from original query to count query
            Map<String, Join<?, ?>> joinMap = joinManager.copyJoins(originalRoot, countRoot);
            
            // Copy restrictions from original query
            Predicate originalRestriction = query.getRestriction();
            if (originalRestriction != null) {
                // Copy the predicate to work with the new root and joins
                Predicate copiedPredicate = predicateManager.copyPredicate(originalRestriction, cb, countRoot, joinMap);
                countQuery.where(copiedPredicate);
            }
            
            // Set count selection with distinct if the original query was distinct
            countQuery.select(query.isDistinct() 
                ? cb.countDistinct(countRoot)
                : cb.count(countRoot));
            
            // Execute count query
            Long result = entityManager.createQuery(countQuery).getSingleResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            log.error("Error executing count query: {}", e.getMessage(), e);
            return 0L;
        }
    }
    
    /**
     * Create selections from the provided view columns
     */
    private List<Selection<?>> createSelections(
            TableFetchRequest request, 
            Root<?> root,
            Map<String, Join<?, ?>> joinMap) {
        
        List<Selection<?>> selections = new ArrayList<>();
        List<ColumnInfo> viewColumns = request.getViewColumns();
        
        // Check if "id" is already in the view columns to avoid duplicate alias
        boolean idAlreadyIncluded = viewColumns != null && viewColumns.stream()
            .anyMatch(col -> "id".equals(col.getFieldName()));
        
        // Always include ID for consistency if not already included
        if (!idAlreadyIncluded) {
            try {
                Path<?> idPath = root.get("id");
                selections.add(idPath.alias("id"));
            } catch (IllegalArgumentException e) {
                log.warn("Entity {} does not have an 'id' field", root.getJavaType().getSimpleName());
            }
        }
        
        // If no columns specified, return just ID
        if (viewColumns == null || viewColumns.isEmpty()) {
            return selections;
        }
        
        // Process each column
        for (ColumnInfo column : viewColumns) {
            String fieldName = column.getFieldName();
            if (fieldName == null || fieldName.isEmpty()) {
                continue;
            }
            
            try {
                // Handle nested paths with joins
                Path<?> path;
                String alias = fieldName.replace(".", "_");
                
                // Log joins before resolving path
                if (log.isDebugEnabled() && fieldName.contains(".")) {
                    log.debug("Available joins for nested path '{}': {}", 
                        fieldName, 
                        String.join(", ", joinMap.keySet()));
                }
                
                // Use the safer method to resolve the path
                path = safelyResolvePath(fieldName, root, joinMap);
                
                if (path != null) {
                    // For debugging
                    if (log.isDebugEnabled()) {
                        log.debug("Adding selection for '{}' with alias '{}'", fieldName, alias);
                    }
                    selections.add(path.alias(alias));
                } else {
                    log.warn("Could not resolve path for selection: {}", fieldName);
                }
            } catch (IllegalArgumentException e) {
                log.debug("Skipping invalid column {}: {}", fieldName, e.getMessage());
                // Skip invalid columns
            }
        }
        
        return selections;
    }
    
    /**
     * Creates ordering (ORDER BY) clauses based on sort requests
     */
    private List<Order> createOrdering(
            TableFetchRequest request,
            CriteriaBuilder cb,
            Root<?> root,
            Map<String, Join<?, ?>> joinMap) {
        
        List<Order> orders = new ArrayList<>();
        List<SortRequest> sortRequests = request.getSorts();
        
        if (sortRequests == null || sortRequests.isEmpty()) {
            // Default sort by ID if available
            try {
                Path<?> idPath = root.get("id");
                orders.add(cb.asc(idPath));
            } catch (IllegalArgumentException e) {
                // No ID field, skip default sorting
            }
            return orders;
        }
        
        for (SortRequest sortRequest : sortRequests) {
            String fieldName = sortRequest.getField();
            if (fieldName == null || fieldName.isEmpty()) {
                continue;
            }
            
            try {
                // Use the safer method to resolve the path
                Path<?> path = safelyResolvePath(fieldName, root, joinMap);
                
                if (path != null) {
                    SortType sortType = sortRequest.getSortType();
                    if (sortType == SortType.DESCENDING) {
                        orders.add(cb.desc(path));
                    } else {
                        orders.add(cb.asc(path));
                    }
                }
            } catch (IllegalArgumentException e) {
                log.debug("Skipping invalid sort field {}: {}", fieldName, e.getMessage());
                // Skip invalid fields
            }
        }
        
        return orders;
    }
    
    /**
     * Deduplicates results by ID field
     */
    private List<Tuple> deduplicateResults(List<Tuple> results) {
        Map<Object, Tuple> uniqueById = new HashMap<>();
        
        for (Tuple tuple : results) {
            Object id = tuple.get("id");
            if (id != null && !uniqueById.containsKey(id)) {
                uniqueById.put(id, tuple);
            }
        }

        List<Tuple> deduplicatedResults = new ArrayList<>(uniqueById.values());
        log.info("After dedupliculation: {} results (was {})",
                deduplicatedResults.size(), results.size());
        
        return deduplicatedResults;
    }
    
    /**
     * Creates a generic selection query for the given request and root
     * This helps support the old QueryHandler functionality
     * 
     * @param cb The criteria builder
     * @param resultType The result type class
     * @param query The criteria query
     * @param request The table fetch request
     * @param root The query root
     * @return The criteria query with selections applied
     */
    /**
     * Creates a generic selection query with proper handling of aliases and joins
     * This method is used by the QueryHandler for backwards compatibility
     * 
     * @param <T> The result type
     * @param cb The criteria builder
     * @param resultType The result class type
     * @param query The criteria query
     * @param request The fetch request containing filter and column information
     * @param root The root entity
     * @return The configured criteria query
     */
    public <T> CriteriaQuery<T> createGenericSelectionQuery(
            CriteriaBuilder cb,
            Class<T> resultType,
            CriteriaQuery<T> query,
            TableFetchRequest request,
            Root<?> root) {
        
        try {
            // Create joins from search map and view columns
            Map<String, Join<?, ?>> joinMap = joinManager.createJoinsFromSearchMapAndViewColumns(
                    request.getSearch(), request.getViewColumns(), root);
            
            // Apply predicates
            List<Predicate> predicates = predicateManager.buildPredicates(request, cb, root);
            if (!predicates.isEmpty()) {
                query.where(predicates.toArray(new Predicate[0]));
            }
    
            // Apply ordering
            List<Order> orders = createOrdering(request, cb, root, joinMap);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }
    
            // Apply distinct if needed to prevent duplicates
            query.distinct(true);
            
            return query;
        } catch (Exception e) {
            log.error("Error creating selection query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create selection query", e);
        }
    }

    /**
     * Safely resolves a path from a field name, handling nested properties better
     * 
     * @param fieldName The field name to resolve
     * @param root The root entity
     * @param joinMap The join map
     * @return The resolved path
     */
    private Path<?> safelyResolvePath(String fieldName, Root<?> root, Map<String, Join<?, ?>> joinMap) {
        try {
            if (fieldName == null || fieldName.isEmpty()) {
                return null;
            }
            
            if (!fieldName.contains(".")) {
                // Direct property of root
                return root.get(fieldName);
            }
            
            // Break down the path
            String[] parts = fieldName.split("\\.");
            
            // Check if path ends with a basic attribute, not an entity
            boolean endsWithBasicAttribute = true;
            try {
                Class<?> rootClass = root.getJavaType();
                Class<?> currentClass = rootClass;
                
                // Navigate through the path to find the type of the last segment
                for (int i = 0; i < parts.length - 1; i++) {
                    Field field = findField(currentClass, parts[i]);
                    if (field == null) {
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
                
                // Check if the last part is a basic attribute or an entity
                Field lastField = findField(currentClass, parts[parts.length - 1]);
                if (lastField != null) {
                    Class<?> lastFieldType = lastField.getType();
                    endsWithBasicAttribute = !lastFieldType.isAnnotationPresent(jakarta.persistence.Entity.class) && 
                                           !java.util.Collection.class.isAssignableFrom(lastFieldType);
                }
            } catch (Exception e) {
                log.debug("Error determining if path ends with basic attribute: {}", e.getMessage());
                // Default to true if we can't determine
                endsWithBasicAttribute = true;
            }
            
            // Find the appropriate join for the path
            String joinPath;
            if (endsWithBasicAttribute) {
                // If it ends with a basic attribute, we need to join up to the penultimate segment
                String[] joinParts = new String[parts.length - 1];
                System.arraycopy(parts, 0, joinParts, 0, joinParts.length);
                joinPath = String.join(".", joinParts);
            } else {
                // If it ends with an entity, we need to join the entire path
                joinPath = fieldName;
            }
            
            // Try finding the exact join or the longest matching prefix
            Join<?, ?> matchingJoin = joinMap.get(joinPath);
            String matchingPrefix = joinPath;
            
            // If no exact match, find the longest matching prefix
            if (matchingJoin == null) {
                // Sort keys by length (descending) to find the longest match first
                List<String> sortedKeys = new ArrayList<>(joinMap.keySet());
                sortedKeys.sort((a, b) -> Integer.compare(b.length(), a.length()));
                
                for (String prefix : sortedKeys) {
                    // Check both exact equality and prefix match (with a dot to ensure proper path boundaries)
                    if (joinPath.equals(prefix) || 
                        joinPath.startsWith(prefix + ".") || 
                        (prefix.startsWith(joinPath) && prefix.charAt(joinPath.length()) == '.')) {
                        matchingJoin = joinMap.get(prefix);
                        matchingPrefix = prefix;
                        // Break on exact match to avoid further searches
                        if (joinPath.equals(prefix)) {
                            break;
                        }
                    }
                }
            }
            
            if (matchingJoin != null) {
                // If we have an exact match and the path is the same as the join path
                if (matchingPrefix.equals(fieldName)) {
                    return matchingJoin;
                }
                
                // Calculate remaining path based on whether this is a query for a basic attribute
                String remainingPath;
                if (endsWithBasicAttribute) {
                    // If the match is for the full join path
                    if (matchingPrefix.equals(joinPath)) {
                        // Just get the last attribute
                        return matchingJoin.get(parts[parts.length - 1]);
                    } else {
                        // Navigate from the matching join to the parent of the attribute, then get the attribute
                        remainingPath = joinPath.substring(matchingPrefix.length());
                        if (remainingPath.startsWith(".")) {
                            remainingPath = remainingPath.substring(1);
                        }
                        
                        // Navigate through the remaining join path
                        Path<?> currentPath = matchingJoin;
                        String[] remainingParts = remainingPath.split("\\.");
                        
                        for (String part : remainingParts) {
                            currentPath = currentPath.get(part);
                        }
                        
                        // Finally, get the attribute
                        return currentPath.get(parts[parts.length - 1]);
                    }
                } else {
                    // For entity paths, navigate the full remaining path
                    remainingPath = fieldName.substring(matchingPrefix.length());
                    if (remainingPath.startsWith(".")) {
                        remainingPath = remainingPath.substring(1);
                    }
                    
                    // Navigate through the remaining parts
                    Path<?> currentPath = matchingJoin;
                    String[] remainingParts = remainingPath.split("\\.");
                    
                    for (String part : remainingParts) {
                        currentPath = currentPath.get(part);
                    }
                    return currentPath;
                }
            } else {
                // No join found, try to navigate from root
                // For basic attributes, we should have already created proper joins in createJoinPathRecursively
                // But as a fallback, try direct navigation
                Path<?> currentPath = root;
                if (endsWithBasicAttribute) {
                    // Navigate to the parent and get the attribute
                    for (int i = 0; i < parts.length - 1; i++) {
                        currentPath = currentPath.get(parts[i]);
                    }
                    return currentPath.get(parts[parts.length - 1]);
                } else {
                    // Navigate the full path
                    for (String part : parts) {
                        currentPath = currentPath.get(part);
                    }
                    return currentPath;
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve path for field '{}': {}", fieldName, e.getMessage());
            return null;
        }
    }
    
    /**
     * Find a field in a class by name, handling inheritance
     */
    private Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return null;
        }
        
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Try superclass if exists
            if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
                return findField(clazz.getSuperclass(), fieldName);
            }
            return null;
        }
    }
}
