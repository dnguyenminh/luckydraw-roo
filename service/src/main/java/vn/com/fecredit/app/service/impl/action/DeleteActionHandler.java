package vn.com.fecredit.app.service.impl.action;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.factory.RepositoryFactory;
import vn.com.fecredit.app.service.impl.EntityMapperService;

/**
 * Handler for DELETE actions
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DeleteActionHandler implements TableActionHandler {

    @PersistenceContext
    private final EntityManager entityManager;
    
    private final RepositoryFactory repositoryFactory;
    private final EntityMapperService entityMapperService;
    private final vn.com.fecredit.app.service.impl.table.EntityManager customEntityManager;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TableActionResponse handle(TableActionRequest request) {
        try {
            // Get the entity ID from the request data
            Map<String, Object> data = request.getData().getData();
            if (data == null || !data.containsKey("id")) {
                return TableActionResponse.error(request, "Delete request must include entity ID");
            }

            // Get the entity class
            Class<? extends AbstractStatusAwareEntity<?>> entityClass = repositoryFactory.getEntityClass(request.getObjectType());
            Class<?> idType = (Class<?>) customEntityManager.getIdType(entityClass);
            Object id = objectMapper.convertValue(data.get("id"), idType);

            // Find the entity to delete
            AbstractStatusAwareEntity<?> entityToDelete = entityManager.find(entityClass, id);
            if (entityToDelete == null) {
                return TableActionResponse.error(
                        request,
                        "Entity not found with ID: " + id);
            }

            // For soft delete, we can set status to INACTIVE/DELETED instead of physically removing
            entityToDelete.setStatus(CommonStatus.DELETED);
            entityManager.merge(entityToDelete);

            return TableActionResponse.success(
                    request,
                    "Successfully deactivated " + request.getObjectType() + " with ID: " + id,
                    entityMapperService.convertEntityToTableRow(entityToDelete));
        } catch (Exception e) {
            log.error("Error processing DELETE action", e);
            return TableActionResponse.error(request, "Failed to delete entity: " + e.getMessage());
        }
    }
}
