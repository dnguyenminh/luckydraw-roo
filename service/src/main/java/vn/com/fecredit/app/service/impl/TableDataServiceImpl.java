package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.ObjectType;
import vn.com.fecredit.app.dto.SortRequest;
import vn.com.fecredit.app.dto.TableFetchRequest;
import vn.com.fecredit.app.dto.TableFetchResponse;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.dto.SortType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the TableDataService interface for handling data tables,
 * matching the CommonAPIRequestAndResponse.puml specification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableDataServiceImpl implements TableDataService {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public TableFetchResponse fetchTableData(TableFetchRequest request) {
        try {
            Class<?> entityClass = determineEntityClass(request);
            JpaRepository<?, Long> repository = getRepository(entityClass);
            
            // Create pageable with sorting
            Pageable pageable = createPageable(request);
            
            // Create filter specifications
            Specification<?> spec = createSpecifications(request, entityClass);
            
            // Execute query
            Page<?> page = executeQuery(repository, spec, pageable);
            
            // Convert result to response
            List<Map<String, Object>> data = page.getContent().stream()
                    .map(this::convertEntityToMap)
                    .collect(Collectors.toList());
            
            return TableFetchResponse.builder()
                    .data(data)
                    .totalRows(page.getTotalElements())
                    .pageCount(page.getTotalPages())
                    .currentPage(page.getNumber())
                    .metadata(new HashMap<>())
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching table data", e);
            return TableFetchResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .data(Collections.emptyList())
                    .build();
        }
    }

    @Override
    public TableFetchResponse fetchRelatedTableData(
            String entityName,
            Long entityId,
            String relationName,
            TableFetchRequest request) {
        
        try {
            // Get the parent entity
            Class<?> parentEntityClass = determineEntityClassByName(entityName);
            Object parentEntity = entityManager.find(parentEntityClass, entityId);
            
            if (parentEntity == null) {
                return TableFetchResponse.builder()
                        .data(Collections.emptyList())
                        .totalRows(0)
                        .pageCount(0)
                        .currentPage(request.getPage())
                        .success(true)
                        .build();
            }
            
            // Get the related entities through reflection
            List<?> relatedEntities = getRelatedEntities(parentEntity, relationName);
            
            // Apply paging
            int pageSize = request.getSize() > 0 ? request.getSize() : 
                           (request.getPageSize() > 0 ? request.getPageSize() : 10);
            int page = request.getPage();
            int fromIndex = page * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, relatedEntities.size());
            
            if (fromIndex >= relatedEntities.size()) {
                fromIndex = 0;
                page = 0;
            }
            
            List<?> pagedEntities = fromIndex < relatedEntities.size() ? 
                                    relatedEntities.subList(fromIndex, toIndex) : 
                                    Collections.emptyList();
            
            // Convert to maps for the response
            List<Map<String, Object>> data = pagedEntities.stream()
                    .map(this::convertEntityToMap)
                    .collect(Collectors.toList());
            
            // Calculate pagination info
            int pageCount = (int) Math.ceil((double) relatedEntities.size() / pageSize);
            
            return TableFetchResponse.builder()
                    .data(data)
                    .totalRows(relatedEntities.size())
                    .pageCount(pageCount)
                    .currentPage(page)
                    .metadata(new HashMap<>())
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching related table data", e);
            return TableFetchResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .data(Collections.emptyList())
                    .build();
        }
    }
    
    /**
     * Determine the entity class based on the request.
     */
    private Class<?> determineEntityClass(TableFetchRequest request) {
        if (request.getObjectType() != null) {
            return getEntityClassFromObjectType(request.getObjectType());
        } else if (request.getEntityName() != null && !request.getEntityName().isEmpty()) {
            return determineEntityClassByName(request.getEntityName());
        }
        
        throw new IllegalArgumentException("Missing objectType or entityName in request");
    }
    
    /**
     * Get entity class based on ObjectType enum.
     */
    private Class<?> getEntityClassFromObjectType(ObjectType objectType) {
        String packageName = "vn.com.fecredit.app.entity";
        String className = objectType.name().charAt(0) + objectType.name().substring(1).toLowerCase();
        
        try {
            return Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException e) {
            log.error("Failed to find entity class for ObjectType: {}", objectType, e);
            throw new IllegalArgumentException("Invalid objectType: " + objectType);
        }
    }
    
    /**
     * Determine entity class by name string.
     */
    private Class<?> determineEntityClassByName(String entityName) {
        String packageName = "vn.com.fecredit.app.entity";
        String className = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
        
        try {
            return Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException e) {
            log.error("Failed to find entity class for name: {}", entityName, e);
            throw new IllegalArgumentException("Invalid entity name: " + entityName);
        }
    }
    
    /**
     * Get the appropriate repository for the entity class.
     */
    @SuppressWarnings("unchecked")
    private JpaRepository<?, Long> getRepository(Class<?> entityClass) {
        // This would normally be implemented with a map of entity classes to repositories
        // For now, we can use reflection to find and instantiate the repository
        throw new UnsupportedOperationException("Repository lookup not implemented yet");
    }
    
    /**
     * Create pageable object with sorting from the request.
     */
    private Pageable createPageable(TableFetchRequest request) {
        int pageSize = request.getSize() > 0 ? request.getSize() : 
                       (request.getPageSize() > 0 ? request.getPageSize() : 10);
        int page = request.getPage();
        
        // Create sort objects
        Sort sort = Sort.unsorted();
        
        if (request.getSorts() != null && !request.getSorts().isEmpty()) {
            List<Sort.Order> orders = request.getSorts().stream()
                    .filter(sortRequest -> sortRequest.getType() != SortType.NONE)
                    .map(sortRequest -> 
                        sortRequest.getType() == SortType.ASCENDING ? 
                            Sort.Order.asc(sortRequest.getField()) : 
                            Sort.Order.desc(sortRequest.getField())
                    )
                    .collect(Collectors.toList());
            
            if (!orders.isEmpty()) {
                sort = Sort.by(orders);
            }
        } else if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            // Legacy sorting
            List<Sort.Order> orders = request.getSortBy().stream()
                    .map(field -> request.isAscending() ? 
                            Sort.Order.asc(field) : 
                            Sort.Order.desc(field))
                    .collect(Collectors.toList());
            
            sort = Sort.by(orders);
        }
        
        return PageRequest.of(page, pageSize, sort);
    }
    
    /**
     * Create filter specifications from request.
     */
    private Specification<?> createSpecifications(TableFetchRequest request, Class<?> entityClass) {
        // Simple implementation for now
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }
    
    /**
     * Execute query with repository and specifications.
     */
    @SuppressWarnings("unchecked")
    private <T> Page<T> executeQuery(JpaRepository<?, Long> repository, Specification<?> spec, Pageable pageable) {
        // This would be implemented to use the appropriate repository methods
        throw new UnsupportedOperationException("Query execution not implemented yet");
    }
    
    /**
     * Create response DTO from page result.
     */
    private TableFetchResponse createResponseFromPage(Page<?> page, TableFetchRequest request) {
        List<Map<String, Object>> data = page.getContent().stream()
                .map(this::convertEntityToMap)
                .collect(Collectors.toList());
        
        return TableFetchResponse.builder()
                .data(data)
                .totalRows(page.getTotalElements())
                .pageCount(page.getTotalPages())
                .currentPage(page.getNumber())
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * Convert an entity object to a map.
     */
    private Map<String, Object> convertEntityToMap(Object entity) {
        // This would be implemented to convert entity to map
        // For now, just return a simple representation
        if (entity instanceof AbstractStatusAwareEntity) {
            AbstractStatusAwareEntity statusAwareEntity = (AbstractStatusAwareEntity) entity;
            Map<String, Object> map = new HashMap<>();
            map.put("id", statusAwareEntity.getId());
            map.put("status", statusAwareEntity.getStatus());
            map.put("createdAt", statusAwareEntity.getCreatedAt());
            map.put("updatedAt", statusAwareEntity.getUpdatedAt());
            return map;
        }
        
        return new HashMap<>();
    }
    
    /**
     * Get related entities from a parent entity through reflection.
     */
    private List<?> getRelatedEntities(Object parentEntity, String relationName) {
        try {
            // Try to get the getter method for the relation
            String methodName = "get" + relationName.substring(0, 1).toUpperCase() + relationName.substring(1);
            java.lang.reflect.Method getter = parentEntity.getClass().getMethod(methodName);
            
            Object result = getter.invoke(parentEntity);
            if (result instanceof Collection<?>) {
                return new ArrayList<>((Collection<?>) result);
            } else if (result != null) {
                return Collections.singletonList(result);
            }
        } catch (Exception e) {
            log.error("Failed to get related entities for relation: {}", relationName, e);
        }
        
        return Collections.emptyList();
    }
}
