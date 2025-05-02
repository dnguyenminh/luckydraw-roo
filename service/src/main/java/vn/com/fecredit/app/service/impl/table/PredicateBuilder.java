package vn.com.fecredit.app.service.impl.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.FilterRequest;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class PredicateBuilder {

    // Add a field to track which fields have already been filtered
    private final Set<String> filteredFields = new HashSet<>();

    /**
     * Builds all predicates for filtering and search
     */
    public List<Predicate> buildPredicates(
            TableFetchRequest request, CriteriaBuilder cb, Root<?> root) {
        // Clear filtered fields tracking for new query
        filteredFields.clear();
        
        List<Predicate> predicates = new ArrayList<>();

        // First apply specific filters from request (these take precedence)
        applyFilters(request, predicates, cb, root);

        // Then apply search criteria 
        applySearch(request, predicates, cb, root);
        
        // Finally add default filters, but only for fields not already filtered
        addDefaultFilters(cb, root, predicates);

        return predicates;
    }

    /**
     * Adds default filters like excluding deleted records
     * Only applies default filters to fields that don't already have filters
     */
    public void addDefaultFilters(CriteriaBuilder cb, Root<?> root, List<Predicate> predicates) {
        // Add status filter only if not already filtered
        if (EntityConverter.containField(root.getJavaType(), "status") && !filteredFields.contains("status")) {
            predicates.add(cb.notEqual(root.get("status"), "DELETED"));
            log.debug("Added default status filter because field wasn't already filtered");
        }

        // Add active filter only if not already filtered
        if (EntityConverter.containField(root.getJavaType(), "active") && !filteredFields.contains("active")) {
            predicates.add(cb.isTrue(root.get("active")));
            log.debug("Added default active filter because field wasn't already filtered");
        }
    }

    /**
     * Applies filters from the request
     */
    public void applyFilters(
            TableFetchRequest request,
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Root<?> root) {

        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            for (FilterRequest filter : request.getFilters()) {
                String fieldName = filter.getField();
                if (fieldName != null) {
                    // Track that we're filtering this field
                    filteredFields.add(fieldName);
                    log.debug("Adding explicit filter for field: {}", fieldName);
                }
                addFilterPredicate(predicates, cb, root, filter);
            }
        }
    }

    /**
     * Add a predicate for a single filter
     */
    private void addFilterPredicate(
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Root<?> root,
            FilterRequest filter) {

        if (filter.getField() == null || filter.getFilterType() == null) {
            return;
        }

        try {
            switch (filter.getFilterType()) {
                case EQUALS:
                    predicates.add(cb.equal(root.get(filter.getField()), filter.getMinValue()));
                    break;
                case NOT_EQUALS:
                    predicates.add(cb.notEqual(root.get(filter.getField()), filter.getMinValue()));
                    break;
                case GREATER_THAN:
                    predicates.add(cb.greaterThan(root.get(filter.getField()), filter.getMinValue()));
                    break;
                case GREATER_THAN_OR_EQUALS:
                    predicates.add(cb.greaterThanOrEqualTo(root.get(filter.getField()), filter.getMinValue()));
                    break;
                case LESS_THAN:
                    predicates.add(cb.lessThan(root.get(filter.getField()), filter.getMinValue()));
                    break;
                case LESS_THAN_OR_EQUALS:
                    predicates.add(cb.lessThanOrEqualTo(root.get(filter.getField()), filter.getMinValue()));
                    break;
                case BETWEEN:
                    if (filter.getMaxValue() != null) {
                        predicates.add(cb.between(
                                root.get(filter.getField()),
                                filter.getMinValue(),
                                filter.getMaxValue()));
                    }
                    break;
                case CONTAINS:
                    predicates.add(cb.like(
                            cb.lower(root.get(filter.getField()).as(String.class)),
                            "%" + filter.getMinValue().toLowerCase() + "%"));
                    break;
                case IN:
                    if (filter.getMinValue() != null && filter.getMinValue().contains(",")) {
                        predicates.add(root.get(filter.getField()).in(
                                (Object[]) filter.getMinValue().split(",")));
                    }
                    break;
                case NOT_IN:
                    if (filter.getMinValue() != null && filter.getMinValue().contains(",")) {
                        predicates.add(cb.not(root.get(filter.getField()).in(
                                (Object[]) filter.getMinValue().split(","))));
                    }
                    break;
                default:
                    log.warn("Unsupported filter type: {}", filter.getFilterType());
            }
        } catch (Exception e) {
            log.error("Error applying filter for field {}: {}", filter.getField(), e.getMessage());
        }
    }

    /**
     * Apply search criteria from request to predicates
     */
    public void applySearch(TableFetchRequest request, List<Predicate> predicates, CriteriaBuilder cb, Root<?> root) {
        if (request.getSearch() == null || request.getSearch().isEmpty()) {
            return;
        }

        for (Map.Entry<ObjectType, DataObject> entry : request.getSearch().entrySet()) {
            DataObject dataObject = entry.getValue();
            if (dataObject == null || dataObject.getData() == null || dataObject.getData().getData() == null) {
                continue;
            }

            for (Map.Entry<String, Object> field : dataObject.getData().getData().entrySet()) {
                String fieldName = field.getKey();
                Object value = field.getValue();

                // Skip if this field already has a filter
                if (filteredFields.contains(fieldName)) {
                    log.debug("Skipping search condition for field '{}' as it already has a filter", fieldName);
                    continue;
                }

                try {
                    // Try to create a predicate for the field
                    Predicate predicate = createSearchPredicate(cb, root, fieldName, value);
                    if (predicate != null) {
                        predicates.add(predicate);
                        filteredFields.add(fieldName); // Track that we've filtered this field
                        log.debug("Added search predicate for field: {}", fieldName);
                    }
                } catch (Exception e) {
                    log.warn("Error creating search predicate for {}: {}", fieldName, e.getMessage());
                }
            }
        }
    }

    /**
     * Creates a predicate for a search condition based on field type
     */
    private Predicate createSearchPredicate(CriteriaBuilder cb, Root<?> root, String fieldName, Object value) {
        if (value == null) {
            return null;
        }

        try {
            Path<?> path = getPath(root, fieldName);
            if (path == null) {
                return null;
            }

            Class<?> fieldType = path.getJavaType();

            if (String.class.equals(fieldType)) {
                // For string fields, use case-insensitive LIKE
                return cb.like(cb.lower(path.as(String.class)),
                        "%" + value.toString().toLowerCase() + "%");
            } else if (Number.class.isAssignableFrom(fieldType)) {
                // For numeric fields, use equality
                return cb.equal(path, value);
            } else if (Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) {
                // For boolean fields
                return cb.equal(path, Boolean.valueOf(value.toString()));
            } else if (java.util.Date.class.isAssignableFrom(fieldType) ||
                    java.time.temporal.Temporal.class.isAssignableFrom(fieldType)) {
                // For date fields - this would need more complex handling for ranges
                return cb.equal(path, value);
            } else if (Enum.class.isAssignableFrom(fieldType)) {
                // For enum fields
                @SuppressWarnings({"unchecked", "rawtypes"})
                Enum<?> enumValue = Enum.valueOf((Class<Enum>) fieldType, value.toString());
                return cb.equal(path, enumValue);
            } else {
                // Default to equality for other types
                return cb.equal(path, value);
            }
        } catch (Exception e) {
            log.warn("Could not create predicate for field {}: {}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * Gets a path for a potentially nested field name
     */
    private Path<?> getPath(Root<?> root, String fieldName) {
        try {
            if (!fieldName.contains(".")) {
                return root.get(fieldName);
            }

            // Handle dot-notation for nested fields
            String[] parts = fieldName.split("\\.");
            Path<?> path = root;

            for (String part : parts) {
                path = path.get(part);
            }

            return path;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid field path: {}", fieldName);
            return null;
        }
    }
}
