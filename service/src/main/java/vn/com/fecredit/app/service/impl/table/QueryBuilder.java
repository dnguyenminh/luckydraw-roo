package vn.com.fecredit.app.service.impl.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.SortRequest;
import vn.com.fecredit.app.service.dto.SortType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryBuilder {
    
    private final EntityManager entityManager;
    private final PredicateBuilder predicateBuilder;
    private final JoinCreator joinCreator;

    /**
     * Builds a criteria query with all necessary filters, joins, and selections
     * 
     * @param request The table fetch request containing filters and other parameters
     * @param rootEntityClass The entity class to build the query from
     * @param <T> The type of the root entity
     * @return The built criteria query
     */
    public <T> CriteriaQuery<Tuple> buildTupleQuery(TableFetchRequest request, Class<T> rootEntityClass) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> query = cb.createTupleQuery();
            Root<T> root = query.from(rootEntityClass);

            // Handle joins based on search map and view columns
            Map<String, Join<?, ?>> joins = joinCreator.createJoinsFromSearchMapAndViewColumns(
                request.getSearch(), request.getViewColumns(), rootEntityClass);

            // Create selections based on viewColumns
            List<Selection<?>> selections = createSelections(request.getViewColumns(), root, joins);
            if (selections.isEmpty()) {
                log.error("No valid columns specified in viewColumns");
                return null;
            }

            query.multiselect(selections);

            // Apply filters and search criteria
            List<Predicate> predicates = predicateBuilder.buildPredicates(request, cb, root);
            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            // Apply sorting
            List<Order> orders = buildOrderClauses(request.getSorts(), cb, root, joins);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }

            query.distinct(true);
            return query;

        } catch (Exception e) {
            log.error("Error building criteria query", e);
            return null;
        }
    }

    /**
     * Creates selections for the query based on view columns
     */
    private List<Selection<?>> createSelections(
            List<ColumnInfo> viewColumns,
            Root<?> root,
            Map<String, Join<?, ?>> joins) {
            
        List<Selection<?>> selections = new ArrayList<>();
        
        if (viewColumns == null || viewColumns.isEmpty()) {
            return selections;
        }
        
        for (ColumnInfo column : viewColumns) {
            String fieldName = column.getFieldName();
            String alias = fieldName.replace(".", "_"); // Create unique alias
            
            try {
                String[] fieldParts = fieldName.split("\\.");
                Path<?> path = root;
                
                if (fieldParts.length > 1) {
                    // Handle joined fields (e.g., "location.name")
                    String joinName = fieldParts[0];
                    String propertyName = fieldParts[1];
                    
                    Join<?, ?> join = joins.get(joinName);
                    if (join != null) {
                        path = join.get(propertyName);
                    } else {
                        log.warn("Join not found for field: {}", fieldName);
                        continue;
                    }
                } else {
                    // Handle root entity fields (e.g., "name")
                    path = root.get(fieldName);
                }
                
                // Add the path as a selection with alias
                Selection<?> selection = path.alias(alias);
                selections.add(selection);
                
            } catch (IllegalArgumentException e) {
                log.warn("Invalid field path: {} - {}", fieldName, e.getMessage());
            }
        }
        
        // If no selections were added, add ID as fallback
        if (selections.isEmpty()) {
            try {
                selections.add(root.get("id").alias("id"));
                log.warn("Using fallback selection (id) because no valid columns were specified");
            } catch (IllegalArgumentException e) {
                log.error("Could not add fallback id selection", e);
            }
        }
        
        return selections;
    }

    /**
     * Builds order clauses for sorting results
     */
    private List<Order> buildOrderClauses(
            List<SortRequest> sorts,
            CriteriaBuilder cb,
            Root<?> root,
            Map<String, Join<?, ?>> joins) {
            
        List<Order> orders = new ArrayList<>();
        if (sorts == null || sorts.isEmpty()) {
            return orders;
        }
        
        for (SortRequest sort : sorts) {
            String fieldName = sort.getField();
            String[] fieldParts = fieldName.split("\\.");
            Path<?> path;
            
            try {
                if (fieldParts.length > 1) {
                    // Handle joined fields (e.g., "location.name")
                    String joinName = fieldParts[0];
                    String propertyName = fieldParts[1];
                    
                    Join<?, ?> join = joins.get(joinName);
                    if (join != null) {
                        path = join.get(propertyName);
                    } else {
                        log.warn("Join not found for field: {}", fieldName);
                        continue;
                    }
                } else {
                    // Handle root entity fields (e.g., "name")
                    path = root.get(fieldName);
                }
                
                // Create ascending or descending order based on sort type
                Order order = (sort.getSortType() == SortType.ASCENDING) ?
                        cb.asc(path) : cb.desc(path);
                orders.add(order);
                
            } catch (IllegalArgumentException e) {
                log.warn("Invalid sort field: {} - {}", fieldName, e.getMessage());
            }
        }
        
        return orders;
    }
}
