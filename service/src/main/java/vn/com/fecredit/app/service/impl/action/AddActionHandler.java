package vn.com.fecredit.app.service.impl.action;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractPersistableEntity;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.factory.RepositoryFactory;
import vn.com.fecredit.app.service.impl.EntityMapperService;

/**
 * Handler for ADD actions
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AddActionHandler implements TableActionHandler {

    private final RepositoryFactory repositoryFactory;
    private final EntityMapperService entityMapperService;

    @Override
    @Transactional
    public TableActionResponse handle(TableActionRequest request) {
        try {
            // Get the entity class
            Class<? extends AbstractPersistableEntity<?>> entityClass = repositoryFactory.getEntityClass(request.getObjectType());

            // Create a new instance of the entity
            AbstractPersistableEntity<?> entity = entityMapperService.createEntityFromData(request.getData(), entityClass);

            // Save the entity
            entity = entityMapperService.saveEntity(entity, repositoryFactory);

            // Convert the saved entity to a TableRow
            TableRow savedRow = entityMapperService.convertEntityToTableRow(entity);

            return TableActionResponse.success(
                    request,
                    "Successfully created " + request.getObjectType() + " with ID: " + entity.getId(),
                    savedRow);
        } catch (Exception e) {
            log.error("Error processing ADD action", e);
            return TableActionResponse.error(request, "Failed to add entity: " + e.getMessage());
        }
    }
}
