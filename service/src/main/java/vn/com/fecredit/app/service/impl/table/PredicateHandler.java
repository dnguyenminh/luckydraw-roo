package vn.com.fecredit.app.service.impl.table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // private final EntityManager entityManager;
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

        // Process each filter and add it as a where condition
        for (FilterRequest filter : request.getFilters()) {
            try {
                addFilterPredicate(cb, root, predicates, filter);
            } catch (Exception e) {
                log.warn("Error applying filter: {}", e.getMessage());
            }
        }
    }

    /**
     * Adds a predicate for a filter directly to the where conditions
     */
    @SuppressWarnings("unchecked")
    private <T extends Comparable<? super T>> void addFilterPredicate(
            CriteriaBuilder cb,
            Root<?> root,
            List<Predicate> predicates,
            FilterRequest filter) {

        try {
            // Get the appropriate path for this field without creating unnecessary joins
            String fieldName = filter.getField();
            FilterType type = filter.getFilterType();
            Path<?> path = root.get(fieldName);
            Class<?> fieldType = path.getJavaType();

            switch (type) {
                case EQUALS: {
                    Object convertedValue = fieldValidator.convertValue(filter.getMinValue(), fieldType);
                    if (convertedValue == null) {
                        predicates.add(cb.isNull(path));
                    } else {
                        predicates.add(cb.equal(path, convertedValue));
                    }
                    break;
                }
                case NOT_EQUALS: {
                    Object convertedValue = fieldValidator.convertValue(filter.getMinValue(), fieldType);
                    if (convertedValue == null) {
                        predicates.add(cb.isNotNull(path));
                    } else {
                        predicates.add(cb.notEqual(path, convertedValue));
                    }
                    break;
                }
                case GREATER_THAN: {
                    Object convertedValue = fieldValidator.convertValue(filter.getMinValue(), fieldType);
                    // @SuppressWarnings("unchecked")
                    predicates.add(cb.greaterThan((Path<T>) path.as(fieldType), (T) convertedValue));
                    break;
                }
                case GREATER_THAN_OR_EQUALS: {
                    Object convertedValue = fieldValidator.convertValue(filter.getMinValue(), fieldType);
                    if (convertedValue != null && Comparable.class.isAssignableFrom(fieldType)) {
                        predicates.add(cb.greaterThanOrEqualTo(
                                (Path<T>) path.as(fieldType),
                                (T) convertedValue));
                    }
                    break;
                }
                case LESS_THAN: {
                    Object convertedValue = fieldValidator.convertValue(filter.getMinValue(), fieldType);
                    if (convertedValue != null && Comparable.class.isAssignableFrom(fieldType)) {
                        predicates.add(cb.lessThan(
                                (Path<T>) path.as(fieldType),
                                (T) convertedValue));
                    }
                    break;
                }
                case LESS_THAN_OR_EQUALS: {
                    Object convertedValue = fieldValidator.convertValue(filter.getMinValue(), fieldType);
                    if (convertedValue != null && Comparable.class.isAssignableFrom(fieldType)) {
                        predicates.add(cb.lessThanOrEqualTo(
                                (Path<T>) path.as(fieldType),
                                (T) convertedValue));
                    }
                    break;
                }
                case CONTAINS: {
                    Object convertedValue = fieldValidator.convertValue(filter.getMinValue(), fieldType);
                    if (convertedValue != null && String.class.equals(fieldType)) {
                        predicates.add(cb.like(
                                cb.lower(path.as(String.class)),
                                "%" + convertedValue.toString().toLowerCase() + "%"));
                    }
                    break;
                }
                // case STARTS_WITH:
                // if (convertedValue != null && String.class.equals(fieldType)) {
                // predicates.add(cb.like(
                // cb.lower(path.as(String.class)),
                // convertedValue.toString().toLowerCase() + "%"));
                // }
                // break;
                // case ENDS_WITH:
                // if (convertedValue != null && String.class.equals(fieldType)) {
                // predicates.add(cb.like(
                // cb.lower(path.as(String.class)),
                // "%" + convertedValue.toString().toLowerCase()));
                // }
                // break;
                case IN: {
                    String value = filter.getMinValue();
                    List<Object> convertedValue = Arrays.asList(value.split(",")).stream().map(
                            v -> fieldValidator.convertValue(v, fieldType)).collect(Collectors.toList());
                    predicates.add(path.in((List<?>) convertedValue));
                    break;
                }
                case BETWEEN: {
                    // Handle BETWEEN with array or range values
                    Object minvalue = fieldValidator.convertValue(filter.getMinValue(), fieldType);
                    Object maxvalue = fieldValidator.convertValue(filter.getMaxValue(), fieldType);
                    predicates.add(cb.between(
                            (Path<T>) path.as(fieldType),
                            (T) minvalue,
                            (T) maxvalue));
                    break;
                }
                default:
                    log.warn("Unsupported filter type: {}", type);
            }
        } catch (Exception e) {
            log.warn("Error adding predicate: {}", e.getMessage());
        }
    }

    // /**
    //  * Gets or creates a path for a field, handling nested paths
    //  */
    // private Path<?> getPathForField(String fieldName, Root<?> root, Map<String, Join<?, ?>> joinMap) {
    //     try {
    //         String[] parts = fieldName.split("\\.");

    //         if (parts.length == 1) {
    //             // Simple field on root entity
    //             if (fieldValidator.hasField(root.getJavaType(), parts[0])) {
    //                 return root.get(parts[0]);
    //             } else {
    //                 log.warn("Invalid field: {} for type {}",
    //                         parts[0], root.getJavaType().getSimpleName());
    //                 return null;
    //             }
    //         } else {
    //             // Field in a related entity - need to create joins
    //             String path = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
    //             String finalField = parts[parts.length - 1];

    //             Join<?, ?> join = joinHandler.createNestedJoins(root, path, joinMap, JoinType.LEFT);

    //             if (fieldValidator.hasField(join.getJavaType(), finalField)) {
    //                 return join.get(finalField);
    //             } else {
    //                 log.warn("Invalid field: {} for type {}",
    //                         finalField, join.getJavaType().getSimpleName());
    //                 return null;
    //             }
    //         }
    //     } catch (Exception e) {
    //         log.warn("Error creating path for field {}: {}", fieldName, e.getMessage());
    //         return null;
    //     }
    // }

    /**
     * Applies search criteria for related entities
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
        Map<String, Join<?, ?>> joinMap = joinHandler.createJoinsForSearchCriteria(root, searchCriteria);

        // Apply search criteria by object type
        for (Map.Entry<ObjectType, DataObject> entry : searchCriteria.entrySet()) {
            ObjectType objectType = entry.getKey();
            DataObject dataObject = entry.getValue();

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
                        join = joinHandler.createNestedJoins(root, relationshipPath, joinMap, JoinType.LEFT);
                    } catch (Exception e) {
                        log.warn("Failed to create join for path {}: {}", relationshipPath, e.getMessage());
                        continue;
                    }
                }

                if (join != null) {
                    applySearchToJoin(join, dataObject, predicates, cb);
                }
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
}
