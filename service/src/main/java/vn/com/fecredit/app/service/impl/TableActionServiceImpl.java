package vn.com.fecredit.app.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.TableActionService;
import vn.com.fecredit.app.service.ImportFileProcessor;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.ProgressCallback;
import vn.com.fecredit.app.service.impl.action.TableActionFactory;
import vn.com.fecredit.app.service.impl.action.TableActionHandler;
import vn.com.fecredit.app.service.dto.ImportError;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * Implementation of the TableActionService for processing table actions.
 * Uses factory method pattern to delegate to specialized handlers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TableActionServiceImpl implements TableActionService {

    private final TableActionFactory actionFactory;
    private final ImportFileProcessor importFileProcessor; // Add this dependency

    /**
     * Process import file with progress tracking
     * 
     * @param request          The import request
     * @param progressCallback Optional callback for reporting progress
     * @return Response with import results
     */
    private TableActionResponse processImport(TableActionRequest request, ProgressCallback progressCallback) {
        if (request == null) {
            return TableActionResponse.error(null, "Request cannot be null");
        }

        String filePath = request.getFilePath();
        ObjectType objectType = request.getObjectType();

        try {
            // Get total records to import
            int totalRecords = importFileProcessor.countExcelRows(filePath);

            AtomicInteger processedRows = new AtomicInteger(0);

            // Create validation callback that reports progress
            BiFunction<Integer, ImportError, Boolean> validationCallback = (rowNum, error) -> {
                // Report progress every 5 rows or for rows with errors
                if (rowNum % 5 == 0 || error != null) {
                    if (progressCallback != null) {
                        progressCallback.onProgress(processedRows.incrementAndGet(), totalRecords);
                    }
                }

                // Continue validation even if there are errors
                return true;
            };

            // Validate file with the progress-reporting callback
            importFileProcessor.validateFile(filePath, objectType, validationCallback);

            // ...existing import processing code...

        } catch (Exception e) {
            log.error("Error processing import", e);
            return TableActionResponse.error(request, "Error processing import: " + e.getMessage());
        }

        // Fix: Add null as third parameter for TableRow
        return TableActionResponse.success(request, "Import processed successfully", null);
    }

    @Override
    @Transactional
    public TableActionResponse executeAction(TableActionRequest request) {
        if (request == null) {
            return TableActionResponse.error(null, "Request cannot be null");
        }

        log.info("Executing table action: {} for entity type: {}",
                request.getAction(), request.getObjectType());

        try {
            switch (request.getAction()) {
                case ADD:
                case UPDATE:
                case DELETE:
                case EXPORT:
                case IMPORT:
                    // Get the appropriate handler and delegate processing
                    TableActionHandler handler = actionFactory.getHandler(request.getAction());
                    return handler.handle(request);
                default:
                    return TableActionResponse.error(request, "Unsupported action: " + request.getAction());
            }
        } catch (Exception e) {
            log.error("Error processing table action", e);
            return TableActionResponse.error(request, "Error processing action: " + e.getMessage());
        }
    }

    /**
     * Execute a table action with progress reporting
     * 
     * @param request          The action request
     * @param progressCallback Callback for progress reporting
     * @return The action response
     */
    @Override
    public TableActionResponse executeActionWithProgress(
            TableActionRequest request,
            ProgressCallback progressCallback) {

        switch (request.getAction()) {
            case IMPORT:
                return processImport(request, progressCallback);

            default:
                // For actions that don't need progress tracking, use the standard method
                return executeAction(request);
        }
    }

    public TableActionResponse processAction(TableActionRequest request) {
        if (request == null) {
            return TableActionResponse.error(null, "Request cannot be null");
        }

        try {
            // Get the appropriate handler for this action type
            TableActionHandler handler = actionFactory.getHandler(request.getAction());

            // Process the request using the handler
            return handler.handle(request);
        } catch (Exception e) {
            log.error("Error processing table action", e);
            return TableActionResponse.error(request, "Error processing action: " + e.getMessage());
        }
    }
}
