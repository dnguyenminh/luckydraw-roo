package vn.com.fecredit.app.service.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.repository.SimpleObjectRepository;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.FieldType;
import vn.com.fecredit.app.service.dto.FilterRequest;
import vn.com.fecredit.app.service.dto.FilterType;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.SortRequest;
import vn.com.fecredit.app.service.dto.SortType;
import vn.com.fecredit.app.service.dto.TabTableRow;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.factory.RelatedTablesFactory;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

/**
 * Implementation of the TableDataService for fetching paginated table data.
 * Supports dynamic entity fetching, sorting, filtering and pagination.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableDataServiceImpl implements TableDataService {

    @PersistenceContext
    private EntityManager entityManager;

    private final RepositoryFactory repositoryFactory;

    private final RelatedTablesFactory relatedTablesFactory;

    @Override
    public TableFetchResponse fetchData(TableFetchRequest request) {
        if (request == null) {
            return createErrorResponse("Request cannot be null");
        }

        try {
            // First try to use ObjectType if provided
            if (request.getObjectType() != null) {
                return fetchByObjectType(request);
            }
            // Fall back to entityName for backward compatibility
            else if (request.getEntityName() != null) {
                try {
                    // Try to map entity name to ObjectType enum
                    ObjectType objectType = ObjectType.valueOf(request.getEntityName());
                    request.setObjectType(objectType);
                    return fetchByObjectType(request);
                } catch (IllegalArgumentException e) {
                    // Entity name doesn't match any predefined object type
                    return createErrorResponse("Unsupported entity: " + request.getEntityName());
                }
            } else {
                return createErrorResponse("No object type or entity name specified");
            }
        } catch (Exception e) {
            log.error("Error fetching table data", e);
            return createErrorResponse("Error fetching data: " + e.getMessage());
        }
    }

    /**
     * Fetch data based on ObjectType
     */
    private <T extends AbstractStatusAwareEntity> TableFetchResponse fetchByObjectType(TableFetchRequest request) {
        ObjectType objectType = request.getObjectType();
        Pageable pageable = createPageable(request);

        try {
            // Get the entity class for this object type
            Class<T> entityClass = repositoryFactory.getEntityClass(objectType);

            // Get the repository for this entity class
            SimpleObjectRepository<T> repository = repositoryFactory.getRepositoryForClass(entityClass);

            // Get the table name for this object type
            String tableName = repositoryFactory.getTableNameForObjectType(objectType);

            // Fetch the entities using the generic method
            return fetchEntities(
                    request,
                    pageable,
                    repository,
                    tableName,
                    this::createEntitySpecification,
                    this::convertEntityToTableRow,
                    () -> getColumnInfo(objectType));
        } catch (IllegalArgumentException e) {
            log.error("Error fetching data for object type {}: {}", objectType, e.getMessage());
            return createErrorResponse("Unsupported object type: " + objectType);
        }
    }

    /**
     * Get column info based on object type
     */
    private Map<String, ColumnInfo> getColumnInfo(ObjectType objectType) {
        switch (objectType) {
            case User:
                return getUserColumnInfo();
            case Event:
                return getEventColumnInfo();
            // Add cases for other entity types
            default:
                log.warn("No column info defined for object type: {}", objectType);
                return new HashMap<>();
        }
    }

    /**
     * Generic method to fetch entities and create a response
     */
    private <T extends AbstractStatusAwareEntity> TableFetchResponse fetchEntities(
            TableFetchRequest request,
            Pageable pageable,
            SimpleObjectRepository<T> repository,
            String tableName,
            Function<TableFetchRequest, Specification<T>> specificationBuilder,
            Function<T, TableRow> rowConverter,
            Supplier<Map<String, ColumnInfo>> columnInfoProvider) {

        Page<T> page;
        try {
            // Create specification for filtering
            Specification<T> spec = specificationBuilder.apply(request);
            
            // Execute the query using JpaSpecificationExecutor
            if (repository instanceof JpaSpecificationExecutor) {
                JpaSpecificationExecutor<T> specExecutor = (JpaSpecificationExecutor<T>) repository;
                page = specExecutor.findAll(spec, pageable);
            } else {
                // Fallback to basic pagination without specifications
                log.warn("Repository does not support specifications, using basic pagination without filtering");
                page = repository.findAll(pageable);
            }
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            return createErrorResponse("Error executing query: " + e.getMessage());
        }

        // Convert to response format
        List<TableRow> rows = page.getContent().stream()
                .map(rowConverter)
                .collect(Collectors.toList());

        // Create response
        TableFetchResponse response = new TableFetchResponse();
        response.setStatus(page.isEmpty() ? FetchStatus.NO_DATA : FetchStatus.SUCCESS);
        response.setTotalPage(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTableName(tableName);
        response.setOriginalRequest(request);
        response.setRows(rows);

        // Add column metadata
        response.setFieldNameMap(columnInfoProvider.get());

        return response;
    }

    /**
     * Create a generic specification for entity filtering
     */
    private <T> Specification<T> createEntitySpecification(TableFetchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply filters
            applyFilters(request, predicates, criteriaBuilder, root);

            // Apply search
            applySearch(request, predicates, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Apply filters from the request to the specification
     */
    private <T> void applyFilters(
            TableFetchRequest request,
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Root<T> root) {

        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            for (FilterRequest filter : request.getFilters()) {
                String field = filter.getField();
                FilterType filterType = filter.getFilterType();

                if (field != null && filterType != null) {
                    addPredicateForField(predicates, cb, root, field, filterType,
                            filter.getMinValue(), filter.getMaxValue());
                }
            }
        }
    }

    /**
     * Apply search parameters from the request to the specification
     */
    private <T> void applySearch(
            TableFetchRequest request,
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Root<T> root) {

        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            for (Map.Entry<String, String> entry : request.getSearch().entrySet()) {
                String field = entry.getKey();
                String value = entry.getValue();

                if (field != null && value != null && !value.isEmpty()) {
                    predicates.add(cb.like(
                            cb.lower(root.get(field)),
                            "%" + value.toLowerCase() + "%"));
                }
            }
        }
    }

    /**
     * Add a predicate based on field and filter type
     */
    private <T> void addPredicateForField(List<Predicate> predicates,
            CriteriaBuilder cb,
            Root<T> root,
            String field,
            FilterType filterType,
            String minValue,
            String maxValue) {
        switch (filterType) {
            case EQUALS:
                if (minValue != null) {
                    predicates.add(cb.equal(root.get(field), minValue));
                }
                break;
            case NOT_EQUALS: // Fixed - was missing case label
                if (minValue != null) {
                    predicates.add(cb.notEqual(root.get(field), minValue));
                }
                break;
            case LESS_THAN:
                if (minValue != null) {
                    predicates.add(cb.lessThan(root.get(field).as(String.class), minValue));
                }
                break;
            case LESS_THAN_OR_EQUALS:
                if (minValue != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(field).as(String.class), minValue));
                }
                break;
            case GREATER_THAN:
                if (minValue != null) {
                    predicates.add(cb.greaterThan(root.get(field).as(String.class), minValue));
                }
                break;
            case GREATER_THAN_OR_EQUALS:
                if (minValue != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get(field).as(String.class), minValue));
                }
                break;
            case BETWEEN:
                if (minValue != null && maxValue != null) {
                    predicates.add(cb.between(root.get(field).as(String.class), minValue, maxValue));
                }
                break;
            case IN:
                if (minValue != null && minValue.contains(",")) {
                    predicates.add(root.get(field).in((Object[]) minValue.split(",")));
                }
                break;
            case NOT_IN:
                if (minValue != null && minValue.contains(",")) {
                    predicates.add(cb.not(root.get(field).in((Object[]) minValue.split(","))));
                }
                break;
            default:
                // Use case-insensitive LIKE as default
                if (minValue != null) {
                    predicates.add(cb.like(
                            cb.lower(root.get(field)),
                            "%" + minValue.toLowerCase() + "%"));
                }
                break;
        }
    }

    /**
     * Create a TableRow from any entity using reflection
     * 
     * @param entity the entity to convert
     * @return TableRow containing the entity's properties
     */
    private <T> TableRow convertEntityToTableRow(T entity) {
        Map<String, Object> data = new HashMap<>();

        if (entity != null) {
            log.debug("Processing entity of type: {}", entity.getClass().getName());
            // Get all methods from the entity class
            Method[] methods = entity.getClass().getMethods();

            for (Method method : methods) {
                String methodName = method.getName();
                // Only process getter methods (except getClass())
                if (methodName.startsWith("get") &&
                        !methodName.equals("getClass") &&
                        method.getParameterCount() == 0) {

                    try {
                        // Extract property name from getter method (remove "get" and lowercase first
                        // character)
                        String propertyName = methodName.substring(3);
                        propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);

                        // Invoke the getter method to get the value
                        Object value = method.invoke(entity);
                        log.debug("Extracted property: {} with value: {}", propertyName, value);

                        // Add property and its value to the data map
                        data.put(propertyName, value);
                    } catch (Exception e) {
                        log.warn("Failed to extract property via method {}: {}", methodName, e.getMessage());
                    }
                }
                // Also handle is/has methods for booleans
                else if ((methodName.startsWith("is") || methodName.startsWith("has")) &&
                        method.getParameterCount() == 0 &&
                        (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                    try {
                        // Extract property name (remove "is"/"has" and lowercase first character)
                        String propertyName = methodName.startsWith("is") ? methodName.substring(2)
                                : methodName.substring(3);
                        propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);

                        // Invoke the method to get the boolean value
                        Object value = method.invoke(entity);
                        log.debug("Extracted boolean property: {} with value: {}", propertyName, value);

                        // Add property and its value to the data map
                        data.put(propertyName, value);
                    } catch (Exception e) {
                        log.warn("Failed to extract boolean property via method {}: {}", methodName, e.getMessage());
                    }
                }
            }
        }

        // Check if the entity has related tables to determine the type of row to return
        if (relatedTablesFactory.hasRelatedTables(entity)) {
            TabTableRow tabRow = new TabTableRow(data);

            // Add related tables from the factory
            List<String> relatedTables = relatedTablesFactory.getRelatedTables(entity);
            for (String tableName : relatedTables) {
                tabRow.addRelatedTable(tableName);
            }

            return tabRow;
        } else {
            // Create the regular TableRow with the extracted data
            TableRow row = new TableRow();
            row.setData(data);
            return row;
        }
    }

    /**
     * Define column metadata for User entities
     */
    private Map<String, ColumnInfo> getUserColumnInfo() {
        Map<String, ColumnInfo> columnInfo = new HashMap<>();
        columnInfo.put("id", new ColumnInfo("id", FieldType.NUMBER.name(), SortType.ASCENDING));
        columnInfo.put("username", new ColumnInfo("username", FieldType.STRING.name(), SortType.ASCENDING));
        columnInfo.put("email", new ColumnInfo("email", FieldType.STRING.name(), SortType.ASCENDING));
        columnInfo.put("fullName", new ColumnInfo("fullName", FieldType.STRING.name(), SortType.ASCENDING));
        columnInfo.put("role", new ColumnInfo("role", FieldType.STRING.name(), SortType.NONE));
        columnInfo.put("enabled", new ColumnInfo("enabled", FieldType.BOOLEAN.name(), SortType.NONE));
        columnInfo.put("status", new ColumnInfo("status", FieldType.STRING.name(), SortType.NONE));
        return columnInfo;
    }

    /**
     * Define column metadata for Event entities
     */
    private Map<String, ColumnInfo> getEventColumnInfo() {
        Map<String, ColumnInfo> columnInfo = new HashMap<>();
        columnInfo.put("id", new ColumnInfo("id", FieldType.NUMBER.name(), SortType.ASCENDING));
        columnInfo.put("name", new ColumnInfo("name", FieldType.STRING.name(), SortType.ASCENDING));
        columnInfo.put("code", new ColumnInfo("code", FieldType.STRING.name(), SortType.ASCENDING));
        columnInfo.put("description", new ColumnInfo("description", FieldType.STRING.name(), SortType.NONE));
        columnInfo.put("startTime", new ColumnInfo("startTime", FieldType.DATETIME.name(), SortType.ASCENDING));
        columnInfo.put("endTime", new ColumnInfo("endTime", FieldType.DATETIME.name(), SortType.ASCENDING));
        columnInfo.put("status", new ColumnInfo("status", FieldType.STRING.name(), SortType.NONE));
        return columnInfo;
    }

    /**
     * Create a pageable object from the request for sorting and pagination
     */
    private Pageable createPageable(TableFetchRequest request) {
        List<Order> orders = new ArrayList<>();

        // Process sort requests
        if (request.getSorts() != null && !request.getSorts().isEmpty()) {
            for (SortRequest sortRequest : request.getSorts()) {
                Direction direction = Direction.ASC;
                if (sortRequest.getSortType() == SortType.DESCENDING) {
                    direction = Direction.DESC;
                }
                orders.add(new Order(direction, sortRequest.getField()));
            }
        }

        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /**
     * Create an error response with message
     */
    private TableFetchResponse createErrorResponse(String message) {
        log.error("Table data fetch error: {}", message);

        TableFetchResponse response = new TableFetchResponse();
        response.setStatus(FetchStatus.ERROR);
        response.setMessage(message);
        response.setTotalPage(0);
        response.setCurrentPage(0);
        response.setPageSize(0);
        response.setTotalElements(0L);
        response.setRows(new ArrayList<>());
        return response;
    }

    /**
     * Functional interface for providing column info
     */
    @FunctionalInterface
    private interface Supplier<T> {
        T get();
    }
}