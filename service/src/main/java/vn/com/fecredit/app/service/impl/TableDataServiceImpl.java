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
    private final ResponseBuilder responseBuilder;
    private final PaginationHelper paginationHelper;
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
                rootEntityClass = entityFinder.findEntityClass(null, objectType);
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
                        Join<?, ?> newJoin = ((From<?, ?>) currentPath).join(pathElements[i], JoinType.INNER);
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
}
