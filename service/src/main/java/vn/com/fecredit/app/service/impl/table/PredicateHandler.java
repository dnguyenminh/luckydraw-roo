package vn.com.fecredit.app.service.impl.table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.query.sqm.tree.domain.SqmBasicValuedSimplePath;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.FilterRequest;
import vn.com.fecredit.app.service.dto.FilterType;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableRow;

@Slf4j
@Component
@RequiredArgsConstructor
public class PredicateHandler {

    private final JoinHandler joinHandler;
    private final FieldValidator fieldValidator;

    /**
     * Adds default filters to exclude deleted records where possible
     */
    public void addDefaultFilters(
        CriteriaBuilder cb,
        Root<?> root,
        List<Predicate> predicates) {
        try {
            // Check if this entity has a status field
            if (fieldValidator.hasField(root.getJavaType(), "status")) {
                // Default filter: status != DELETED
                predicates.add(cb.notEqual(
                    root.get("status"),
                    CommonStatus.DELETED.name())); // Use string name for compatibility
            }
        } catch (Exception e) {
            log.debug("Could not add default status filter: {}", e.getMessage());
        }
    }

    /**
     * Applies filters from the request to the criteria query
     */
    public void applyFilters(
        TableFetchRequest request,
        List<Predicate> predicates,
        CriteriaBuilder cb,
        Root<?> root) {

        if (request == null || request.getFilters() == null || request.getFilters().isEmpty()) {
            return;
        }

        Map<String, Join<?, ?>> joinMap = new HashMap<>();
        Map<String, ColumnInfo> columnTypeMap = request.getViewColumns()
            .stream()
            .collect(Collectors.toMap(ColumnInfo::getFieldName, viewCol -> viewCol));
        for (FilterRequest filter : request.getFilters()) {
            try {
                String fieldName = filter.getField();
                FilterType type = filter.getFilterType();

                if (fieldName == null || type == null) {
                    continue;
                }

                // Handle potentially nested paths
                Path<?> path = getPathForField(fieldName, root, joinMap);
                Class<?> fieldType = (Class<?>) (((SqmBasicValuedSimplePath) path).getLhs()).getExpressible().getRelationalJavaType().getJavaType();
                ColumnInfo columnInfo = columnTypeMap.get(path.getAlias());
                if (columnInfo != null) {
                    columnInfo.setObjectType(ObjectType.valueOf(fieldType.getSimpleName()));
                }
                if (path != null) {
                    // Handle filter types with appropriate methods
                    switch (type) {
                        case EQUALS:
                            addEqualsPredicate(cb, predicates, path, filter.getMinValue());
                            break;
                        case NOT_EQUALS:
                            addNotEqualsPredicate(cb, predicates, path, filter.getMinValue());
                            break;
                        case GREATER_THAN:
                            addComparisonPredicate(cb, predicates, path, filter.getMinValue(),
                                ComparisonType.GREATER_THAN);
                            break;
                        case GREATER_THAN_OR_EQUALS:
                            addComparisonPredicate(cb, predicates, path, filter.getMinValue(),
                                ComparisonType.GREATER_THAN_OR_EQUAL);
                            break;
                        case LESS_THAN:
                            addComparisonPredicate(cb, predicates, path, filter.getMinValue(),
                                ComparisonType.LESS_THAN);
                            break;
                        case LESS_THAN_OR_EQUALS:
                            addComparisonPredicate(cb, predicates, path, filter.getMinValue(),
                                ComparisonType.LESS_THAN_OR_EQUAL);
                            break;
                        case CONTAINS:
                            addLikePredicate(cb, predicates, (Path<String>) path, filter.getMinValue(), LikeType.CONTAINS);
                            break;
                        case STARTS_WITH:
                            addLikePredicate(cb, predicates, (Path<String>) path, filter.getMinValue(), LikeType.STARTS_WITH);
                            break;
                        case ENDS_WITH:
                            addLikePredicate(cb, predicates, (Path<String>) path, filter.getMinValue(), LikeType.ENDS_WITH);
                            break;
                        case IN:
                            addInPredicate(cb, predicates, path, filter.getMinValue());
                            break;
                        case BETWEEN:
                            addBetweenPredicate(cb, predicates, path, filter.getMinValue(), filter.getMaxValue());
                            break;
                        default:
                            log.warn("Unsupported filter type: {}", type);
                    }
                }
            } catch (Exception e) {
                log.warn("Error applying filter: {}", e.getMessage());
            }
        }
    }

    /**
     * Helper enum for comparison types
     */
    private enum ComparisonType {
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL
    }

    /**
     * Helper enum for like predicate types
     */
    private enum LikeType {
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH
    }

    /**
     * Safely adds a comparison predicate that handles types properly
     */
    @SuppressWarnings({"unchecked"})
    private <Y extends Comparable<? super Y>> void addComparisonPredicate(
        CriteriaBuilder cb,
        List<Predicate> predicates,
        Path<?> path,
        Object value,
        ComparisonType comparisonType) {

        if (value == null) {
            return; // Skip null comparisons
        }

        try {
            Class<?> fieldType = path.getJavaType();
            Object convertedValue = fieldValidator.convertValue(value, fieldType);

            if (convertedValue != null && Comparable.class.isAssignableFrom(fieldType)) {
                Path<Y> typedPath = (Path<Y>) path;
                Y typedValue = (Y) convertedValue;

                switch (comparisonType) {
                    case GREATER_THAN:
                        predicates.add(cb.greaterThan(typedPath, typedValue));
                        break;
                    case GREATER_THAN_OR_EQUAL:
                        predicates.add(cb.greaterThanOrEqualTo(typedPath, typedValue));
                        break;
                    case LESS_THAN:
                        predicates.add(cb.lessThan(typedPath, typedValue));
                        break;
                    case LESS_THAN_OR_EQUAL:
                        predicates.add(cb.lessThanOrEqualTo(typedPath, typedValue));
                        break;
                }
            } else if (Number.class.isAssignableFrom(fieldType)) {
                // Use numeric comparisons for numbers
                Path<Number> numPath = (Path<Number>) path;
                Number numValue = (Number) convertedValue;

                switch (comparisonType) {
                    case GREATER_THAN:
                        predicates.add(cb.gt(numPath, numValue));
                        break;
                    case GREATER_THAN_OR_EQUAL:
                        predicates.add(cb.ge(numPath, numValue));
                        break;
                    case LESS_THAN:
                        predicates.add(cb.lt(numPath, numValue));
                        break;
                    case LESS_THAN_OR_EQUAL:
                        predicates.add(cb.le(numPath, numValue));
                        break;
                }
            }
        } catch (Exception e) {
            log.warn("Error adding comparison predicate: {}", e.getMessage());
        }
    }

    /**
     * Adds a between predicate that properly handles types
     */
    @SuppressWarnings({"unchecked"})
    private <Y extends Comparable<? super Y>> void addBetweenPredicate(
        CriteriaBuilder cb,
        List<Predicate> predicates,
        Path<?> path,
        Object minValue,
        Object maxValue) {

        if (minValue == null || maxValue == null) {
            return; // Skip if either bound is null
        }

        try {
            Class<?> fieldType = path.getJavaType();
            Object convertedMinValue = fieldValidator.convertValue(minValue, fieldType);
            Object convertedMaxValue = fieldValidator.convertValue(maxValue, fieldType);

            if (convertedMinValue != null && convertedMaxValue != null &&
                Comparable.class.isAssignableFrom(fieldType)) {
                Path<Y> typedPath = (Path<Y>) path;
                Y typedMinValue = (Y) convertedMinValue;
                Y typedMaxValue = (Y) convertedMaxValue;

                predicates.add(cb.between(typedPath, typedMinValue, typedMaxValue));
            }
        } catch (Exception e) {
            log.warn("Error adding between predicate: {}", e.getMessage());
        }
    }

    /**
     * Applies search criteria for related entities with graceful error handling
     */
    @SuppressWarnings("unchecked")
    public void applySearch(
        TableFetchRequest request,
        List<Predicate> predicates,
        CriteriaBuilder cb,
        Root<?> root) {

        if (request == null || request.getSearch() == null || request.getSearch().isEmpty()) {
            return;
        }

        Map<ObjectType, DataObject> searchCriteria = (Map<ObjectType, DataObject>) (Map<?, ?>) request.getSearch();

        // Create joins based on search criteria
        Map<String, Join<?, ?>> joinMap = new HashMap<>();

        // Apply search criteria by object type
        for (Map.Entry<ObjectType, DataObject> entry : searchCriteria.entrySet()) {
            ObjectType objectType = entry.getKey();
            DataObject dataObject = entry.getValue();

            try {
                // Skip if this is the same entity type as the root
                if (objectType.name().equalsIgnoreCase(root.getJavaType().getSimpleName())) {
                    applySearchToRoot(root, dataObject, predicates, cb);
                    continue;
                }

                // Get the relationship path for this object type
                String relationshipPath = joinHandler.getRelationshipPath(root.getJavaType(), objectType);

                if (relationshipPath != null) {
                    // Get or create the join for this path
                    Join<?, ?> join = joinMap.get(relationshipPath);

                    if (join == null) {
                        try {
                            join = joinHandler.createNestedJoins(root, relationshipPath, joinMap, JoinType.INNER);

                            if (join == null) {
                                log.warn("Failed to create join for path: {}", relationshipPath);
                                continue;
                            }
                        } catch (Exception e) {
                            log.warn("Failed to create join for path {}: {}",
                                relationshipPath, e.getMessage());
                            continue;
                        }
                    }

                    if (join != null) {
                        applySearchToJoin(join, dataObject, predicates, cb);
                    }
                } else {
                    log.warn("No relationship path found from {} to {}",
                        root.getJavaType().getSimpleName(), objectType);
                }
            } catch (Exception e) {
                log.warn("Error applying search for object type {}: {}", objectType, e.getMessage());
            }
        }
    }

    /**
     * Applies search criteria to the root entity
     */
    private void applySearchToRoot(
        Root<?> root,
        DataObject dataObject,
        List<Predicate> predicates,
        CriteriaBuilder cb) {

        if (dataObject == null || dataObject.getData() == null) {
            return;
        }

        TableRow data = dataObject.getData();
        Map<String, Object> fields = data.getData();

        if (fields == null || fields.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> field : fields.entrySet()) {
            String fieldName = field.getKey();
            Object value = field.getValue();

            if (fieldValidator.hasField(root.getJavaType(), fieldName)) {
                Path<?> path = root.get(fieldName);
                addEqualsPredicate(cb, predicates, path, value);
            } else {
                log.warn("Invalid search field: {} for type {}",
                    fieldName, root.getJavaType().getSimpleName());
            }
        }
    }

    /**
     * Applies search criteria to a joined entity
     */
    private void applySearchToJoin(
        Join<?, ?> join,
        DataObject dataObject,
        List<Predicate> predicates,
        CriteriaBuilder cb) {

        if (dataObject == null || dataObject.getData() == null) {
            return;
        }

        TableRow data = dataObject.getData();
        Map<String, Object> fields = data.getData();

        if (fields == null || fields.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> field : fields.entrySet()) {
            String fieldName = field.getKey();
            Object value = field.getValue();

            if (fieldValidator.hasField(join.getJavaType(), fieldName)) {
                Path<?> path = join.get(fieldName);
                addEqualsPredicate(cb, predicates, path, value);
            } else {
                log.warn("Invalid search field: {} for type {}",
                    fieldName, join.getJavaType().getSimpleName());
            }
        }
    }

    /**
     * Adds an EQUALS predicate for a field and value
     */
    private void addEqualsPredicate(
        CriteriaBuilder cb,
        List<Predicate> predicates,
        Path<?> path,
        Object value) {

        try {
            if (value == null) {
                predicates.add(cb.isNull(path));
                return;
            }

            Class<?> fieldType = path.getJavaType();
            Object convertedValue = fieldValidator.convertValue(value, fieldType);

            if (convertedValue instanceof String &&
                CommonStatus.class.isAssignableFrom(fieldType)) {
                // Handle enum conversion for status fields
                try {
                    CommonStatus status = CommonStatus.valueOf(convertedValue.toString());
                    predicates.add(cb.equal(path, status));
                    return;
                } catch (IllegalArgumentException e) {
                    // Not a valid enum value, continue with string comparison
                }
            }

            predicates.add(cb.equal(path, convertedValue));

        } catch (Exception e) {
            log.warn("Error adding equals predicate: {}", e.getMessage());
        }
    }

    /**
     * Adds a NOT_EQUALS predicate for a field and value
     */
    private void addNotEqualsPredicate(
        CriteriaBuilder cb,
        List<Predicate> predicates,
        Path<?> path,
        Object value) {

        try {
            if (value == null) {
                predicates.add(cb.isNotNull(path));
                return;
            }

            Class<?> fieldType = path.getJavaType();
            Object convertedValue = fieldValidator.convertValue(value, fieldType);

            predicates.add(cb.notEqual(path, convertedValue));

        } catch (Exception e) {
            log.warn("Error adding not equals predicate: {}", e.getMessage());
        }
    }

    /**
     * Adds a LIKE predicate for a field and value
     */
    private void addLikePredicate(
        CriteriaBuilder cb,
        List<Predicate> predicates,
        Path<String> path,
        Object value,
        LikeType likeType) {

        try {
            if (value == null || !String.class.equals(path.getJavaType())) {
                return;
            }

            String stringValue = escapeForLike(value.toString());
//            Path<String> stringPath = (Path<String>) path;

            switch (likeType) {
                case CONTAINS:
                    predicates.add(cb.like(cb.lower(path), "%" + stringValue + "%"));
                    break;
                case STARTS_WITH:
                    predicates.add(cb.like(cb.lower(path), stringValue + "%"));
                    break;
                case ENDS_WITH:
                    predicates.add(cb.like(cb.lower(path), "%" + stringValue));
                    break;
            }
        } catch (Exception e) {
            log.warn("Error adding like predicate: {}", e.getMessage());
        }
    }

    /**
     * Escapes special characters in a string for use in a LIKE expression
     * Escapes: % _ \ characters by adding a backslash before them
     */
    private String escapeForLike(String input) {
        if (input == null) {
            return null;
        }

        // Replace special characters
        return input.replaceAll("([%_\\\\])", "\\\\$1");
    }

    /**
     * Adds an IN predicate for a field and value
     */
    private void addInPredicate(
        CriteriaBuilder cb,
        List<Predicate> predicates,
        Path<?> path,
        Object value) {

        try {
            if (value == null) {
                return;
            }

            Class<?> fieldType = path.getJavaType();
            List<Object> convertedValues = Arrays.stream(value.toString().split(","))
                .map(v -> fieldValidator.convertValue(v, fieldType))
                .collect(Collectors.toList());

            predicates.add(path.in(convertedValues));

        } catch (Exception e) {
            log.warn("Error adding in predicate: {}", e.getMessage());
        }
    }

    /**
     * Gets or creates a path for a field, handling nested paths
     */
    private Path<?> getPathForField(String fieldName, Root<?> root, Map<String, Join<?, ?>> joinMap) {
        try {
            String[] parts = fieldName.split("\\.");

            if (parts.length == 1) {
                // Simple field on root entity
                if (fieldValidator.hasField(root.getJavaType(), parts[0])) {
                    return root.get(parts[0]);
                } else {
                    log.warn("Invalid field: {} for type {}",
                        parts[0], root.getJavaType().getSimpleName());
                    return null;
                }
            } else {
                // Field in a related entity - need to create joins
                String path = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
                String finalField = parts[parts.length - 1];

                Join<?, ?> join = joinHandler.createNestedJoins(root, path, joinMap, JoinType.INNER);

                if (fieldValidator.hasField(join.getJavaType(), finalField)) {
                    return join.get(finalField);
                } else {
                    log.warn("Invalid field: {} for type {}",
                        finalField, join.getJavaType().getSimpleName());
                    return null;
                }
            }
        } catch (Exception e) {
            log.warn("Error creating path for field {}: {}", fieldName, e.getMessage());
            return null;
        }
    }
}
