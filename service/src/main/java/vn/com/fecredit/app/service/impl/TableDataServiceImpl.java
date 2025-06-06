package vn.com.fecredit.app.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
// Keep other imports
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
// TypedQuery no longer needed
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
import vn.com.fecredit.app.service.impl.table.EntityManager;
import vn.com.fecredit.app.service.impl.table.PaginationHelper;
import vn.com.fecredit.app.service.impl.table.PredicateManager;
import vn.com.fecredit.app.service.impl.table.QueryManager;
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
public class TableDataServiceImpl implements TableDataService {    @PersistenceContext
    private jakarta.persistence.EntityManager entityManager;    
    private final RepositoryFactory repositoryFactory;
    private final EntityManager customEntityManager;
    private final PredicateManager predicateManager;    private final ResponseBuilder responseBuilder;
    private final PaginationHelper paginationHelper;
    private final ColumnInfoProvider columnInfoProvider;
    private final QueryManager queryManager;

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
    }    @Override
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
            }                // Find root entity class - this validates the object type is mappable to an entity
            Class<?> rootEntityClass;
            try {
                rootEntityClass = customEntityManager.findEntityClass(null, objectType);
                if (rootEntityClass == null) {
                    return responseBuilder.createErrorResponse("Unsupported entity for object type: " + objectType);
                }
            } catch (Exception e) {
                return responseBuilder.createErrorResponse("Unsupported entity: " + objectType);
            }

            // Create query
            CriteriaQuery<Tuple> query = queryManager.buildCriteriaQuery(request, rootEntityClass);

            // If query is null, create a simple default query to retrieve just IDs
            if (query == null) {
                log.warn("Failed to build query with provided parameters, creating simple default query");
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                query = cb.createTupleQuery();
                Root<?> root = query.from(rootEntityClass);
                query.multiselect(root.get("id").alias("id"));
                query.distinct(true);
            }

            try {
                // Get total count - pass the query instead of the request
                long totalCount = queryManager.countTotalRecords(query);

                // Get paginated results (even if empty)
                List<Tuple> results = queryManager.executeQueryWithPagination(query, request);

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
                        customEntityManager.getTableName(rootEntityClass));
            } catch (Exception e) {
                log.error("Error executing query: {}", e.getMessage(), e);
                return responseBuilder.createErrorResponse("Error executing query: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error in fetchScalarProperties", e);
            return responseBuilder.createErrorResponse("Error fetching scalar properties: " + e.getMessage());
        }    }
    // Removed unused methods

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
    }    private <T extends AbstractStatusAwareEntity<ID>, ID extends Serializable> TableFetchResponse fetchByObjectType(
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
                String tableName = repositoryFactory.getTableNameForObjectType(objectType);                // Fetch the entities using the generic method
                return fetchEntities(
                        request,
                        pageable,
                        repository,
                        tableName,
                        this::createEntitySpecification,                        // Use the new EntityManager to convert entities to rows
                        entity -> customEntityManager.convertEntityToTableRow(entity, 
                            new java.util.ArrayList<>(columnInfoProvider.getColumnInfo(objectType, request).values())),
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
            }            // Convert to response format
            List<TableRow> rows = page.getContent().stream()
                    .map(rowConverter::apply)
                    .collect(Collectors.toList());

            // Build and return response
            return responseBuilder.buildEntityResponse(request, rows, page, tableName, columnInfoProvider.get());

        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            return responseBuilder.createErrorResponse("Error executing query: " + e.getMessage());
        }
    }    private <T> Specification<T> createEntitySpecification(TableFetchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Add default status filter
            predicateManager.addDefaultFilters(criteriaBuilder, root, predicates);

            // Apply filters from the request
            predicateManager.applyFilters(request, predicates, criteriaBuilder, root);

            // Apply search criteria
            predicateManager.applySearch(request, predicates, criteriaBuilder, root);

            // Return combined predicates
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };    }
}
