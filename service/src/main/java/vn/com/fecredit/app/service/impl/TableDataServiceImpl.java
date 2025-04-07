package vn.com.fecredit.app.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.repository.SimpleObjectRepository;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.DataObjectKey;
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
import vn.com.fecredit.app.service.validator.TableFetchRequestValidator;

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

    private final TableFetchRequestValidator validator;

    // Add a cache for column info to improve performance
    private final Map<Class<?>, Map<String, ColumnInfo>> columnInfoCache = new ConcurrentHashMap<>();

    @Override
    public TableFetchResponse fetchData(TableFetchRequest request) {
        if (request == null) {
            return createErrorResponse("Request cannot be null");
        }

        // Add null check for validator
        if (validator != null) {
            // Validate the request
            Errors errors = new BeanPropertyBindingResult(request, "tableFetchRequest");
            validator.validate(request, errors);

            if (errors.hasErrors()) {
                return createErrorResponse("Invalid request: " + errors.getAllErrors().get(0).getDefaultMessage());
            }
        } else {
            log.warn("TableFetchRequestValidator is null, skipping validation");
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends AbstractStatusAwareEntity> TableFetchResponse fetchByObjectType(TableFetchRequest request) {
        ObjectType objectType = request.getObjectType();
        log.info("Fetching data for object type: {}", objectType);

        // Create the pageable object for pagination
        Pageable pageable = createPageable(request);

        try {
            // Step 1: Get the entity class for this object type
            Class<T> entityClass;
            try {
                entityClass = repositoryFactory.getEntityClass(objectType);
            } catch (Exception e) {
                log.error("Error getting entity class for object type {}: {}", objectType, e.getMessage());
                return createErrorResponse("Error getting entity class for object type: " + objectType);
            }

            if (entityClass == null) {
                log.error("Entity class not found for object type: {}", objectType);
                return createErrorResponse("Entity class not found for object type: " + objectType);
            }
            log.debug("Found entity class: {}", entityClass.getName());

            // Step 2: Get the repository for this entity class
            SimpleObjectRepository<T> repository;
            try {
                repository = repositoryFactory.getRepositoryForClass(entityClass);
            } catch (Exception e) {
                log.error("Error getting repository for entity class {}: {}", entityClass.getName(), e.getMessage());
                return createErrorResponse("Error getting repository for entity class: " + entityClass.getName());
            }

            if (repository == null) {
                log.error("Repository not found for entity class: {}", entityClass.getName());
                return createErrorResponse("Repository not found for entity class: " + entityClass.getName());
            }
            log.debug("Found repository: {}", repository.getClass().getName());

            // Step 3: Get the table name for this object type
            String tableName;
            try {
                tableName = repositoryFactory.getTableNameForObjectType(objectType);
            } catch (Exception e) {
                log.error("Error getting table name for object type {}: {}", objectType, e.getMessage());
                return createErrorResponse("Error getting table name for object type: " + objectType);
            }

            if (tableName == null) {
                log.error("Table name not found for object type: {}", objectType);
                return createErrorResponse("Table name not found for object type: " + objectType);
            }
            log.debug("Found table name: {}", tableName);

            // Also in Step 3: Discover relationships from the entity class
            Map<String, Class<?>> relationshipMap = new HashMap<>();
            try {
                // Get all declared fields
                for (Field field : getAllFields(entityClass)) {
                    // Check if field represents a relationship (ManyToOne, OneToMany, etc.)
                    if (isRelationshipField(field)) {
                        Class<?> relatedType = determineRelatedEntityType(field);
                        if (relatedType != null) {
                            relationshipMap.put(field.getName(), relatedType);
                            log.debug("Found relationship: {} -> {}", field.getName(), relatedType.getSimpleName());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error discovering relationships: {}", e.getMessage());
                // Continue without relationships if there's an error
            }

            // Step 4: Create specification for the entity query
            Specification<T> spec = createEntitySpecification(request);

            // Step 5: Execute the query with pagination
            Page<T> page;
            try {
                page = repository.findAll(spec, pageable);
                log.debug("Query executed, found {} results", page.getTotalElements());
            } catch (Exception e) {
                log.error("Error executing query: {}", e.getMessage());
                return createErrorResponse("Error executing query: " + e.getMessage());
            }

            // Handle empty results
            if (page.isEmpty()) {
                log.info("No data found for query");
                return TableFetchResponse.builder()
                        .status(FetchStatus.NO_DATA)
                        .message("No data found")
                        .tableName(tableName)
                        .originalRequest(request)
                        .totalElements(0L)
                        .rows(List.of())
                        .fieldNameMap(getColumnInfo(objectType))
                        .build();
            }

            // Step 6: Convert entities to table rows for the response
            List<TableRow> rows = new ArrayList<>();
            for (T entity : page.getContent()) {
                TableRow row;
                if (relatedTablesFactory.hasRelatedTables(entity)) {
                    // Create TabTableRow for entities with related tables
                    TabTableRow tabRow = new TabTableRow(convertEntityToMap(entity));
                    List<String> relatedTables = relatedTablesFactory.getRelatedTables(entity);
                    tabRow.setRelatedTables(relatedTables);
                    row = tabRow;
                } else {
                    // Create standard TableRow for entities without related tables
                    row = convertEntityToTableRow(entity);
                }
                rows.add(row);
            }

            // Step 7: Build and return the response
            TableFetchResponse response = TableFetchResponse.builder()
                    .status(FetchStatus.SUCCESS)
                    .tableName(tableName)
                    .totalElements(page.getTotalElements())
                    .totalPage(page.getTotalPages())
                    .currentPage(page.getNumber())
                    .pageSize(page.getSize())
                    .originalRequest(request)
                    .rows(rows)
                    .fieldNameMap(getColumnInfo(objectType))
                    .relatedLinkedObjects(request.getSearch()) // Now using string keys
                    .build();

            return response;
        } catch (Exception e) {
            log.error("Error fetching data for object type {}: {}", objectType, e.getMessage(), e);
            return createErrorResponse(
                    "Error fetching data for object type: " + objectType + ". Reason: " + e.getMessage());
        }
    }

    /**
     * Determine the related entity type from a relationship field
     */
    private Class<?> determineRelatedEntityType(Field field) {
        try {
            if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                // For direct relationships, use the field type
                return field.getType();
            } else if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
                // For collection relationships, extract the generic type parameter
                ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                return (Class<?>) paramType.getActualTypeArguments()[0];
            }
        } catch (Exception e) {
            log.debug("Error determining related entity type for field {}: {}", field.getName(), e.getMessage());
        }
        return null;
    }

    /**
     * Get column info based on object type
     */
    private Map<String, ColumnInfo> getColumnInfo(ObjectType objectType) {
        try {
            // Get the entity class for this object type
            Class<?> entityClass = repositoryFactory.getEntityClass(objectType);

            // Check if we already have cached column info for this entity class
            Map<String, ColumnInfo> cachedInfo = columnInfoCache.get(entityClass);
            if (cachedInfo != null) {
                log.debug("Using cached column info for entity class: {}", entityClass.getName());
                return cachedInfo;
            }

            // Generate column info dynamically based on entity class structure
            Map<String, ColumnInfo> columnInfo = generateColumnInfoFromEntityClass(entityClass);

            // Cache the generated column info for future use
            columnInfoCache.put(entityClass, columnInfo);

            return columnInfo;
        } catch (Exception e) {
            log.warn("Failed to get dynamic column info for object type: {}. Error: {}", objectType, e.getMessage());
            log.debug("Exception details:", e);

            // Just return an empty map as fallback
            return new HashMap<>();
        }
    }

    /**
     * Dynamically generate column information from an entity class using reflection
     *
     * @param entityClass the JPA entity class to analyze
     * @return map of field names to column metadata
     */
    private Map<String, ColumnInfo> generateColumnInfoFromEntityClass(Class<?> entityClass) {
        Map<String, ColumnInfo> columnInfo = new HashMap<>();

        if (entityClass == null) {
            return columnInfo;
        }

        log.debug("Generating column info for entity class: {}", entityClass.getName());

        // Get all declared fields from the class and its superclasses
        List<Field> allFields = getAllFields(entityClass);

        // Process each field
        for (Field field : allFields) {
            // Skip static, transient, and synthetic fields
            if (Modifier.isStatic(field.getModifiers()) ||
                    Modifier.isTransient(field.getModifiers()) ||
                    field.isSynthetic()) {
                continue;
            }

            // Skip fields annotated with relationship annotations
            if (isRelationshipField(field)) {
                continue;
            }

            String fieldName = field.getName();
            String fieldType = mapJavaTypeToFieldType(field.getType()).name();
            SortType defaultSortType = determineDefaultSortType(field);

            log.debug("Adding column info for field: {}, type: {}, sort: {}", fieldName, fieldType, defaultSortType);

            // Add the column info to the map
            columnInfo.put(fieldName, new ColumnInfo(fieldName, fieldType, defaultSortType));
        }

        return columnInfo;
    }

    /**
     * Get all fields from a class and its superclasses
     *
     * @param clazz the class to analyze
     * @return list of all fields
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        // Traverse class hierarchy to get all fields
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * Check if a field represents a relationship (OneToMany, ManyToOne, etc.)
     *
     * @param field the field to check
     * @return true if it's a relationship field
     */
    private boolean isRelationshipField(Field field) {
        // Check for JPA relationship annotations
        return field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToOne.class) ||
                field.isAnnotationPresent(ManyToMany.class) ||
                field.isAnnotationPresent(OneToOne.class);
    }

    /**
     * Map Java type to FieldType enum
     *
     * @param javaType the Java class type
     * @return the corresponding FieldType
     */
    private FieldType mapJavaTypeToFieldType(Class<?> javaType) {
        if (javaType == String.class) {
            return FieldType.STRING;
        } else if (javaType == Boolean.class || javaType == boolean.class) {
            return FieldType.BOOLEAN;
        } else if (Number.class.isAssignableFrom(javaType) ||
                javaType == int.class ||
                javaType == long.class ||
                javaType == float.class ||
                javaType == double.class) {
            return FieldType.NUMBER;
        } else if (javaType == LocalDate.class) {
            return FieldType.DATE;
        } else if (javaType == LocalDateTime.class || javaType == Date.class) {
            return FieldType.DATETIME;
        } else if (javaType == LocalTime.class) {
            return FieldType.TIME;
        } else if (javaType.isEnum()) {
            return FieldType.STRING; // Enums are displayed as strings
        } else if (Collection.class.isAssignableFrom(javaType)) {
            return FieldType.ARRAY;
        } else {
            // Default to OBJECT for complex types
            return FieldType.OBJECT;
        }
    }

    /**
     * Determine default sort type based on field characteristics
     *
     * @param field the field to analyze
     * @return appropriate SortType
     */
    private SortType determineDefaultSortType(Field field) {
        // ID fields are typically sorted in ascending order
        if (field.getName().equals("id") ||
                field.isAnnotationPresent(Id.class)) {
            return SortType.ASCENDING;
        }

        // Name/title/code fields are typically sorted in ascending order
        if (field.getName().equals("name") ||
                field.getName().equals("title") ||
                field.getName().equals("code") ||
                field.getName().equals("username") ||
                field.getName().equals("email")) {
            return SortType.ASCENDING;
        }

        // Date fields are typically sorted in descending order (newest first)
        if (field.getType() == LocalDate.class ||
                field.getType() == LocalDateTime.class ||
                field.getType() == Date.class ||
                field.getName().contains("date") ||
                field.getName().contains("time")) {
            return SortType.DESCENDING;
        }

        // Other fields default to NONE (no default sort)
        return SortType.NONE;
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
     * Convert an entity to a Map representation for inclusion in the response
     */
    private Map<String, Object> convertEntityToMap(Object entity) {
        Map<String, Object> result = new HashMap<>();
        if (entity == null) {
            return result;
        }

        // Use reflection to extract properties
        for (Method method : entity.getClass().getMethods()) {
            String methodName = method.getName();
            if (methodName.startsWith("get") && !methodName.equals("getClass") &&
                    method.getParameterCount() == 0) {
                try {
                    String propertyName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
                    Object value = method.invoke(entity);

                    // Handle collection fields by extracting IDs
                    if (value instanceof Collection) {
                        result.put(propertyName + "Ids", extractIds((Collection<?>) value));
                    }
                    // Skip complex objects unless they have an ID
                    else if (value != null && !value.getClass().isPrimitive() &&
                            !value.getClass().getName().startsWith("java.lang") &&
                            !value.getClass().getName().startsWith("java.time") &&
                            !value.getClass().isEnum()) {
                        try {
                            Method getIdMethod = value.getClass().getMethod("getId");
                            Object id = getIdMethod.invoke(value);
                            if (id != null) {
                                result.put(propertyName + "Id", id);
                            }
                        } catch (Exception e) {
                            // Skip complex objects without getId method
                        }
                    }
                    // Include all primitive types and known simple types
                    else {
                        result.put(propertyName, value);
                    }
                } catch (Exception e) {
                    log.warn("Error extracting property {}: {}", methodName, e.getMessage());
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
                    result.put(propertyName, value);
                } catch (Exception e) {
                    log.warn("Failed to extract boolean property via method {}: {}", methodName, e.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * Extract IDs from a collection of entities
     */
    private List<Object> extractIds(Collection<?> entities) {
        List<Object> ids = new ArrayList<>();
        for (Object entity : entities) {
            try {
                Method getIdMethod = entity.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(entity);
                if (id != null) {
                    ids.add(id);
                }
            } catch (Exception e) {
                // Skip entities without getId method
            }
        }
        return ids;
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
            // Handle search map which uses ObjectType as keys
            for (Map.Entry<ObjectType, DataObject> entry : request.getSearch().entrySet()) {
                // Only process the search criteria for the requested object type
                if (entry.getKey() == request.getObjectType() && entry.getValue() != null) {
                    Map<String, Object> searchCriteria = entry.getValue().getData().getData();

                    if (searchCriteria != null) {
                        for (Map.Entry<String, Object> criterion : searchCriteria.entrySet()) {
                            String field = criterion.getKey();
                            Object value = criterion.getValue();

                            if (field != null && value != null && !value.toString().isEmpty()) {
                                // Check if the field exists in the root entity
                                try {
                                    if (value instanceof String) {
                                        // For string values, use case-insensitive LIKE search
                                        predicates.add(cb.like(
                                                cb.lower(root.get(field).as(String.class)),
                                                "%" + value.toString().toLowerCase() + "%"));
                                    } else {
                                        // For non-string values, use equals
                                        predicates.add(cb.equal(root.get(field), value));
                                    }
                                    log.debug("Added search predicate for field: {} with value: {}", field, value);
                                } catch (IllegalArgumentException e) {
                                    log.warn("Field {} not found in entity {}, skipping search criterion",
                                            field, root.getJavaType().getSimpleName());
                                }
                            }
                        }
                    }
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
        try {
            switch (filterType) {
                case EQUALS:
                    if (minValue != null) {
                        // Get the field type to perform appropriate comparison
                        Class<?> fieldType = getFieldType(root, field);

                        if (fieldType != null && Enum.class.isAssignableFrom(fieldType)) {
                            // Handle enum fields by converting string value to enum
                            try {
                                Object enumValue = convertStringToEnum(fieldType, minValue);
                                predicates.add(cb.equal(root.get(field), enumValue));
                                log.debug("Added enum equals predicate for field {}: {}", field, enumValue);
                            } catch (Exception e) {
                                // Fallback to string comparison if enum conversion fails
                                log.warn("Failed to convert '{}' to enum type {}, using string comparison", minValue,
                                        fieldType);
                                predicates.add(cb.equal(root.get(field).as(String.class), minValue));
                            }
                        } else {
                            // Standard string equality
                            predicates.add(cb.equal(root.get(field), minValue));
                        }
                    }
                    break;
                case NOT_EQUALS:
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
                                cb.lower(root.get(field).as(String.class)),
                                "%" + minValue.toLowerCase() + "%"));
                    }
                    break;
            }
        } catch (Exception e) {
            log.warn("Error creating predicate for field {}: {}", field, e.getMessage());
        }
    }

    /**
     * Get the type of a field from the root entity
     */
    private <T> Class<?> getFieldType(Root<T> root, String fieldName) {
        try {
            return root.get(fieldName).getJavaType();
        } catch (Exception e) {
            log.warn("Could not determine type for field {}: {}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * Convert a string value to an enum of the specified type
     */
    private Object convertStringToEnum(Class<?> enumType, String value) {
        return Enum.valueOf((Class<Enum>) enumType, value);
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
