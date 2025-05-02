package vn.com.fecredit.app.service.impl.table;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CountQueryExecutor {

    private final EntityManager em;
    private final PredicateBuilder predicateBuilder;

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
            predicateBuilder.addDefaultFilters(cb, root, predicates);
            
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
}
