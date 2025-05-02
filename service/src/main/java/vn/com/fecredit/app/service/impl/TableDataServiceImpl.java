package vn.com.fecredit.app.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.repository.AbstractRepository;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.factory.RepositoryFactory;
import vn.com.fecredit.app.service.impl.table.ColumnInfoProvider;
import vn.com.fecredit.app.service.impl.table.CriteriaQueryBuilder;
import vn.com.fecredit.app.service.impl.table.EntityConverter;
import vn.com.fecredit.app.service.impl.table.EntityFinder;
import vn.com.fecredit.app.service.impl.table.PaginationHelper;
import vn.com.fecredit.app.service.impl.table.PredicateBuilder;
import vn.com.fecredit.app.service.impl.table.ResponseBuilder;

/**
 * Implementation of the TableDataService for fetching paginated table data.
 * This class orchestrates the process but delegates most work to specialized
 * components.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableDataServiceImpl implements TableDataService {

    @PersistenceContext
    private EntityManager entityManager;

    private final RepositoryFactory repositoryFactory;
    private final EntityFinder entityFinder;
    private final PredicateBuilder predicateBuilder;
    private final EntityConverter entityConverter;
    // private final CountQueryExecutor countQueryExecutor;
    private final ResponseBuilder responseBuilder;
    private final PaginationHelper paginationHelper;
    // private final JoinCreator joinCreator;
    private final ColumnInfoProvider columnInfoProvider;
    private final CriteriaQueryBuilder criteriaQueryBuilder;

    @Override
    public TableFetchResponse fetchData(TableFetchRequest request) {
        if (request == null) {
            return responseBuilder.createErrorResponse("Request cannot be null");
        }

        try {
            // First try to use ObjectType if provided
            if (request.getObjectType() != null) {
                return fetchByObjectType(request);
                // Fall back to entityName for backward compatibility
            } else if (request.getEntityName() != null) {
                try {
                    // Try to map entity name to ObjectType enum
                    ObjectType objectType = ObjectType.valueOf(request.getEntityName());
                    request.setObjectType(objectType);
                    return fetchByObjectType(request);
                } catch (IllegalArgumentException e) {
                    // Entity name doesn't match any predefined object type
                    return responseBuilder.createErrorResponse("Unsupported entity: " + request.getEntityName());
                }
            } else {
                return responseBuilder.createErrorResponse("No object type or entity name specified");
            }
        } catch (Exception e) {
            log.error("Error fetching table data", e);
            return responseBuilder.createErrorResponse("Error fetching data: " + e.getMessage());
        }
    }

    @Override
    public TableFetchResponse fetchScalarProperties(TableFetchRequest request) {
        try {
            // Validate request
            if (request == null) {
                return responseBuilder.createErrorResponse("Request cannot be null");
            }

            ObjectType objectType = request.getObjectType();
            if (objectType == null) {
                return responseBuilder.createErrorResponse("Unsupported entity: null");
            }
            
            // Verify that the object type is valid by checking if it exists in the enum
            try {
                ObjectType.valueOf(objectType.name());
            } catch (IllegalArgumentException e) {
                return responseBuilder.createErrorResponse("Unsupported entity: " + objectType);
            }
            
            // Ensure we have a valid page size
            if (request.getSize() <= 0) {
                request.setSize(10); // Set default page size
            }

            // Find root entity class - this validates the object type is mappable to an entity
            Class<?> rootEntityClass;
            try {
                rootEntityClass = repositoryFactory.getEntityClass(objectType);
                if (rootEntityClass == null) {
                    return responseBuilder.createErrorResponse("Unsupported entity for object type: " + objectType);
                }
            } catch (Exception e) {
                return responseBuilder.createErrorResponse("Unsupported entity: " + objectType);
            }

            // Create query
            CriteriaQuery<Tuple> query = criteriaQueryBuilder.buildCriteriaQuery(request, rootEntityClass);
            
            // If query is null, create a simple default query to retrieve just IDs
            if (query == null) {
                log.warn("Failed to build query with provided parameters, creating simple default query");
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                query = cb.createTupleQuery();
                Root<?> root = query.from(rootEntityClass);
                query.multiselect(root.get("id").alias("id"));
                query.distinct(true);
            }

            // Log all joins recursively
            Root<?> root = query.getRoots().iterator().next();
            @SuppressWarnings("unchecked")
            Map<String, Object> searchMap = (Map<String, Object>) (Map<?, ?>) request.getSearch();
            logJoinsRecursively(root, "", searchMap);

            try {
                // Get total count - pass the query instead of the request
                long totalCount = countTotalRecords(query);

                // Get paginated results (even if empty)
                List<Tuple> results = executeQueryWithPagination(query, request);
                
                if (results == null) {
                    results = Collections.emptyList();
                    log.warn("Query execution returned null results, using empty list");
                }

                // Build response - let the response builder handle empty results appropriately
                return responseBuilder.buildResponse(
                        request,
                        results,
                        rootEntityClass,
                        totalCount,
                        entityFinder.getTableName(rootEntityClass));
            } catch (Exception e) {
                log.error("Error executing query: {}", e.getMessage(), e);
                return responseBuilder.createErrorResponse("Error executing query: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error in fetchScalarProperties", e);
            return responseBuilder.createErrorResponse("Error fetching scalar properties: " + e.getMessage());
        }
    }

    /**
     * Executes the query with pagination applied
     */
    private List<Tuple> executeQueryWithPagination(CriteriaQuery<Tuple> query, TableFetchRequest request) {
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
                // For Event queries, deduplicate by event ID to match native query behavior
                Map<Object, Tuple> uniqueById = new HashMap<>();
                for (Tuple tuple : results) {
                    Object id = tuple.get("id");
                    if (id != null && !uniqueById.containsKey(id)) {
                        uniqueById.put(id, tuple);
                    }
                }
                
                List<Tuple> deduplicatedResults = new ArrayList<>(uniqueById.values());
                log.info("After deduplication: {} results (was {})", 
                        deduplicatedResults.size(), results.size());
                        
                return deduplicatedResults;
            }
            
            log.info("Query executed successfully, returning {} results", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error executing paginated query: {}", e.getMessage(), e);
            return Collections.emptyList(); // Return empty list instead of null
        }
    }

    // Method moved to CriteriaQueryBuilder class

    // /**
    //  * Builds order clauses for sorting results
    //  */
    // /**
    //  * Builds order clauses for sorting results with support for nested paths.
    //  */
    // private List<jakarta.persistence.criteria.Order> buildOrderClauses(
    //         List<SortRequest> sorts,
    //         CriteriaBuilder cb,
    //         Root<?> root,
    //         Map<String, Join<?, ?>> existingJoins) {

    //     List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
    //     if (sorts == null || sorts.isEmpty()) {
    //         return orders;
    //     }

    //     Map<String, Join<?, ?>> joinMap = new HashMap<>(existingJoins); // Work with a copy and reuse existing joins

    //     for (SortRequest sort : sorts) {
    //         String fieldName = sort.getField();
    //         try {
    //             Path<?> path = getPathForField(fieldName, root, joinMap);
    //             if (path != null) {
    //                 jakarta.persistence.criteria.Order order = (sort.getSortType() == SortType.ASCENDING) ? cb.asc(path)
    //                         : cb.desc(path);
    //                 orders.add(order);
    //                 log.debug("Added order clause for field: {}", fieldName);
    //             }
    //         } catch (IllegalArgumentException e) {
    //             log.warn("Invalid sort field: {} - {}", fieldName, e.getMessage());
    //         }
    //     }

    //     return orders;
    // }

    // /**
    //  * Gets or creates a path for a field, handling nested associations.
    //  */
    // /**
    //  * Gets or creates a path for a field, handling nested associations and
    //  * relationship name mappings
    //  */
    // private Path<?> getPathForField(String fieldName, Root<?> root, Map<String, Join<?, ?>> joinMap) {
    //     try {
    //         String[] fieldParts = fieldName.split("\\.");
    //         if (fieldParts.length == 1) {
    //             return root.get(fieldName);
    //         }

    //         // Handle nested paths
    //         From<?, ?> currentFrom = root;
    //         // Path<?> currentPath = root;

    //         StringBuilder joinKey = new StringBuilder();

    //         // Process all parts except the last one
    //         for (int i = 0; i < fieldParts.length - 1; i++) {
    //             String part = fieldParts[i];

    //             // Build the join key for the current level
    //             if (joinKey.length() > 0) {
    //                 joinKey.append(".");
    //             }
    //             joinKey.append(part);
    //             String currentJoinKey = joinKey.toString();

    //             // Try to find or create the join
    //             Join<?, ?> join = joinMap.get(currentJoinKey);
    //             if (join != null) {
    //                 currentFrom = join;
    //                 // currentPath = join;
    //             } else {
    //                 // Try to find the attribute through the metamodel
    //                 jakarta.persistence.metamodel.ManagedType<?> type = entityManager.getMetamodel()
    //                         .managedType(currentFrom.getJavaType());

    //                 String attributeName = part;
    //                 jakarta.persistence.metamodel.Attribute<?, ?> attribute = null;

    //                 try {
    //                     // Try exact match first
    //                     attribute = type.getAttribute(attributeName);
    //                 } catch (IllegalArgumentException e) {
    //                     // Try without plural 's' if not found
    //                     if (attributeName.endsWith("s")) {
    //                         try {
    //                             attribute = type.getAttribute(attributeName.substring(0, attributeName.length() - 1));
    //                             attributeName = attributeName.substring(0, attributeName.length() - 1);
    //                         } catch (IllegalArgumentException ex) {
    //                             // Ignore and continue to next attempt
    //                         }
    //                     }
    //                 }

    //                 if (attribute != null) {
    //                     Join<?, ?> newJoin = currentFrom.join(attributeName, JoinType.LEFT);
    //                     joinMap.put(currentJoinKey, newJoin);
    //                     currentFrom = newJoin;
    //                     // currentPath = newJoin;
    //                     log.debug("Created join for attribute: {} with key: {}", attributeName, currentJoinKey);
    //                 } else {
    //                     log.warn("Could not find attribute: {} in type: {}", part, type.getJavaType().getSimpleName());
    //                     throw new IllegalArgumentException("Invalid path: " + fieldName);
    //                 }
    //             }
    //         }

    //         // Get the final attribute
    //         String finalPart = fieldParts[fieldParts.length - 1];
    //         try {
    //             return currentFrom.get(finalPart);
    //         } catch (IllegalArgumentException e) {
    //             log.warn("Failed to get attribute {} from {}", finalPart, currentFrom.getModel());
    //             throw e;
    //         }
    //     } catch (Exception e) {
    //         log.error("Error creating path for field {}: {}", fieldName, e.getMessage());
    //         throw new IllegalArgumentException("Could not create path for: " + fieldName, e);
    //     }
    // }

    // /**
    //  * Creates Selection objects for the query based on the view columns.
    //  * Handles nested associations correctly with proper join management.
    //  */
    // private List<Selection<?>> createSelections(List<ColumnInfo> viewColumns, Root<?> root) {
    //     List<Selection<?>> selections = new ArrayList<>();
    //     Map<String, Join<?, ?>> joinMap = new HashMap<>(); // Cache for reusing joins

    //     // If no columns specified, return empty list for handling by caller
    //     if (viewColumns == null || viewColumns.isEmpty()) {
    //         return selections;
    //     }

    //     Set<String> usedAliases = new HashSet<>();
    //     // Add standard ID field first for consistent results if not in viewColumns
    //     boolean hasIdColumn = viewColumns.stream()
    //             .anyMatch(col -> "id".equals(col.getFieldName()));
    //     if (!hasIdColumn) {
    //         selections.add(root.get("id").alias("id"));
    //         usedAliases.add("id");
    //     }

    //     for (ColumnInfo column : viewColumns) {
    //         // Skip if alias already used
    //         if (usedAliases.contains(column.getFieldName())) {
    //             continue;
    //         }
    //         usedAliases.add(column.getFieldName());
    //         String fieldName = column.getFieldName();
    //         // Create a unique alias by adding a numeric suffix if needed
    //         String baseAlias = fieldName.replace(".", "_");
    //         String alias = baseAlias;
    //         int suffix = 1;
    //         while (usedAliases.contains(alias)) {
    //             alias = baseAlias + "_" + suffix++;
    //         }
    //         usedAliases.add(alias);

    //         try {
    //             String[] fieldParts = fieldName.split("\\.");
    //             From<?, ?> currentFrom = root;
    //             Path<?> currentPath = root;
    //             StringBuilder joinMapKey = new StringBuilder();

    //             jakarta.persistence.metamodel.ManagedType<?> currentType = entityManager.getMetamodel()
    //                     .managedType(root.getJavaType());

    //             // Handle nested paths (e.g., "locations.region.name")
    //             for (int i = 0; i < fieldParts.length; i++) {
    //                 String part = fieldParts[i];

    //                 if (i < fieldParts.length - 1) {
    //                     // This is a join part
    //                     if (joinMapKey.length() > 0) {
    //                         joinMapKey.append(".");
    //                     }
    //                     joinMapKey.append(part);
    //                     String joinKey = joinMapKey.toString();

    //                     Join<?, ?> join = joinMap.get(joinKey);
    //                     if (join == null) {
    //                         // Try to find the attribute in the current type
    //                         jakarta.persistence.metamodel.Attribute<?, ?> attribute = findAttribute(currentType, part);
    //                         if (attribute != null) {
    //                             join = ((From<?, ?>) currentPath).join(attribute.getName(), JoinType.LEFT);
    //                             joinMap.put(joinKey, join);
    //                             log.debug("Created new join for path: {} using attribute: {}",
    //                                     joinKey, attribute.getName());

    //                             // Update current type for next iteration
    //                             currentType = entityManager.getMetamodel()
    //                                     .managedType(attribute.getJavaType());
    //                         } else {
    //                             log.warn("Could not find attribute {} in {}",
    //                                     part, currentType.getJavaType().getSimpleName());
    //                             throw new IllegalArgumentException("Invalid path: " + fieldName);
    //                         }
    //                     } else {
    //                         // Update current type for next iteration using existing join
    //                         jakarta.persistence.criteria.Path<?> joinPath = join;
    //                         Class<?> joinClass = joinPath.getJavaType();
    //                         currentType = entityManager.getMetamodel()
    //                                 .managedType(joinClass);
    //                     }
    //                     currentPath = join;
    //                     currentFrom = join;
    //                 } else {
    //                     // This is the final attribute
    //                     try {
    //                         jakarta.persistence.metamodel.Attribute<?, ?> attribute = findAttribute(currentType, part);
    //                         if (attribute != null) {
    //                             Path<?> finalPath = currentFrom.get(attribute.getName());
    //                             Selection<?> selection = finalPath.alias(alias);
    //                             selections.add(selection);
    //                             log.debug("Added selection for path: {} using attribute: {}",
    //                                     fieldName, attribute.getName());
    //                         } else {
    //                             log.warn("Could not find attribute {} in {}",
    //                                     part, currentType.getJavaType().getSimpleName());
    //                         }
    //                     } catch (IllegalArgumentException e) {
    //                         log.warn("Could not create selection for path: {} - {}", fieldName, e.getMessage());
    //                     }
    //                 }
    //             }
    //         } catch (Exception e) {
    //             log.warn("Failed to process field: {} - {}", fieldName, e.getMessage());
    //         }
    //     }

    //     return selections;
    // }

    // /**
    //  * Builds the predicates for filtering and search
    //  */
    // /**
    //  * Builds predicates for filtering and searching with enhanced error handling
    //  */
    // private List<jakarta.persistence.criteria.Predicate> buildPredicates(
    //         TableFetchRequest request, CriteriaBuilder cb, Root<?> root) {
    //     List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

    //     try {
    //         // Add default filter to exclude deleted records
    //         predicateBuilder.addDefaultFilters(cb, root, predicates);

    //         if (request == null) {
    //             log.warn("Request is null, only applying default filters");
    //             return predicates;
    //         }

    //         // Create joins map for handling relationships
    //         Map<String, Join<?, ?>> joinMap = new HashMap<>();

    //         // Apply filters from request with proper path resolution
    //         if (request.getFilters() != null && !request.getFilters().isEmpty()) {
    //             try {
    //                 predicateBuilder.applyFilters(request, predicates, cb, root);
    //             } catch (Exception e) {
    //                 log.error("Error applying filters: {}", e.getMessage());
    //                 // Continue with other predicates even if filters fail
    //             }
    //         }

    //         // Apply global search if specified
    //         if (request.getSearch() != null && !request.getSearch().isEmpty()) {
    //             try {
    //                 List<jakarta.persistence.criteria.Predicate> searchPredicates = new ArrayList<>();

    //                 // Handle text search across columns
    //                 if (request.getViewColumns() != null && !request.getViewColumns().isEmpty()) {
    //                     String searchTerm = request.getSearch().toString().toLowerCase();
    //                     String searchPattern = "%" + searchTerm + "%";

    //                     // Create LIKE predicate for each string field
    //                     for (ColumnInfo column : request.getViewColumns()) {
    //                         try {
    //                             Path<?> path = getPathForField(column.getFieldName(), root, joinMap);
    //                             if (path != null && path.getJavaType() == String.class) {
    //                                 searchPredicates.add(cb.like(cb.lower(path.as(String.class)), searchPattern));
    //                                 log.debug("Added search predicate for field: {}", column.getFieldName());
    //                             }
    //                         } catch (Exception e) {
    //                             log.debug("Skipping search on field {}: {}", column.getFieldName(), e.getMessage());
    //                         }
    //                     }

    //                     // Add the combined search predicates if any were created
    //                     if (!searchPredicates.isEmpty()) {
    //                         predicates.add(
    //                                 cb.or(searchPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
    //                     }
    //                 }
    //             } catch (Exception e) {
    //                 log.error("Error applying search criteria: {}", e.getMessage());
    //             }
    //         }
    //     } catch (Exception e) {
    //         log.error("Error building predicates: {}", e.getMessage());
    //     }

    //     return predicates;
    // }

    /**
     * Counts the total number of records that match the criteria using the same
     * query structure
     * 
     * @param originalQuery The data query whose structure should be used for
     *                      counting
     * @return The total count of matching records
     */
    private long countTotalRecords(CriteriaQuery<Tuple> originalQuery) {
        try {
            log.debug("Creating count query from original query");
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

            // Get the root from original query and create corresponding root for count query
            Root<?> originalRoot = originalQuery.getRoots().iterator().next();
            Root<?> countRoot = countQuery.from(originalRoot.getJavaType());

            // Log original joins with full details
            Set<?> originalJoins = originalRoot.getJoins();
            log.debug("Original query joins before copying: {}",
                    originalJoins.stream()
                            .filter(j -> j instanceof Join)
                            .map(j -> {
                                Join<?, ?> join = (Join<?, ?>) j;
                                return String.format("%s(%s)",
                                        join.getAttribute().getName(),
                                        join.getJoinType().name());
                            })
                            .collect(Collectors.joining(", ")));

            // Initialize join maps
            Map<String, Join<?, ?>> joinMap = new HashMap<>();
            Set<String> processedJoins = new HashSet<>();

            // Copy all joins from original query including nested joins
            copyJoinsRecursively(originalRoot, countRoot, joinMap, processedJoins);

            // Ensure we count distinct IDs to match native query behavior
            if (originalRoot.getJavaType().getSimpleName().equals("Event")) {
                // For Event entity, count distinct IDs to match native query
                countQuery.select(cb.countDistinct(countRoot.get("id")));
            } else {
                // Default count behavior
                countQuery.select(cb.countDistinct(countRoot));
            }

            // Log copied joins with full details
            Set<?> copiedJoins = countRoot.getJoins();
            log.debug("Count query joins after copying: {}",
                    copiedJoins.stream()
                            .filter(j -> j instanceof Join)
                            .map(j -> {
                                Join<?, ?> join = (Join<?, ?>) j;
                                return String.format("%s(%s)",
                                        join.getAttribute().getName(),
                                        join.getJoinType().name());
                            })
                            .collect(Collectors.joining(", ")));

            // Copy where clause
            jakarta.persistence.criteria.Predicate originalPredicate = originalQuery.getRestriction();
            if (originalPredicate != null) {
                countQuery.where(copyPredicate(originalPredicate, cb, countRoot, joinMap));
            }

            // Copy distinct setting
            countQuery.distinct(originalQuery.isDistinct());

            // Execute count query
            log.debug("Executing count query with following joins: {}", String.join(", ", joinMap.keySet()));
            Long total = entityManager.createQuery(countQuery).getSingleResult();
            log.debug("Count query returned: {}", total);

            return total != null ? total : 0L;
        } catch (Exception e) {
            log.error("Error executing count query: {}", e.getMessage(), e);
            return 0L;
        }
    }

    // private Join<?, ?> copyJoinWithAlias(Join<?, ?> originalJoin, From<?, ?> newParent) {
    //     return newParent.join(
    //             originalJoin.getAttribute().getName(),
    //             originalJoin.getJoinType());
    // }

    /**
     * Recursively copies all joins from the original From object to the new one,
     * preserving the exact join structure and attributes
     */
    private void copyJoinsRecursively(From<?, ?> originalFrom, From<?, ?> newFrom,
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
     * Creates a unique join key that includes the full path to handle nested joins
     */
    private String createJoinKey(Join<?, ?> join) {
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

    // @SuppressWarnings({ "unchecked", "rawtypes" })
    private jakarta.persistence.criteria.Predicate copyPredicate(
            jakarta.persistence.criteria.Predicate original,
            CriteriaBuilder cb,
            Root<?> newRoot,
            Map<String, Join<?, ?>> joinMap) {

        if (original == null) {
            return null;
        }

        // Handle composite predicates (AND/OR)
        if (original.getOperator() != null) {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            for (jakarta.persistence.criteria.Expression<Boolean> expr : original.getExpressions()) {
                if (expr instanceof jakarta.persistence.criteria.Predicate) {
                    predicates.add(copyPredicate((jakarta.persistence.criteria.Predicate) expr, cb, newRoot, joinMap));
                }
            }
            return original.getOperator() == jakarta.persistence.criteria.Predicate.BooleanOperator.AND
                    ? cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]))
                    : cb.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }

        try {
            String predString = original.toString().toLowerCase();

            // Extract the path information from the predicate string
            String[] parts = predString.split("\\s+"); // Split on whitespace
            String pathPart = parts[0]; // The first part should be the path

            // Try to extract full path
            String[] pathElements = pathPart.split("\\.");
            if (pathElements.length > 1) {
                // This is a joined path
                Path<?> currentPath = null;
                StringBuilder currentJoinKey = new StringBuilder();

                // Start with root or find first join
                for (int i = 0; i < pathElements.length - 1; i++) {
                    if (currentJoinKey.length() > 0) {
                        currentJoinKey.append(".");
                    }
                    currentJoinKey.append(pathElements[i]);

                    Join<?, ?> join = joinMap.get(currentJoinKey.toString());
                    if (join != null) {
                        currentPath = join;
                    } else if (i == 0) {
                        currentPath = newRoot.get(pathElements[i]);
                    } else if (currentPath instanceof From) {
                        Join<?, ?> newJoin = ((From<?, ?>) currentPath).join(pathElements[i], JoinType.LEFT);
                        joinMap.put(currentJoinKey.toString(), newJoin);
                        currentPath = newJoin;
                    }
                }

                // Get the final attribute if we found a path
                if (currentPath != null) {
                    String finalAttribute = pathElements[pathElements.length - 1];
                    if (finalAttribute.indexOf(" ") > 0) { // Remove any trailing spaces/operators
                        finalAttribute = finalAttribute.substring(0, finalAttribute.indexOf(" "));
                    }
                    return recreatePredicateWithNewPath(original, cb, currentPath.get(finalAttribute));
                }
            } else {
                // This is a root path
                String attributeName = pathElements[0];
                if (attributeName.indexOf(" ") > 0) {
                    attributeName = attributeName.substring(0, attributeName.indexOf(" "));
                }
                return recreatePredicateWithNewPath(original, cb, newRoot.get(attributeName));
            }
        } catch (Exception e) {
            log.debug("Error copying predicate, will use original: {}", e.getMessage());
        }

        return original;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private jakarta.persistence.criteria.Predicate recreatePredicateWithNewPath(
            jakarta.persistence.criteria.Predicate original,
            CriteriaBuilder cb,
            Path<?> newPath) {
        try {
            String predString = original.toString().toLowerCase();
            Class<?> pathType = newPath.getJavaType();
            Object value = extractValue(predString);

            // Convert value to appropriate type
            Object convertedValue = convertValue(value, pathType);

            // Determine predicate type and create appropriate version
            if (predString.contains(" = ")) {
                return cb.equal(newPath, convertedValue);
            }

            if (predString.contains(" <> ")) {
                return cb.notEqual(newPath, convertedValue);
            }

            // Handle numeric comparisons
            if (Comparable.class.isAssignableFrom(pathType)) {
                Path<Comparable> comparablePath = (Path<Comparable>) newPath;
                Comparable comparableValue = (Comparable) convertedValue;

                if (predString.contains(" > ")) {
                    return cb.greaterThan(comparablePath, comparableValue);
                }

                if (predString.contains(" >= ")) {
                    return cb.greaterThanOrEqualTo(comparablePath, comparableValue);
                }

                if (predString.contains(" < ")) {
                    return cb.lessThan(comparablePath, comparableValue);
                }

                if (predString.contains(" <= ")) {
                    return cb.lessThanOrEqualTo(comparablePath, comparableValue);
                }
            }

            // Handle LIKE predicates for strings
            if (String.class.equals(pathType)) {
                if (predString.contains(" like ")) {
                    return cb.like(newPath.as(String.class), (String) convertedValue);
                }
            }

            // Handle IS NULL predicates
            if (predString.contains(" is null")) {
                return cb.isNull(newPath);
            }

            if (predString.contains(" is not null")) {
                return cb.isNotNull(newPath);
            }

            log.debug("Unsupported predicate type: {}", predString);
            return original;

        } catch (Exception e) {
            log.debug("Failed to recreate predicate with new path: {} - {}", newPath, e.getMessage());
            return original;
        }
    }

    /**
     * Converts a value to the appropriate type based on the path's Java type
     */
    /**
     * Converts a value to the appropriate type with support for various formats
     */
    @SuppressWarnings("unchecked")
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }

        String stringValue = value.toString().trim();

        try {
            // Handle numeric types with format handling
            if (targetType == Long.class || targetType == long.class) {
                stringValue = stringValue.replaceAll("[,_]", ""); // Remove thousands separators
                return Long.parseLong(stringValue);
            }
            if (targetType == Integer.class || targetType == int.class) {
                stringValue = stringValue.replaceAll("[,_]", "");
                return Integer.parseInt(stringValue);
            }
            if (targetType == Double.class || targetType == double.class) {
                stringValue = stringValue.replaceAll("[,_]", "");
                return Double.parseDouble(stringValue);
            }
            if (targetType == Float.class || targetType == float.class) {
                stringValue = stringValue.replaceAll("[,_]", "");
                return Float.parseFloat(stringValue);
            }
            if (targetType == java.math.BigDecimal.class) {
                stringValue = stringValue.replaceAll("[,_]", "");
                return new java.math.BigDecimal(stringValue);
            }

            // Handle boolean with various formats
            if (targetType == Boolean.class || targetType == boolean.class) {
                stringValue = stringValue.toLowerCase();
                if (stringValue.matches("true|yes|1|on"))
                    return true;
                if (stringValue.matches("false|no|0|off"))
                    return false;
                return Boolean.parseBoolean(stringValue);
            }

            // Handle date/time types with multiple formats
            if (targetType == java.time.LocalDateTime.class) {
                try {
                    return java.time.LocalDateTime.parse(stringValue);
                } catch (Exception e) {
                    // Try common date-time formats
                    java.time.format.DateTimeFormatter[] formatters = {
                            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                            java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
                    };
                    for (java.time.format.DateTimeFormatter formatter : formatters) {
                        try {
                            return java.time.LocalDateTime.parse(stringValue, formatter);
                        } catch (Exception ex) {
                            // Continue to next format
                        }
                    }
                    throw new IllegalArgumentException("Could not parse datetime: " + stringValue);
                }
            }

            if (targetType == java.time.LocalDate.class) {
                try {
                    return java.time.LocalDate.parse(stringValue);
                } catch (Exception e) {
                    // Try common date formats
                    java.time.format.DateTimeFormatter[] formatters = {
                            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                            java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    };
                    for (java.time.format.DateTimeFormatter formatter : formatters) {
                        try {
                            return java.time.LocalDate.parse(stringValue, formatter);
                        } catch (Exception ex) {
                            // Continue to next format
                        }
                    }
                    throw new IllegalArgumentException("Could not parse date: " + stringValue);
                }
            }

            // Handle enums with case-insensitive matching
            if (targetType.isEnum()) {
                @SuppressWarnings({ "rawtypes" })
                Class<Enum> enumType = (Class<Enum>) targetType;
                try {
                    return Enum.valueOf(enumType, stringValue.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Try case-insensitive search
                    for (Object enumConstant : targetType.getEnumConstants()) {
                        if (enumConstant.toString().equalsIgnoreCase(stringValue)) {
                            return enumConstant;
                        }
                    }
                    throw new IllegalArgumentException("No enum constant " + targetType.getName() + "." + stringValue);
                }
            }

        } catch (Exception e) {
            log.warn("Failed to convert value '{}' to type {}: {}", stringValue, targetType, e.getMessage());
        }

        return value;
    }

    private Object extractValue(String predString) throws Exception {
        String[] parts = predString.split("[=<>]");
        if (parts.length > 1) {
            String value = parts[1].trim();
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }
            return value;
        }
        throw new IllegalArgumentException("Could not extract value from predicate string");
    }

    private <T extends AbstractStatusAwareEntity<ID>, ID extends Serializable> TableFetchResponse fetchByObjectType(
            TableFetchRequest request) {
        ObjectType objectType = request.getObjectType();
        Pageable pageable = paginationHelper.createPageable(request);

        try {
            // Get the entity class for this object type
            Class<T> entityClass = repositoryFactory.getEntityClass(objectType);

            try {
                // Get the repository for this entity class
                AbstractRepository<T, ID> repository = repositoryFactory.getRepositoryForClass(entityClass);

                // Get the table name for this object type
                String tableName = repositoryFactory.getTableNameForObjectType(objectType);

                // Fetch the entities using the generic method
                return fetchEntities(
                        request,
                        pageable,
                        repository,
                        tableName,
                        this::createEntitySpecification,
                        entityConverter::convertEntityToTableRow,
                        () -> columnInfoProvider.getColumnInfo(objectType, request));
            } catch (IllegalArgumentException e) {
                log.error("Error getting repository for entity class {}: {}", entityClass.getName(), e.getMessage());
                return responseBuilder.createErrorResponse("Error getting repository: " + entityClass.getName());
            }
        } catch (IllegalArgumentException e) {
            log.error("Error getting entity class for object type {}: {}", objectType, e.getMessage());
            return responseBuilder.createErrorResponse("Error getting entity class for object type: " + objectType);
        }
    }

    private <T extends AbstractStatusAwareEntity<ID>, ID extends Serializable> TableFetchResponse fetchEntities(
            TableFetchRequest request,
            Pageable pageable,
            AbstractRepository<T, ID> repository,
            String tableName,
            Function<TableFetchRequest, Specification<T>> specificationBuilder,
            Function<T, TableRow> rowConverter,
            Supplier<Map<String, ColumnInfo>> columnInfoProvider) {

        try {
            // Create specification for filtering
            Specification<T> spec = specificationBuilder.apply(request);

            // Execute the query using JpaSpecificationExecutor
            Page<T> page;
            if (repository instanceof JpaSpecificationExecutor) {
                JpaSpecificationExecutor<T> specExecutor = (JpaSpecificationExecutor<T>) repository;
                page = specExecutor.findAll(spec, pageable);
            } else {
                log.warn("Repository does not support specifications, using basic pagination");
                page = repository.findAll(pageable);
            }

            // Convert to response format
            List<TableRow> rows = page.getContent().stream()
                    .map(entity -> {
                        TableRow row = rowConverter.apply(entity);
                        // Filter row properties if needed
                        if (request.getViewColumns() != null && !request.getViewColumns().isEmpty()) {
                            entityConverter.filterRowByViewColumns(row, request.getViewColumns());
                        }
                        return row;
                    })
                    .collect(Collectors.toList());

            // Build and return response
            return responseBuilder.buildEntityResponse(request, rows, page, tableName, columnInfoProvider.get());

        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            return responseBuilder.createErrorResponse("Error executing query: " + e.getMessage());
        }
    }

    private <T> Specification<T> createEntitySpecification(TableFetchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Add default status filter
            predicateBuilder.addDefaultFilters(criteriaBuilder, root, predicates);

            // Apply filters from the request
            predicateBuilder.applyFilters(request, predicates, criteriaBuilder, root);

            // Apply search criteria
            predicateBuilder.applySearch(request, predicates, criteriaBuilder, root);

            // Return combined predicates
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    // @SuppressWarnings("unchecked")
    // private List<Tuple> convertResultsToTuples(List<?> results, Class<?> entityClass) {
    //     if (!results.isEmpty() && results.get(0) instanceof Tuple) {
    //         return (List<Tuple>) results;
    //     }

    //     if (!results.isEmpty() && results.get(0) instanceof Object[]) {
    //         return convertNativeResultsToTuples((List<Object[]>) results, entityClass);
    //     }

    //     List<Tuple> tuples = new ArrayList<>();
    //     // ... conversion logic for entity objects

    //     return tuples;
    // }

    // private List<Tuple> convertNativeResultsToTuples(List<Object[]> nativeResults, Class<?> entityClass) {
    //     List<Tuple> tuples = new ArrayList<>();
    //     jakarta.persistence.metamodel.EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);

    //     for (Object[] row : nativeResults) {
    //         final Object[] currentRow = row;
    //         Tuple tuple = new jakarta.persistence.Tuple() {
    //             @Override
    //             public <X> X get(TupleElement<X> tupleElement) {
    //                 return get(tupleElement.getAlias(), tupleElement.getJavaType());
    //             }

    //             @Override
    //             public Object get(String alias) {
    //                 if ("id".equals(alias))
    //                     return currentRow[0];
    //                 if ("code".equals(alias))
    //                     return currentRow[1];
    //                 if ("createdAt".equals(alias))
    //                     return currentRow[2];
    //                 if ("createdBy".equals(alias))
    //                     return currentRow[3];
    //                 if ("description".equals(alias))
    //                     return currentRow[4];
    //                 if ("endTime".equals(alias))
    //                     return currentRow[5];
    //                 if ("name".equals(alias))
    //                     return currentRow[6];
    //                 if ("startTime".equals(alias))
    //                     return currentRow[7];
    //                 if ("status".equals(alias))
    //                     return currentRow[8];
    //                 if ("updatedAt".equals(alias))
    //                     return currentRow[9];
    //                 if ("updatedBy".equals(alias))
    //                     return currentRow[10];
    //                 if ("version".equals(alias))
    //                     return currentRow[11];
    //                 return null;
    //             }

    //             @Override
    //             public <X> X get(String alias, Class<X> type) {
    //                 Object value = get(alias);
    //                 if (value == null)
    //                     return null;
    //                 return type.cast(value);
    //             }

    //             @Override
    //             public <X> X get(int i, Class<X> type) {
    //                 if (i >= 0 && i < currentRow.length) {
    //                     return type.cast(currentRow[i]);
    //                 }
    //                 return null;
    //             }

    //             @Override
    //             public Object get(int i) {
    //                 if (i >= 0 && i < currentRow.length) {
    //                     return currentRow[i];
    //                 }
    //                 return null;
    //             }

    //             @Override
    //             public Object[] toArray() {
    //                 return currentRow;
    //             }

    //             @Override
    //             public List<TupleElement<?>> getElements() {
    //                 return List.of(
    //                         new TupleElementImpl<>("id", Long.class),
    //                         new TupleElementImpl<>("name", String.class),
    //                         new TupleElementImpl<>("description", String.class),
    //                         new TupleElementImpl<>("startTime", java.time.LocalDateTime.class),
    //                         new TupleElementImpl<>("endTime", java.time.LocalDateTime.class),
    //                         new TupleElementImpl<>("status", String.class));
    //             }
    //         };
    //         tuples.add(tuple);
    //     }

    //     return tuples;
    // }

    // /**
    //  * Attempts to find an attribute in a ManagedType, handling common naming
    //  * variations
    //  */
    // private jakarta.persistence.metamodel.Attribute<?, ?> findAttribute(
    //         jakarta.persistence.metamodel.ManagedType<?> type, String attributeName) {
    //     try {
    //         // Try exact match first
    //         return type.getAttribute(attributeName);
    //     } catch (IllegalArgumentException e) {
    //         // Try various name variations
    //         List<String> variations = new ArrayList<>();

    //         // Remove plural 's'
    //         if (attributeName.endsWith("s")) {
    //             variations.add(attributeName.substring(0, attributeName.length() - 1));
    //         }

    //         // Convert camelCase to snake_case
    //         if (attributeName.matches(".*[A-Z].*")) {
    //             String snakeCase = attributeName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    //             variations.add(snakeCase);
    //         }

    //         // Convert snake_case to camelCase
    //         if (attributeName.contains("_")) {
    //             StringBuilder camelCase = new StringBuilder();
    //             boolean capitalizeNext = false;

    //             for (char c : attributeName.toCharArray()) {
    //                 if (c == '_') {
    //                     capitalizeNext = true;
    //                 } else if (capitalizeNext) {
    //                     camelCase.append(Character.toUpperCase(c));
    //                     capitalizeNext = false;
    //                 } else {
    //                     camelCase.append(c);
    //                 }
    //             }
    //             variations.add(camelCase.toString());
    //         }

    //         // Try all variations
    //         for (String variation : variations) {
    //             try {
    //                 jakarta.persistence.metamodel.Attribute<?, ?> attr = type.getAttribute(variation);
    //                 log.debug("Found attribute {} using variation {}", attributeName, variation);
    //                 return attr;
    //             } catch (IllegalArgumentException ex) {
    //                 // Continue to next variation
    //             }
    //         }

    //         log.debug("Could not find attribute {} in any variation", attributeName);
    //         return null;
    //     }
    // }

    /**
     * Recursively logs all joins in the query structure
     */
    private void logJoinsRecursively(From<?, ?> from, String indent, Map<String, Object> search) {
        log.debug("{}Query root class: {}", indent, from.getJavaType().getSimpleName());
        log.debug("{}Expected joins from search criteria: {}", indent,
                Optional.ofNullable(search)
                        .map(Map::size)
                        .orElse(0));

        Set<String> processedJoins = new HashSet<>();
        Set<?> joinSet = from.getJoins();
        if (joinSet == null || joinSet.isEmpty()) {
            log.debug("{}No joins found", indent);
            return;
        }

        for (Object obj : joinSet) {
            if (!(obj instanceof Join)) {
                continue;
            }
            Join<?, ?> join = (Join<?, ?>) obj;
            String joinPath = createJoinKey(join);

            // Skip if already processed to avoid duplicates
            if (!processedJoins.add(joinPath)) {
                log.debug("{}Skipping duplicate join: {}", indent, joinPath);
                continue;
            }

            log.debug("{}Join: {} ({}) -> {} [path: {}]",
                    indent,
                    join.getAttribute().getName(),
                    join.getJoinType(),
                    join.getJavaType().getSimpleName(),
                    joinPath);

            // Recursively log nested joins with increased indentation
            logJoinsRecursively(join, indent + "  ", search);
        }
    }

    // /**
    //  * Gets the JPA relationship path between two entity types
    //  */
    // private String getRelationshipPath(Class<?> sourceClass, ObjectType targetType) {
    //     try {
    //         // Get target entity class
    //         Class<?> targetClass = repositoryFactory.getEntityClass(targetType);
    //         if (targetClass == null) {
    //             return null;
    //         }

    //         // Handle direct relationships based on entity model from test
    //         if (sourceClass.getSimpleName().equals("Event")) {
    //             switch (targetType) {
    //                 case EventLocation:
    //                     return "locations";
    //                 case ParticipantEvent:
    //                     return "participantEvents";
    //                 case Participant:
    //                     return "locations.participants";
    //                 case Region:
    //                     return "locations.region";
    //                 case Province:
    //                     return "locations.region.provinces";
    //                 default:
    //                     break;
    //             }
    //         }

    //         // Try to find relationship through metamodel
    //         jakarta.persistence.metamodel.EntityType<?> entityType = entityManager.getMetamodel().entity(sourceClass);

    //         // Look for direct relationship
    //         for (jakarta.persistence.metamodel.Attribute<?, ?> attr : entityType.getAttributes()) {
    //             if (attr.isCollection()) {
    //                 jakarta.persistence.metamodel.PluralAttribute<?, ?, ?> pluralAttr = (jakarta.persistence.metamodel.PluralAttribute<?, ?, ?>) attr;
    //                 if (pluralAttr.getElementType().getJavaType().equals(targetClass)) {
    //                     return attr.getName();
    //                 }
    //             } else if (attr.isAssociation() && attr.getJavaType().equals(targetClass)) {
    //                 return attr.getName();
    //             }
    //         }

    //         log.debug("No direct relationship found from {} to {}",
    //                 sourceClass.getSimpleName(), targetClass.getSimpleName());
    //         return null;

    //     } catch (Exception e) {
    //         log.error("Error finding relationship path: {}", e.getMessage());
    //         return null;
    //     }
    // }

    // private static class TupleElementImpl<X> implements jakarta.persistence.TupleElement<X> {
    //     private final String alias;
    //     private final Class<X> javaType;

    //     public TupleElementImpl(String alias, Class<X> javaType) {
    //         this.alias = alias;
    //         this.javaType = javaType;
    //     }

    //     @Override
    //     public String getAlias() {
    //         return alias;
    //     }

    //     @Override
    //     public Class<X> getJavaType() {
    //         return javaType;
    //     }
    // }
}
