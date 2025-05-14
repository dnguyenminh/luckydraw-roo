package vn.com.fecredit.app.service.impl.action;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.EntityManager;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.factory.RepositoryFactory;
import vn.com.fecredit.app.service.impl.EntityMapperService;
import vn.com.fecredit.app.service.impl.FileProcessingService;
import vn.com.fecredit.app.service.impl.TableDataServiceImpl;

/**
 * Handler for IMPORT actions
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ImportActionHandler implements TableActionHandler {

    private final EntityManager entityManager;
    private final RepositoryFactory repositoryFactory;
    private final FileProcessingService fileProcessingService;
    private final TableDataServiceImpl tableDataService;
    private final EntityMapperService entityMapperService;

    @Override
    @Transactional
    public TableActionResponse handle(TableActionRequest request) {
        try {
            return fileProcessingService.processImportData(
                request,
                entityManager,
                repositoryFactory,
                tableDataService,
                entityMapperService
            );
        } catch (Exception e) {
            log.error("Error processing IMPORT action", e);
            return TableActionResponse.error(request, "Failed to import data: " + e.getMessage());
        }
    }
}
