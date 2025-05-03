package vn.com.fecredit.app.service.impl.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryHandler {

    private final JoinHandler joinHandler;
    private final PredicateHandler predicateHandler;
    private final FieldValidator fieldValidator;
    private final EntityHandler entityHandler;

    /**
     * Creates a selection query based on the request parameters
     */
    public <T> CriteriaQuery<T> createSelectionQuery(
            CriteriaBuilder cb,
            Class<T> resultType,
            CriteriaQuery<T> query,
            TableFetchRequest request) {

        try {
            Root<?> root = query.from(entityHandler.resolveEntityClass(null, request.getObjectType()));

            // Get joins from search criteria
            Map<String, Join<?, ?>> joinMap = joinHandler.createJoinsForSearchCriteria(
                    root, request.getSearch());

            // Apply projections/selections
            List<Selection<?>> selections = createSelections(request.getViewColumns(), root, joinMap);
            if (!selections.isEmpty()) {
                query.multiselect(selections);
            }

            // Build predicates
            List<Predicate> predicates = buildPredicates(request, cb, root);
            if (!predicates.isEmpty()) {
                query.where(predicates.toArray(new Predicate[0]));
            }

            // Apply sorting
            List<Order> orders = buildOrderClauses(request.getSorts(), cb, root, joinMap);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }

            // Apply distinct if needed (especially for queries with joins)
            boolean hasJoins = !joinMap.isEmpty();
            if (hasJoins) {
                query.distinct(true);
            }

            return query;

        } catch (Exception e) {
            log.error("Error creating selection query: {}", e.getMessage(), e);
            throw new RuntimeException("Could not create selection query", e);
        }
    }

    /**
     * Creates selections based on view columns
     */
    private List<Selection<?>> createSelections(
            List<ColumnInfo> viewColumns,
            Root<?> root,
            Map<String, Join<?, ?>> joinMap) {

        List<Selection<?>> selections = new ArrayList<>();

        // If no columns specified, add ID as default selection
        if (viewColumns == null || viewColumns.isEmpty()) {
            if (fieldValidator.hasField(root.getJavaType(), "id")) {
                selections.add(root.get("id").alias("id"));
            }
            return selections;
        }

        // Add ID first if not already included
        boolean hasIdColumn = viewColumns.stream()
                .anyMatch(col -> "id".equals(col.getFieldName()));

        if (!hasIdColumn && fieldValidator.hasField(root.getJavaType(), "id")) {
            selections.add(root.get("id").alias("id"));
        }

        // Process each view column
        for (ColumnInfo column : viewColumns) {
            String fieldName = column.getFieldName();

            try {
                Path<?> path = getPathForField(fieldName, root, joinMap);
                if (path != null) {
                    selections.add(path.alias(fieldName));
                }
            } catch (Exception e) {
                log.warn("Could not create selection for {}: {}", fieldName, e.getMessage());
            }
        }

        return selections;
    }

    /**
     * Gets a path for a field, handling nested paths
     */
    private Path<?> getPathForField(
            String fieldName,
            Root<?> root,
            Map<String, Join<?, ?>> joinMap) {

        String[] parts = fieldName.split("\\.");

        if (parts.length == 1) {
            // Simple field directly on root entity
            if (fieldValidator.hasField(root.getJavaType(), parts[0])) {
                return root.get(parts[0]);
            } else {
                log.warn("Invalid field: {} for type {}",
                        parts[0], root.getJavaType().getSimpleName());
                return null;
            }
        } else {
            // Nested path that requires joins
            String path = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
            String finalField = parts[parts.length - 1];

            try {
                Join<?, ?> join = joinHandler.createNestedJoins(root, path, joinMap, JoinType.INNER);

                if (fieldValidator.hasField(join.getJavaType(), finalField)) {
                    return join.get(finalField);
                } else {
                    log.warn("Invalid field: {} for type {}",
                            finalField, join.getJavaType().getSimpleName());
                    return null;
                }
            } catch (Exception e) {
                log.warn("Could not resolve path for field {}: {}", fieldName, e.getMessage());
                return null;
            }
        }
    }

    /**
     * Builds order clauses for sorting
     */
    private List<Order> buildOrderClauses(
            List<SortRequest> sorts,
            CriteriaBuilder cb,
            Root<?> root,
            Map<String, Join<?, ?>> joinMap) {

        List<Order> orders = new ArrayList<>();

        if (sorts == null || sorts.isEmpty()) {
            return orders;
        }

        for (SortRequest sort : sorts) {
            if (sort.getField() == null) {
                continue;
            }

            try {
                // Get path for the sort field
                Path<?> path = getPathForField(sort.getField(), root, joinMap);

                if (path != null) {
                    // Create the appropriate order clause
                    Order order = sort.getSortType() == SortType.ASCENDING
                            ? cb.asc(path)
                            : cb.desc(path);

                    orders.add(order);
                }
            } catch (Exception e) {
                log.warn("Could not create order clause for {}: {}",
                        sort.getField(), e.getMessage());
            }
        }

        return orders;
    }

    /**
     * Builds predicates for where clause
     */
    private List<Predicate> buildPredicates(
            TableFetchRequest request,
            CriteriaBuilder cb,
            Root<?> root) {

        List<Predicate> predicates = new ArrayList<>();

        // Add default filters (e.g., exclude deleted records)
        predicateHandler.addDefaultFilters(cb, root, predicates);

        // Apply explicit filters from request
        predicateHandler.applyFilters(request, predicates, cb, root);

        // Apply search criteria for related entities
        predicateHandler.applySearch(request, predicates, cb, root);

        return predicates;
    }
}


