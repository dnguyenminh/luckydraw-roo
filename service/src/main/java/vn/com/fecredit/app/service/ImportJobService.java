package vn.com.fecredit.app.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.ImportValidationError;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableAction;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.impl.ImportFileProcessorImpl;

/**
 * Service responsible for processing import jobs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportJobService {

    private final ChunkedUploadService chunkedUploadService;
    private final TableActionService tableActionService;
    private final ImportProgressTracker progressTracker;
    private final ImportFileProcessorImpl importFileProcessor;

    /**
     * Process an import job asynchronously
     *
     * @param objectType The entity type to import
     * @param sessionId The upload session ID
     * @param jobId The job ID for tracking progress
     * @return The job ID
     */
    public String processImportAsync(String objectType, String sessionId, String jobId) {
        // Create job status and store in tracker
        progressTracker.createJob(jobId, objectType);

        // Start import processing in a separate thread
        progressTracker.startAsyncProcess(jobId, () -> {
            try {
                processImport(objectType, sessionId, jobId);
            } catch (Exception e) {
                handleImportError(jobId, e);
            } finally {
                cleanupChunks(sessionId);
            }
        });

        return jobId;
    }

    /**
     * Process the actual import
     */
    private void processImport(String objectType, String sessionId, String jobId) throws Exception {
        // Convert string to ObjectType enum
        ObjectType objectTypeEnum = parseObjectType(objectType);
        if (objectTypeEnum == null) {
            throw new IllegalArgumentException("Invalid object type: " + objectType);
        }

        // Start validation phase
        progressTracker.updateStatus(jobId, ImportProgressTracker.Status.VALIDATING);

        // Get session metadata
        Map<String, Object> sessionInfo = retrieveSessionInfo(sessionId);
        String fileName = (String) sessionInfo.get("fileName");
        Integer totalChunks = (Integer) sessionInfo.get("totalChunks");

        // Combine chunks into a file
        Path combinedFilePath = chunkedUploadService.combineChunksToFile(
            objectTypeEnum,
            sessionId,
            totalChunks
        );

        // Count total rows for progress calculation
        int totalRows = countFileRows(combinedFilePath);
        if (totalRows <= 0) {
            throw new IllegalArgumentException("Empty or invalid import file");
        }

        // Perform validation with progress reporting
        List<ImportValidationError> validationErrors = validateImportFile(
            jobId,
            combinedFilePath,
            objectTypeEnum,
            totalRows
        );

        // If validation failed, update status and exit
        if (!validationErrors.isEmpty()) {
            progressTracker.updateStatus(jobId, ImportProgressTracker.Status.FAILED);
            progressTracker.updateMessage(jobId,
                String.format("Validation failed with %d errors", validationErrors.size()));

            // Add first 100 errors to the tracker
            int errorCount = 0;
            for (ImportValidationError error : validationErrors) {
                if (errorCount++ >= 100) break;
                progressTracker.addError(
                    jobId,
                    error.getRowNumber(),
                    error.getMessage(),
                    error.getField(),
                    error.getValue()
                );
            }
            return;
        }

        // Start import phase after validation passes
        progressTracker.updateStatus(jobId, ImportProgressTracker.Status.IMPORTING);

        // Create and execute import request with progress tracking
        TableActionRequest actionRequest = new TableActionRequest();
        actionRequest.setAction(TableAction.IMPORT);
        actionRequest.setObjectType(objectTypeEnum);
        actionRequest.setFileName(fileName);
        actionRequest.setSessionId(sessionId);
        actionRequest.setTotalChunks(totalChunks);
        actionRequest.setFilePath(combinedFilePath.toString());

        // Set up progress tracking callback
        final AtomicInteger lastReportedProgress = new AtomicInteger(0);

        // Use executeActionWithProgress instead of setProgressCallback
        TableActionResponse result = tableActionService.executeActionWithProgress(
            actionRequest,
            (current, total) -> {
                int progressPercent = total > 0 ? (int)((current * 100.0) / total) : 0;

                // Only update if progress changed significantly (avoid too many updates)
                if (progressPercent > lastReportedProgress.get() + 2 || progressPercent == 100) {
                    lastReportedProgress.set(progressPercent);
                    progressTracker.updateImportProgress(jobId, progressPercent);
                }
            }
        );

        // Update status based on result
        updateImportStatus(jobId, result);
    }

    /**
     * Count total rows in the import file
     * @param filePath Path to the import file
     * @return Number of rows (excluding header)
     */
    private int countFileRows(Path filePath) throws Exception {
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        String extension = filePath.toString().toLowerCase();

        if (extension.endsWith(".csv")) {
            return countCsvRows(filePath);
        } else if (extension.endsWith(".xlsx") || extension.endsWith(".xls")) {
            return importFileProcessor.countExcelRows(filePath.toString());
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + extension);
        }
    }

    /**
     * Count rows in a CSV file
     */
    private int countCsvRows(Path filePath) throws Exception {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            // Skip header row
            if (reader.readLine() != null) {
                while (reader.readLine() != null) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Validate import file contents with progress tracking
     * @param jobId The job ID for tracking progress
     * @param filePath Path to the file to validate
     * @param objectType Entity type being imported
     * @param totalRows Total number of data rows
     * @return List of validation errors
     */
    private List<ImportValidationError> validateImportFile(
            String jobId,
            Path filePath,
            ObjectType objectType,
            int totalRows) throws Exception {

        List<ImportValidationError> errors = new ArrayList<>();

        // Prepare progress tracking
        AtomicInteger rowsProcessed = new AtomicInteger(0);

        // Validate file based on object type
        importFileProcessor.validateFile(
            filePath.toString(),
            objectType,
            (row, error) -> {
                // Record the error
                errors.add(new ImportValidationError(row, error.getField(), error.getValue(), error.getMessage()));

                // Update progress after each batch of records (avoid too frequent updates)
                int processed = rowsProcessed.incrementAndGet();
                if (processed % 10 == 0 || processed == totalRows) {
                    int progressPercent = (int)((processed * 100.0) / totalRows);
                    progressTracker.updateValidationProgress(jobId, progressPercent);
                }

                // If too many errors, stop validation (prevent overwhelming the system)
                return errors.size() < 1000;
            }
        );

        // Ensure progress is at 100% when validation completes
        progressTracker.updateValidationProgress(jobId, 100);

        return errors;
    }

    /**
     * Retrieve session information from service
     */
    private Map<String, Object> retrieveSessionInfo(String sessionId) {
        Map<String, Object> sessionInfo = chunkedUploadService.getSessionInfo(sessionId);
        String fileName = (String) sessionInfo.get("fileName");
        Integer totalChunks = (Integer) sessionInfo.get("totalChunks");

        if (fileName == null || totalChunks == null) {
            throw new IllegalArgumentException("Session information not complete: missing fileName or totalChunks");
        }

        log.info("Retrieved session info for {}: fileName={}, totalChunks={}",
                sessionId, fileName, totalChunks);

        return sessionInfo;
    }

    /**
     * Execute the import operation
     */
    private TableActionResponse executeImport(ObjectType objectTypeEnum, String fileName,
            String sessionId, Integer totalChunks, Path combinedFilePath) {

        // Create a TableActionRequest for the import
        TableActionRequest actionRequest = new TableActionRequest();
        actionRequest.setAction(TableAction.IMPORT);
        actionRequest.setObjectType(objectTypeEnum);
        actionRequest.setFileName(fileName);
        actionRequest.setSessionId(sessionId);
        actionRequest.setTotalChunks(totalChunks);
        actionRequest.setFilePath(combinedFilePath.toString());

        // Process the import using the TableActionService
        return tableActionService.executeAction(actionRequest);
    }

    /**
     * Update import job status based on import results
     */
    private void updateImportStatus(String jobId, TableActionResponse result) {
        if (result.getStatus() == FetchStatus.SUCCESS) {
            progressTracker.updateStatus(jobId, ImportProgressTracker.Status.COMPLETED);
            progressTracker.updateMessage(jobId, "Import completed successfully");

            // Add import statistics if available
            if (result.getData() != null && result.getData().getData() != null) {
                Map<String, Object> statistics = progressTracker.extractImportStatistics(result);
                progressTracker.updateStatistics(jobId, statistics);
            }
        } else {
            progressTracker.updateStatus(jobId, ImportProgressTracker.Status.FAILED);
            progressTracker.updateMessage(jobId, result.getMessage());

            // Extract errors if available
            progressTracker.extractImportErrors(jobId, result);
        }
    }

    /**
     * Handle error during import process
     */
    private void handleImportError(String jobId, Exception e) {
        log.error("Failed to process import: {}", e.getMessage(), e);
        progressTracker.updateStatus(jobId, ImportProgressTracker.Status.FAILED);
        progressTracker.updateMessage(jobId, "Import failed: " + e.getMessage());
        progressTracker.addError(jobId, 0, e.getMessage());
    }

    /**
     * Clean up chunks after import is complete
     */
    private void cleanupChunks(String sessionId) {
        try {
            chunkedUploadService.cleanupChunks(sessionId);
        } catch (Exception e) {
            log.warn("Failed to clean up chunks: {}", e.getMessage());
        }
    }

    /**
     * Parse object type string to enum
     */
    private ObjectType parseObjectType(String objectType) {
        try {
            return ObjectType.valueOf(objectType);
        } catch (IllegalArgumentException e) {
            log.error("Invalid object type: {}", objectType);
            return null;
        }
    }
}
