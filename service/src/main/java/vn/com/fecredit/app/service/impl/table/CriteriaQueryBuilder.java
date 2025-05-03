package vn.com.fecredit.app.service.impl.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
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
import vn.com.fecredit.app.service.dto.TableFetchRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class CriteriaQueryBuilder {

    private final EntityManager entityManager;
    private final PredicateHandler predicateHandler;
    private final ColumnInfoProvider columnInfoProvider;
    private final JoinHandler joinHandler;

    /**
     * Builds optimized criteria query with exception handling
     */
    public CriteriaQuery<Tuple> buildCriteriaQuery(TableFetchRequest request, Class<?> rootEntityClass) {
        if (request == null || rootEntityClass == null) {
            log.error("Cannot build criteria query with null request or root entity class");
            return null;
        }

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> query = cb.createTupleQuery();

            // Create root
            Root<?> root = query.from(rootEntityClass);

            // Get columns and create selections
            List<Selection<?>> selections = createSelections(request, root);
            if (!selections.isEmpty()) {
                query.multiselect(selections);
            } else {
                // Default to ID selection if no columns specified
                Path<?> idPath = root.get("id");
                query.multiselect(idPath.alias("id"));
            }

            // Apply predicates
            List<Predicate> predicates = createPredicates(request, cb, root);
            if (!predicates.isEmpty()) {
                query.where(predicates.toArray(new Predicate[0]));
            }

            // Apply ordering
            List<Order> orders = createOrdering(request, cb, root);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }

            // Set distinct if needed
            query.distinct(true);

            return query;

        } catch (Exception e) {
            log.error("Error building criteria query: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Create selections with improved handling for nested paths
     */
    private List<Selection<?>> createSelections(TableFetchRequest request, Root<?> root) {
        List<Selection<?>> selections = new ArrayList<>();
        Map<String, Join<?, ?>> joinMap = new HashMap<>();

        try {
            // Always include ID for consistency
            try {
                Path<?> idPath = root.get("id");
                selections.add(idPath.alias("id"));
            } catch (IllegalArgumentException e) {
                log.warn("Entity {} does not have an 'id' field", root.getJavaType().getSimpleName());
            }

            // Add requested columns
            if (request.getViewColumns() != null && !request.getViewColumns().isEmpty()) {
                for (ColumnInfo column : request.getViewColumns()) {
                    String fieldName = column.getFieldName();
                    if (fieldName != null && !fieldName.equals("id")) {
                        try {
                            // Handle nested paths (e.g., "locations.region.name")
                            if (fieldName.contains(".")) {
                                addNestedPathSelection(root, fieldName, selections, joinMap);
                            } else {
                                // Simple path
                                Path<?> path = root.get(fieldName);
                                selections.add(path.alias(fieldName));
                            }
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid field: {} for type {}", 
                                fieldName, root.getJavaType().getSimpleName());
                        }
                    }
                }
            } else {
                // No columns specified, add common fields from the entity
                Map<String, ColumnInfo> allColumns = columnInfoProvider.getColumnInfo(root.getJavaType());
                for (Map.Entry<String, ColumnInfo> entry : allColumns.entrySet()) {
                    String fieldName = entry.getKey();
                    // Skip id as it's already added and skip complex objects
                    if (!fieldName.equals("id") && isPrimitiveOrStringField(root, fieldName)) {
                        try {
                            Path<?> path = root.get(fieldName);
                            selections.add(path.alias(fieldName));
                        } catch (IllegalArgumentException e) {
                            // Skip invalid fields
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error creating selections: {}", e.getMessage());
        }

        return selections;
    }

    /**
     * Add a selection for a nested path like "locations.region.name"
     */
    private void addNestedPathSelection(
            Root<?> root, 
            String fieldPath, 
            List<Selection<?>> selections, 
            Map<String, Join<?, ?>> joinMap) {
        
        try {
            String[] parts = fieldPath.split("\\.");
            if (parts.length <= 1) {
                return;
            }
            
            // All parts except the last one constitute the join path
            String joinPath = String.join(".", Arrays.copyOf(parts, parts.length - 1));
            String finalAttribute = parts[parts.length - 1];
            
            // Create joins for the path
            Join<?, ?> join = joinHandler.createNestedJoins(root, joinPath, joinMap, JoinType.LEFT);
            
            if (join != null) {
                try {
                    // Get the attribute from the final join
                    Path<?> attributePath = join.get(finalAttribute);
                    
                    // Convert dots to underscores in the alias
                    String alias = fieldPath.replace('.', '_');
                    
                    // Use the converted alias for consistent access
                    selections.add(attributePath.alias(alias));
                    
                    log.debug("Added nested path selection: {} with alias {}", fieldPath, alias);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid field: {} on join {}", 
                        finalAttribute, join.getJavaType().getSimpleName());
                }
            } else {
                log.warn("Could not create join path for field: {}", fieldPath);
            }
        } catch (Exception e) {
            log.warn("Error adding nested path selection for {}: {}", fieldPath, e.getMessage());
        }
    }

    /**
     * Check if a field is a primitive type or string (suitable for default selection)
     */
    private boolean isPrimitiveOrStringField(Root<?> root, String fieldName) {
        try {
            Class<?> type = root.get(fieldName).getJavaType();
            return type.isPrimitive() || 
                   type == String.class || 
                   Number.class.isAssignableFrom(type) ||
                   type == Boolean.class ||
                   type == Character.class ||
                   type.isEnum() ||
                   java.util.Date.class.isAssignableFrom(type) ||
                   java.time.temporal.Temporal.class.isAssignableFrom(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create predicates with better error handling
     */
    private List<Predicate> createPredicates(TableFetchRequest request, CriteriaBuilder cb, Root<?> root) {
        List<Predicate> predicates = new ArrayList<>();

        try {
            // Add default filters
            predicateHandler.addDefaultFilters(cb, root, predicates);

            // Apply specific filters
            predicateHandler.applyFilters(request, predicates, cb, root);

            // Apply search criteria
            predicateHandler.applySearch(request, predicates, cb, root);

        } catch (Exception e) {
            log.error("Error creating predicates: {}", e.getMessage());
        }

        return predicates;
    }

    /**
     * Create ordering with better error handling
     */
    private List<Order> createOrdering(TableFetchRequest request, CriteriaBuilder cb, Root<?> root) {
        List<Order> orders = new ArrayList<>();

        if (request.getSorts() != null && !request.getSorts().isEmpty()) {
            request.getSorts().forEach(sort -> {
                try {
                    if (sort.getField() != null) {
                        Path<?> path = root.get(sort.getField());

                        if (sort.getSortType() != null && 
                            sort.getSortType() == vn.com.fecredit.app.service.dto.SortType.DESCENDING) {
                            orders.add(cb.desc(path));
                        } else {
                            orders.add(cb.asc(path));
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid sort field: {} for type {}", 
                        sort.getField(), root.getJavaType().getSimpleName());
                }
            });
        }

        // Add ID sorting as fallback for stable results
        if (orders.isEmpty()) {
            try {
                Path<?> idPath = root.get("id");
                orders.add(cb.asc(idPath));
            } catch (IllegalArgumentException e) {
                // Entity doesn't have ID field, just continue
            }
        }

        return orders;
    }
}