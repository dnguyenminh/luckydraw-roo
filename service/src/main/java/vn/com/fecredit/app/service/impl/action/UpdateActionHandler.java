package vn.com.fecredit.app.service.impl.action;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractPersistableEntity;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.factory.RepositoryFactory;
import vn.com.fecredit.app.service.impl.EntityMapperService;
import vn.com.fecredit.app.service.impl.table.EntityConverter;

/**
 * Handler for UPDATE actions
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateActionHandler implements TableActionHandler {

    @PersistenceContext
    private final EntityManager entityManager;
    
    private final RepositoryFactory repositoryFactory;
    private final EntityMapperService entityMapperService;
    private final EntityConverter entityConverter;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TableActionResponse handle(TableActionRequest request) {
        try {
            // Get the entity ID from the request data
            Map<String, Object> data = request.getData().getData();
            if (data == null || !data.containsKey("id")) {
                return TableActionResponse.error(request, "Update request must include entity ID");
            }

            Class<? extends AbstractPersistableEntity<?>> entityClass = repositoryFactory.getEntityClass(request.getObjectType());
            Class<?> idType = (Class<?>) entityConverter.getIdType(entityClass);
            Object id = objectMapper.convertValue(data.get("id"), idType);

            // Find the existing entity
            AbstractPersistableEntity<?> existingEntity = entityManager.find(entityClass, id);
            if (existingEntity == null) {
                return TableActionResponse.error(
                        request,
                        "Entity not found with ID: " + id);
            }

            // Update the entity fields
            entityMapperService.updateEntityFromData(existingEntity, request.getData());

            // Save the updated entity
            existingEntity = entityMapperService.saveEntity(existingEntity, repositoryFactory);

            // Convert the updated entity to a TableRow
            TableRow updatedRow = entityMapperService.convertEntityToTableRow(existingEntity);

            return TableActionResponse.success(
                    request,
                    "Successfully updated " + request.getObjectType() + " with ID: " + id,
                    updatedRow);
        } catch (Exception e) {
            log.error("Error processing UPDATE action", e);
            return TableActionResponse.error(request, "Failed to update entity: " + e.getMessage());
        }
    }
}
