package vn.com.fecredit.app.service.impl.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CountQueryExecutor {
    private final EntityManager em;
    private final PredicateManager predicateManager;
    private final JoinManager joinManager;

    /**
     * Gets the total count for a query using the same criteria
     */
    public long getTotalCount(CriteriaQuery<?> originalQuery) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            
            // Get root entity from original query
            Root<?> originalRoot = originalQuery.getRoots().iterator().next();
            
            // Create new count query
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<?> root = countQuery.from(originalRoot.getJavaType());
            
            // Copy joins from original query to maintain relationships
            originalRoot.getJoins().forEach(originalJoin -> {
                root.join(originalJoin.getAttribute().getName(), originalJoin.getJoinType());
            });

            // Copy restrictions from original query
            Predicate originalRestriction = originalQuery.getRestriction();
            if (originalRestriction != null) {
                countQuery.where(originalRestriction);
            }
            
            // Set count selection with same distinct setting as original
            countQuery.select(originalQuery.isDistinct() 
                ? cb.countDistinct(root)
                : cb.count(root));
                  // Apply default filters to ensure consistency
            List<Predicate> predicates = new ArrayList<>();
            predicateManager.addDefaultFilters(cb, root, predicates);
            
            if (!predicates.isEmpty()) {
                Predicate filtersPredicate = cb.and(predicates.toArray(new Predicate[0]));
                if (originalRestriction != null) {
                    countQuery.where(cb.and(originalRestriction, filtersPredicate));
                } else {
                    countQuery.where(filtersPredicate);
                }
            }
            
            // Execute count query
            return em.createQuery(countQuery).getSingleResult();
            
        } catch (Exception e) {
            log.error("Error executing count query: {}", e.getMessage(), e);
            return 0L;
        }
    }
    
    /**
     * Counts the total number of records that match the criteria using the same
     * query structure but with more complex join handling
     *
     * @param originalQuery The data query whose structure should be used for counting
     * @return The total count of matching records
     */
    public long countTotalRecords(CriteriaQuery<Tuple> originalQuery) {
        try {
            log.debug("Creating count query from original query");
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

            // Get the root from original query and create corresponding root for count query
            Root<?> originalRoot = originalQuery.getRoots().iterator().next();
            Root<?> countRoot = countQuery.from(originalRoot.getJavaType());

            // Log original joins with full details
            Set<?> originalJoins = originalRoot.getJoins();
            logJoins("Original query joins before copying", originalJoins);

            // Initialize join maps
            Map<String, Join<?, ?>> joinMap = new HashMap<>();
            Set<String> processedJoins = new HashSet<>();

            // Copy all joins from original query including nested joins
            joinManager.copyJoinsRecursively(originalRoot, countRoot, joinMap, processedJoins);

            // Ensure we count distinct IDs to match native query behavior
            if (originalRoot.getJavaType().getSimpleName().equals("Event")) {
                // For Event entity, count distinct IDs to match native query
                countQuery.select(cb.countDistinct(countRoot.get("id")));
            } else {
                // Default count behavior
                countQuery.select(cb.countDistinct(countRoot));
            }

            // Log copied joins with full details
            Set<?> copiedJoins = countRoot.getJoins();
            logJoins("Count query joins after copying", copiedJoins);            // Copy where clause
            Predicate originalPredicate = originalQuery.getRestriction();
            if (originalPredicate != null) {
                countQuery.where(predicateManager.copyPredicate(originalPredicate, cb, countRoot, joinMap));
            }

            // Copy distinct setting
            countQuery.distinct(originalQuery.isDistinct());

            // Execute count query
            log.debug("Executing count query with following joins: {}", String.join(", ", joinMap.keySet()));
            Long total = em.createQuery(countQuery).getSingleResult();
            log.debug("Count query returned: {}", total);

            return total != null ? total : 0L;
        } catch (Exception e) {
            log.error("Error executing count query: {}", e.getMessage(), e);
            return 0L;
        }
    }
    
    /**
     * Logs joins for debugging purposes
     */
    private void logJoins(String message, Set<?> joins) {
        log.debug("{}: {}", 
            message,
            joins.stream()
                .filter(j -> j instanceof Join)
                .map(j -> {
                    Join<?, ?> join = (Join<?, ?>) j;
                    return String.format("%s(%s)", 
                            join.getAttribute().getName(), 
                            join.getJoinType().name());
                })
                .collect(Collectors.joining(", ")));
    }
}
