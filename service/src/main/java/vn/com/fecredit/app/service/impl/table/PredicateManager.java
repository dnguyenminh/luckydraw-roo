package vn.com.fecredit.app.service.impl.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.FilterOperator;
import vn.com.fecredit.app.service.dto.FilterRequest;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableRow;

/**
 * Consolidated class for all Predicate-related operations.
 * This class combines functionality from:
 * - PredicateBuilder
 * - PredicateCopier
 * - PredicateHandler
 */
@Component
@Slf4j
public class PredicateManager {

    /**
     * Adds default filters to a list of predicates
     */
    public void addDefaultFilters(CriteriaBuilder cb, Root<?> root, List<Predicate> predicates) {
        try {
            // Check if the entity has a status field
            if (AbstractStatusAwareEntity.class.isAssignableFrom(root.getJavaType())) {
                Path<String> statusPath = root.get("status");
                predicates.add(cb.notEqual(statusPath, "DELETED"));
            }
        } catch (Exception e) {
            log.debug("Could not add default status filter for {}: {}", 
                    root.getJavaType().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Applies filters from a TableFetchRequest to a list of predicates
     */
    public void applyFilters(TableFetchRequest request, List<Predicate> predicates, 
            CriteriaBuilder cb, Root<?> root) {
        
        if (request.getFilters() == null || request.getFilters().isEmpty()) {
            return;
        }
        
        for (FilterRequest filter : request.getFilters()) {
            String fieldName = filter.getField();
            Object value = filter.getValue();
            FilterOperator operator = filter.getOperator();
            
            if (fieldName == null || operator == null) {
                continue;
            }
            
            try {
                // Parse path for nested attributes
                Path<?> path;
                if (fieldName.contains(".")) {
                    String[] parts = fieldName.split("\\.");
                    From<?, ?> from = root;
                    Map<String, Join<?, ?>> joins = new HashMap<>();
                    
                    // Create joins for all but the last part
                    String joinPath = "";
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (!joinPath.isEmpty()) {
                            joinPath += ".";
                        }
                        joinPath += parts[i];
                        
                        // Get or create join
                        if (joins.containsKey(joinPath)) {
                            from = joins.get(joinPath);
                        } else {
                            Join<?, ?> join = from.join(parts[i], JoinType.LEFT);
                            joins.put(joinPath, join);
                            from = join;
                        }
                    }
                    
                    // Get the field from the last join
                    path = from.get(parts[parts.length - 1]);
                } else {
                    path = root.get(fieldName);
                }
                
                // Apply the filter based on operator
                applyOperator(predicates, cb, path, operator, value);
                
            } catch (Exception e) {
                log.warn("Error applying filter on field {}: {}", fieldName, e.getMessage());
            }
        }
    }

    /**
     * Applies a specific operator to create a predicate
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void applyOperator(List<Predicate> predicates, CriteriaBuilder cb, 
            Path<?> path, FilterOperator operator, Object value) {
        
        // Handle null values specially
        if (value == null) {
            switch (operator) {
                case EQ:
                case NULL:
                    predicates.add(cb.isNull(path));
                    return;
                case NE:
                case NOT_NULL:
                    predicates.add(cb.isNotNull(path));
                    return;
                default:
                    return; // Other operators can't work with null
            }
        }
        
        // Convert value if needed
        Object convertedValue = convertValue(value, path.getJavaType());
        
        // For string fields, use case-insensitive comparison
        if (path.getJavaType() == String.class && convertedValue instanceof String) {
            String stringValue = ((String) convertedValue).toLowerCase();
            Expression<String> lowerField = cb.lower(path.as(String.class));
            
            switch (operator) {
                case EQ:
                    predicates.add(cb.equal(lowerField, stringValue));
                    break;
                case NE:
                    predicates.add(cb.notEqual(lowerField, stringValue));
                    break;
                case CONTAINS:
                    predicates.add(cb.like(lowerField, "%" + stringValue + "%"));
                    break;
                case STARTS_WITH:
                    predicates.add(cb.like(lowerField, stringValue + "%"));
                    break;
                case ENDS_WITH:
                    predicates.add(cb.like(lowerField, "%" + stringValue));
                    break;
                default:
                    log.warn("Unsupported operator {} for string field", operator);
            }
            return;
        }
        
        // For comparable fields, use comparisons
        if (Comparable.class.isAssignableFrom(path.getJavaType()) && convertedValue instanceof Comparable) {
            Path<Comparable> comparablePath = (Path<Comparable>) path;
            Comparable comparableValue = (Comparable) convertedValue;
            
            switch (operator) {
                case EQ:
                    predicates.add(cb.equal(path, comparableValue));
                    break;
                case NE:
                    predicates.add(cb.notEqual(path, comparableValue));
                    break;
                case GT:
                    predicates.add(cb.greaterThan(comparablePath, comparableValue));
                    break;
                case GE:
                    predicates.add(cb.greaterThanOrEqualTo(comparablePath, comparableValue));
                    break;
                case LT:
                    predicates.add(cb.lessThan(comparablePath, comparableValue));
                    break;
                case LE:
                    predicates.add(cb.lessThanOrEqualTo(comparablePath, comparableValue));
                    break;
                default:
                    log.warn("Unsupported operator {} for comparable field", operator);
            }
            return;
        }
        
        // Default case: use equals
        predicates.add(cb.equal(path, convertedValue));
    }

    /**
     * Applies search criteria from a TableFetchRequest
     */
    public void applySearch(TableFetchRequest request, List<Predicate> predicates, 
            CriteriaBuilder cb, Root<?> root) {
        
        Map<ObjectType, DataObject> searchMap = request.getSearch();
        if (searchMap == null || searchMap.isEmpty()) {
            return;
        }
        
        Map<String, Join<?, ?>> joins = new HashMap<>();
        List<Predicate> searchPredicates = new ArrayList<>();
        
        // Process each search object by its object type
        for (Map.Entry<ObjectType, DataObject> entry : searchMap.entrySet()) {
            DataObject dataObject = entry.getValue();
            if (dataObject == null || dataObject.getData() == null) {
                continue;
            }
            
            // Extract search criteria from the data object's row data
            TableRow rowData = dataObject.getData();
            for (Map.Entry<String, Object> fieldEntry : rowData.getData().entrySet()) {
                String fieldName = fieldEntry.getKey();
                Object value = fieldEntry.getValue();
                
                if (fieldName == null || value == null) {
                    continue;
                }
                
                // Handle nested search keys using dot notation
                if (fieldName.contains(".")) {
                    handleNestedSearch(root, joins, searchPredicates, cb, fieldName, value);
                } else {
                    // Handle simple field search
                    try {
                        Path<?> path = root.get(fieldName);
                        createSearchPredicate(cb, path, value).ifPresent(searchPredicates::add);
                    } catch (Exception e) {
                        log.debug("Error creating search predicate for field {}: {}", fieldName, e.getMessage());
                    }
                }
            }
        }
        
        // Combine search predicates with AND logic
        if (!searchPredicates.isEmpty()) {
            predicates.add(cb.and(searchPredicates.toArray(new Predicate[0])));
        }
    }

    /**
     * Handles nested search with dot notation
     */
    private void handleNestedSearch(Root<?> root, Map<String, Join<?, ?>> joins, 
            List<Predicate> predicates, CriteriaBuilder cb, String key, Object value) {
        
        String[] parts = key.split("\\.");
        From<?, ?> from = root;
        StringBuilder path = new StringBuilder();
        
        // Create joins for all but the last part
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (path.length() > 0) {
                path.append(".");
            }
            path.append(part);
            
            String currentPath = path.toString();
            if (joins.containsKey(currentPath)) {
                from = joins.get(currentPath);
            } else {
                // Try to determine join type based on field type
                JoinType joinType = determineJoinType(from, part);
                Join<?, ?> join = from.join(part, joinType);
                joins.put(currentPath, join);
                from = join;
            }
        }
        
        // Get the field from the last join
        String fieldName = parts[parts.length - 1];
        try {
            Path<?> fieldPath = from.get(fieldName);
            createSearchPredicate(cb, fieldPath, value).ifPresent(predicates::add);
        } catch (Exception e) {
            log.debug("Error creating search predicate for nested field {}: {}", key, e.getMessage());
        }
    }
    
    /**
     * Determines the appropriate join type based on the field type
     */
    private JoinType determineJoinType(From<?, ?> from, String attributeName) {
        try {
            // Try to use reflection to determine if attribute is a collection
            Class<?> entityType = from.getJavaType();
            
            if (entityType != null) {
                try {
                    java.lang.reflect.Field field = findField(entityType, attributeName);
                    if (field != null) {
                        Class<?> fieldType = field.getType();
                        if (java.util.Collection.class.isAssignableFrom(fieldType)) {
                            // Collections usually need inner join to avoid duplicates
                            return JoinType.INNER;
                        }
                    }
                } catch (Exception e) {
                    // Ignore reflection errors
                }
            }
            
            // For singular associations or if we can't determine, use left join to include nulls
            return JoinType.LEFT;
            
        } catch (Exception e) {
            // Default to LEFT join for safety
            return JoinType.LEFT;
        }
    }
    
    /**
     * Finds a field in a class hierarchy
     */
    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null) {
            return null;
        }
        
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Try superclass
            return findField(clazz.getSuperclass(), fieldName);
        }
    }

    /**
     * Creates a search predicate based on the field type and value
     * 
     * This method handles various value types including:
     * - Simple values (strings, numbers, dates, etc.)
     * - Complex object values with operators (Map with "operator" and "value" keys)
     * - Collection values (arrays, lists, sets)
     * - Null values
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private java.util.Optional<Predicate> createSearchPredicate(CriteriaBuilder cb, Path<?> path, Object value) {
        if (value == null) {
            return java.util.Optional.of(cb.isNull(path));
        }
        
        // Handle DataObject values if they come from the frontend
        if (value instanceof DataObject) {
            DataObject dataObject = (DataObject) value;
            if (dataObject.getData() != null && dataObject.getData().getData() != null) {
                // Extract the first value from the DataObject (assuming it contains search criteria)
                Map<String, Object> data = dataObject.getData().getData();
                if (!data.isEmpty()) {
                    return createSearchPredicate(cb, path, data.values().iterator().next());
                }
            }
            return java.util.Optional.empty();
        }
        
        // Handle complex search objects
        if (value instanceof Map) {
            return handleComplexSearchValue(cb, path, (Map<String, Object>) value);
        }
        
        // Handle array/collection values
        if (value instanceof Object[] || value instanceof Collection) {
            Collection<?> values = value instanceof Object[] ? 
                    java.util.Arrays.asList((Object[]) value) : (Collection<?>) value;
                    
            // Empty collections should be ignored
            if (values.isEmpty()) {
                return java.util.Optional.empty();
            }
            
            // For single-value collections, treat as simple value
            if (values.size() == 1) {
                return createSearchPredicate(cb, path, values.iterator().next());
            }
            
            // For multi-value collections, create IN predicate
            List<Object> convertedValues = new ArrayList<>();
            Class<?> fieldType = path.getJavaType();
            
            for (Object val : values) {
                try {
                    if (val != null) {
                        convertedValues.add(convertValue(val, fieldType));
                    }
                } catch (Exception e) {
                    log.debug("Failed to convert value for IN predicate: {}", e.getMessage());
                }
            }
            
            if (!convertedValues.isEmpty()) {
                return java.util.Optional.of(path.in(convertedValues));
            }
            
            return java.util.Optional.empty();
        }
        
        // Convert value to match field type
        try {
            Class<?> fieldType = path.getJavaType();
            Object convertedValue = convertValue(value, fieldType);
            
            // String fields use LIKE for searching by default
            if (String.class.equals(fieldType) && convertedValue instanceof String) {
                String stringValue = ((String) convertedValue).toLowerCase();
                
                // Empty string search should be treated specially
                if (stringValue.isEmpty()) {
                    // Search for either null or empty string
                    return java.util.Optional.of(
                            cb.or(
                                cb.isNull(path),
                                cb.equal(path, "")
                            )
                    );
                }
                
                // Default string search uses contains
                return java.util.Optional.of(
                        cb.like(cb.lower(path.as(String.class)), "%" + stringValue + "%"));
            }
            
            // Boolean fields
            if ((Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) 
                    && convertedValue instanceof Boolean) {
                return java.util.Optional.of(cb.equal(path, convertedValue));
            }
            
            // Date fields - handle ranges if specified as a string with format "fromDate~toDate"
            if ((java.time.LocalDate.class.equals(fieldType) || java.time.LocalDateTime.class.equals(fieldType))
                    && value instanceof String && ((String) value).contains("~")) {
                
                String[] rangeParts = ((String) value).split("~", 2);
                Predicate datePredicate = null;
                
                if (rangeParts.length == 2) {
                    String fromDateStr = rangeParts[0].trim();
                    String toDateStr = rangeParts[1].trim();
                    
                    if (!fromDateStr.isEmpty()) {
                        Object fromDate = convertValue(fromDateStr, fieldType);
                        if (fromDate != null) {
                            datePredicate = cb.greaterThanOrEqualTo(
                                    (Path<Comparable>) path, (Comparable) fromDate);
                        }
                    }
                    
                    if (!toDateStr.isEmpty()) {
                        Object toDate = convertValue(toDateStr, fieldType);
                        if (toDate != null) {
                            Predicate toDatePredicate = cb.lessThanOrEqualTo(
                                    (Path<Comparable>) path, (Comparable) toDate);
                            
                            datePredicate = (datePredicate != null) ? 
                                    cb.and(datePredicate, toDatePredicate) : toDatePredicate;
                        }
                    }
                    
                    if (datePredicate != null) {
                        return java.util.Optional.of(datePredicate);
                    }
                }
            }
            
            // Use equals for other types
            return java.util.Optional.of(cb.equal(path, convertedValue));
            
        } catch (Exception e) {
            log.debug("Error creating search predicate: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }
    
    /**
     * Handles complex search values that are represented as maps
     * 
     * Supports the following formats:
     * 1. Standard format: {"operator": "EQ", "value": "someValue"}
     * 2. Range format: {"from": value1, "to": value2}
     * 3. Multiple conditions: {"OR": [condition1, condition2]} or {"AND": [condition1, condition2]}
     * 4. Nested format: {"field": "nested.field", "operator": "EQ", "value": "someValue"}
     */
    private java.util.Optional<Predicate> handleComplexSearchValue(
            CriteriaBuilder cb, Path<?> path, Map<String, Object> valueMap) {
        
        // Check for special operators in the map (standard format)
        if (valueMap.containsKey("operator") && valueMap.containsKey("value")) {
            String operator = valueMap.get("operator").toString().toUpperCase();
            Object searchValue = valueMap.get("value");
            
            // If the map has a field key, it's a reference to a different field
            if (valueMap.containsKey("field") && valueMap.get("field") instanceof String) {
                try {
                    String fieldName = (String) valueMap.get("field");
                    if (path.getParentPath() instanceof From<?, ?>) {
                        From<?, ?> from = (From<?, ?>) path.getParentPath();
                        Path<?> referencedPath = from.get(fieldName);
                        return createPredicateWithOperator(cb, referencedPath, operator, searchValue);
                    }
                } catch (Exception e) {
                    log.debug("Cannot create predicate for referenced field: {}", e.getMessage());
                }
            }
            
            return createPredicateWithOperator(cb, path, operator, searchValue);
        }
        
        // Check for range format (from/to values)
        if (valueMap.containsKey("from") || valueMap.containsKey("to")) {
            Class<?> fieldType = path.getJavaType();
            List<Predicate> rangePredicates = new ArrayList<>();                    // Handle comparison with fromValue
            if (valueMap.containsKey("from") && valueMap.get("from") != null) {
                Object fromValue = convertValue(valueMap.get("from"), fieldType);
                if (Comparable.class.isAssignableFrom(fieldType) && fromValue instanceof Comparable) {
                    // Use a type-safe approach for comparable values
                    Predicate predicate = cb.greaterThanOrEqualTo(
                            path.as((Class<Comparable>) fieldType), (Comparable) fromValue);
                    rangePredicates.add(predicate);
                }
            }
            
            // Handle comparison with toValue
            if (valueMap.containsKey("to") && valueMap.get("to") != null) {
                Object toValue = convertValue(valueMap.get("to"), fieldType);
                if (Comparable.class.isAssignableFrom(fieldType) && toValue instanceof Comparable) {
                    // Use a type-safe approach for comparable values
                    Predicate predicate = cb.lessThanOrEqualTo(
                            path.as((Class<Comparable>) fieldType), (Comparable) toValue);
                    rangePredicates.add(predicate);
                }
            }
            
            if (!rangePredicates.isEmpty()) {
                return java.util.Optional.of(
                        cb.and(rangePredicates.toArray(new Predicate[0])));
            }
        }
        
        // Handle logical operators (AND/OR with multiple conditions)
        if (valueMap.containsKey("AND") && valueMap.get("AND") instanceof Collection) {
            Collection<?> conditions = (Collection<?>) valueMap.get("AND");
            List<Predicate> andPredicates = new ArrayList<>();
            
            for (Object condition : conditions) {
                if (condition instanceof Map) {
                    Map<String, Object> conditionMap = (Map<String, Object>) condition;
                    handleComplexSearchValue(cb, path, conditionMap)
                        .ifPresent(andPredicates::add);
                }
            }
            
            if (!andPredicates.isEmpty()) {
                return java.util.Optional.of(
                        cb.and(andPredicates.toArray(new Predicate[0])));
            }
        }
        
        if (valueMap.containsKey("OR") && valueMap.get("OR") instanceof Collection) {
            Collection<?> conditions = (Collection<?>) valueMap.get("OR");
            List<Predicate> orPredicates = new ArrayList<>();
            
            for (Object condition : conditions) {
                if (condition instanceof Map) {
                    Map<String, Object> conditionMap = (Map<String, Object>) condition;
                    handleComplexSearchValue(cb, path, conditionMap)
                        .ifPresent(orPredicates::add);
                }
            }
            
            if (!orPredicates.isEmpty()) {
                return java.util.Optional.of(
                        cb.or(orPredicates.toArray(new Predicate[0])));
            }
        }
        
        // Default to equals if no special structure is found
        return java.util.Optional.of(cb.equal(path, valueMap));
    }
    
    /**
     * Creates a predicate with a specific operator
     * 
     * Supported operators:
     * - EQ, NE: Equal, Not Equal
     * - CONTAINS, STARTS_WITH, ENDS_WITH: String pattern matching
     * - GT, GE, LT, LE: Greater than, Greater or equal, Less than, Less or equal
     * - IN, NOT_IN: Collection membership
     * - IS_NULL, IS_NOT_NULL: Null checks
     * - BETWEEN: Range comparison
     * - EXISTS, NOT_EXISTS: Subquery existence
     * - EMPTY, NOT_EMPTY: Collection emptiness
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private java.util.Optional<Predicate> createPredicateWithOperator(
            CriteriaBuilder cb, Path<?> path, String operator, Object value) {
        
        if (value == null && 
            !operator.equals("IS_NULL") && 
            !operator.equals("IS_NOT_NULL") &&
            !operator.equals("EMPTY") && 
            !operator.equals("NOT_EMPTY")) {
            return java.util.Optional.empty();
        }
        
        try {
            Class<?> fieldType = path.getJavaType();
            
            switch (operator) {
                case "EQ":
                    Object convertedValue = convertValue(value, fieldType);
                    
                    // Special handling for strings - case insensitive equals
                    if (String.class.equals(fieldType) && convertedValue instanceof String) {
                        String stringValue = ((String) convertedValue).toLowerCase();
                        if (stringValue.isEmpty()) {
                            // Empty string search matches both null and empty
                            return java.util.Optional.of(
                                cb.or(
                                    cb.isNull(path),
                                    cb.equal(cb.lower(path.as(String.class)), "")
                                )
                            );
                        } else {
                            return java.util.Optional.of(
                                cb.equal(cb.lower(path.as(String.class)), stringValue)
                            );
                        }
                    }
                    
                    return java.util.Optional.of(cb.equal(path, convertedValue));
                    
                case "NE":
                    convertedValue = convertValue(value, fieldType);
                    
                    // Special handling for strings - case insensitive not equals
                    if (String.class.equals(fieldType) && convertedValue instanceof String) {
                        String stringValue = ((String) convertedValue).toLowerCase();
                        if (stringValue.isEmpty()) {
                            // Not empty string means not null and not empty
                            return java.util.Optional.of(
                                cb.and(
                                    cb.isNotNull(path),
                                    cb.notEqual(cb.lower(path.as(String.class)), "")
                                )
                            );
                        } else {
                            return java.util.Optional.of(
                                cb.notEqual(cb.lower(path.as(String.class)), stringValue)
                            );
                        }
                    }
                    
                    return java.util.Optional.of(cb.notEqual(path, convertedValue));
                    
                case "CONTAINS":
                    if (String.class.equals(fieldType) && value != null) {
                        String stringValue = value.toString().toLowerCase();
                        return java.util.Optional.of(
                                cb.like(cb.lower(path.as(String.class)), "%" + stringValue + "%"));
                    }
                    break;
                    
                case "NOT_CONTAINS":
                    if (String.class.equals(fieldType) && value != null) {
                        String stringValue = value.toString().toLowerCase();
                        return java.util.Optional.of(
                                cb.notLike(cb.lower(path.as(String.class)), "%" + stringValue + "%"));
                    }
                    break;
                    
                case "STARTS_WITH":
                    if (String.class.equals(fieldType) && value != null) {
                        String stringValue = value.toString().toLowerCase();
                        return java.util.Optional.of(
                                cb.like(cb.lower(path.as(String.class)), stringValue + "%"));
                    }
                    break;
                    
                case "ENDS_WITH":
                    if (String.class.equals(fieldType) && value != null) {
                        String stringValue = value.toString().toLowerCase();
                        return java.util.Optional.of(
                                cb.like(cb.lower(path.as(String.class)), "%" + stringValue));
                    }
                    break;
                    
                case "GT":
                    if (Comparable.class.isAssignableFrom(fieldType)) {
                        Object gtValue = convertValue(value, fieldType);
                        Predicate predicate = cb.greaterThan(
                                path.as((Class<Comparable>) fieldType), 
                                (Comparable) gtValue);
                        return java.util.Optional.of(predicate);
                    }
                    break;
                    
                case "GE":
                    if (Comparable.class.isAssignableFrom(fieldType)) {
                        Object geValue = convertValue(value, fieldType);
                        Predicate predicate = cb.greaterThanOrEqualTo(
                                path.as((Class<Comparable>) fieldType), 
                                (Comparable) geValue);
                        return java.util.Optional.of(predicate);
                    }
                    break;
                    
                case "LT":
                    if (Comparable.class.isAssignableFrom(fieldType)) {
                        Object ltValue = convertValue(value, fieldType);
                        Predicate predicate = cb.lessThan(
                                path.as((Class<Comparable>) fieldType), 
                                (Comparable) ltValue);
                        return java.util.Optional.of(predicate);
                    }
                    break;
                    
                case "LE":
                    if (Comparable.class.isAssignableFrom(fieldType)) {
                        Object leValue = convertValue(value, fieldType);
                        Predicate predicate = cb.lessThanOrEqualTo(
                                path.as((Class<Comparable>) fieldType), 
                                (Comparable) leValue);
                        return java.util.Optional.of(predicate);
                    }
                    break;
                    
                case "BETWEEN":
                    if (value instanceof Map) {
                        Map<String, Object> rangeMap = (Map<String, Object>) value;
                        Object fromValue = rangeMap.get("from");
                        Object toValue = rangeMap.get("to");
                        
                        if (Comparable.class.isAssignableFrom(fieldType) && fromValue != null && toValue != null) {
                            Object fromConverted = convertValue(fromValue, fieldType);
                            Object toConverted = convertValue(toValue, fieldType);
                            
                            Predicate betweenPredicate = cb.between(
                                    path.as((Class<Comparable>) fieldType),
                                    (Comparable) fromConverted,
                                    (Comparable) toConverted);
                            
                            return java.util.Optional.of(betweenPredicate);
                        }
                    } else if (value instanceof String && ((String) value).contains("~")) {
                        String[] parts = ((String) value).split("~", 2);
                        if (parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
                            Object fromConverted = convertValue(parts[0].trim(), fieldType);
                            Object toConverted = convertValue(parts[1].trim(), fieldType);
                            
                            Predicate betweenPredicate = cb.between(
                                    path.as((Class<Comparable>) fieldType),
                                    (Comparable) fromConverted,
                                    (Comparable) toConverted);
                            
                            return java.util.Optional.of(betweenPredicate);
                        }
                    }
                    break;
                    
                case "IN":
                    if (value instanceof Collection || value instanceof Object[]) {
                        Collection<?> values = value instanceof Object[] ? 
                                java.util.Arrays.asList((Object[]) value) : (Collection<?>) value;
                                
                        if (!values.isEmpty()) {
                            Set<Object> convertedValues = new HashSet<>();
                            for (Object val : values) {
                                if (val != null) {
                                    convertedValues.add(convertValue(val, fieldType));
                                }
                            }
                            if (!convertedValues.isEmpty()) {
                                return java.util.Optional.of(path.in(convertedValues));
                            }
                        }
                    } else if (value instanceof String && ((String) value).contains(",")) {
                        // Handle comma-separated list of values
                        String[] values = ((String) value).split(",");
                        Set<Object> convertedValues = new HashSet<>();
                        for (String val : values) {
                            if (val != null && !val.trim().isEmpty()) {
                                convertedValues.add(convertValue(val.trim(), fieldType));
                            }
                        }
                        if (!convertedValues.isEmpty()) {
                            return java.util.Optional.of(path.in(convertedValues));
                        }
                    }
                    break;
                    
                case "NOT_IN":
                    if (value instanceof Collection || value instanceof Object[]) {
                        Collection<?> values = value instanceof Object[] ? 
                                java.util.Arrays.asList((Object[]) value) : (Collection<?>) value;
                                
                        if (!values.isEmpty()) {
                            Set<Object> convertedValues = new HashSet<>();
                            for (Object val : values) {
                                if (val != null) {
                                    convertedValues.add(convertValue(val, fieldType));
                                }
                            }
                            if (!convertedValues.isEmpty()) {
                                return java.util.Optional.of(cb.not(path.in(convertedValues)));
                            }
                        }
                    }
                    break;
                    
                case "IS_NULL":
                    return java.util.Optional.of(cb.isNull(path));
                    
                case "IS_NOT_NULL":
                    return java.util.Optional.of(cb.isNotNull(path));
                    
                case "EMPTY":
                    if (java.util.Collection.class.isAssignableFrom(fieldType)) {
                        // Check if collection is empty
                        return java.util.Optional.of(
                                cb.isEmpty(path.as(java.util.Collection.class)));
                    } else if (String.class.equals(fieldType)) {
                        // For strings, empty means null or empty string
                        return java.util.Optional.of(
                                cb.or(
                                    cb.isNull(path),
                                    cb.equal(path, "")
                                )
                        );
                    }
                    break;
                    
                case "NOT_EMPTY":
                    if (java.util.Collection.class.isAssignableFrom(fieldType)) {
                        // Check if collection is not empty
                        return java.util.Optional.of(
                                cb.isNotEmpty(path.as(java.util.Collection.class)));
                    } else if (String.class.equals(fieldType)) {
                        // For strings, not empty means not null and not empty string
                        return java.util.Optional.of(
                                cb.and(
                                    cb.isNotNull(path),
                                    cb.notEqual(path, "")
                                )
                        );
                    }
                    break;
                    
                default:
                    log.debug("Unsupported operator: {}", operator);
            }
            
        } catch (Exception e) {
            log.debug("Error creating predicate with operator {}: {}", operator, e.getMessage());
        }
        
        return java.util.Optional.empty();
    }

    /**
     * Copies a predicate from one query to another, preserving its structure
     */
    public Predicate copyPredicate(
            Predicate original,
            CriteriaBuilder cb,
            Root<?> newRoot,
            Map<String, Join<?, ?>> joinMap) {

        if (original == null) {
            return null;
        }

        // Handle composite predicates (AND/OR)
        if (original.getOperator() != null) {
            List<Predicate> predicates = new ArrayList<>();
            for (Expression<Boolean> expr : original.getExpressions()) {
                if (expr instanceof Predicate) {
                    predicates.add(copyPredicate((Predicate) expr, cb, newRoot, joinMap));
                }
            }
            return original.getOperator() == Predicate.BooleanOperator.AND
                    ? cb.and(predicates.toArray(new Predicate[0]))
                    : cb.or(predicates.toArray(new Predicate[0]));
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

    /**
     * Recreates a predicate with a new path
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Predicate recreatePredicateWithNewPath(
            Predicate original,
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
     * Extracts a value from a predicate string
     */
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

    /**
     * Converts a value to the appropriate type
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
            log.debug("Failed to convert value '{}' to type {}: {}", stringValue, targetType, e.getMessage());
        }

        return value;
    }

    /**
     * Build a list of predicates based on a TableFetchRequest
     *
     * @param request The table fetch request containing filters and search criteria
     * @param cb The criteria builder
     * @param root The root entity
     * @return List of predicates
     */
    public List<Predicate> buildPredicates(TableFetchRequest request, CriteriaBuilder cb, Root<?> root) {
        List<Predicate> predicates = new ArrayList<>();
        
        // Add default filters (e.g., non-deleted status)
        addDefaultFilters(cb, root, predicates);
        
        // Apply request filters
        applyFilters(request, predicates, cb, root);
        
        // Apply search criteria
        applySearch(request, predicates, cb, root);
        
        return predicates;
    }
}
