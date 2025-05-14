package vn.com.fecredit.app.controller.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.ChunkedUploadService;
import vn.com.fecredit.app.service.ImportJobService;
import vn.com.fecredit.app.service.ImportProgressTracker;
import vn.com.fecredit.app.service.dto.ObjectType;

/**
 * REST controller for handling chunked file uploads
 * and associated import actions.
 */
@RestController
@RequestMapping("/api/table-data/action")
@RequiredArgsConstructor
@Slf4j
public class ChunkedUploadController {

    private final ChunkedUploadService chunkedUploadService;
    private final ImportJobService importJobService;
    private final ImportProgressTracker progressTracker;

    /**
     * Upload a chunk of a file
     * 
     * @param objectType The type of entity the file relates to
     * @param file The chunk file data
     * @param sessionId The session ID for this upload
     * @param chunkIndex The index of this chunk
     * @param totalChunks The total number of chunks in the file
     * @param fileName The original file name
     * @return Success or error response
     */
    @PostMapping("/{objectType}/upload-chunk")
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @PathVariable("objectType") String objectType,
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionId") String sessionId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("fileName") String fileName) {
        
        try {
            log.info("Received chunk {} of {} for file {} with session ID {}", 
                     chunkIndex + 1, totalChunks, fileName, sessionId);
            
            // Convert the string objectType to enum
            ObjectType objectTypeEnum;
            try {
                objectTypeEnum = ObjectType.valueOf(objectType);
            } catch (IllegalArgumentException e) {
                log.error("Invalid object type: {}", objectType);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid object type: " + objectType);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            chunkedUploadService.saveChunk(
                objectTypeEnum, 
                sessionId, 
                fileName, 
                chunkIndex, 
                totalChunks, 
                file.getBytes()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("chunkIndex", chunkIndex);
            response.put("totalChunks", totalChunks);
            response.put("message", String.format("Chunk %d of %d received successfully", chunkIndex + 1, totalChunks));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to process chunk {} for session {}: {}", chunkIndex, sessionId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process file chunk: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Process chunks and start import with progress tracking
     * 
     * This endpoint combines uploaded chunks into a file and starts the import process
     * asynchronously while providing progress tracking.
     * 
     * @param objectType Type of entity being imported
     * @param request Request body containing sessionId
     * @return Response containing job ID for tracking progress
     */
    @PostMapping("/{objectType}/process-chunks-and-import")
    public ResponseEntity<?> processChunksAndImport(
            @PathVariable("objectType") String objectType,
            @RequestBody Map<String, Object> request) {

        // Extract the session ID from the request
        String sessionId = (String) request.get("sessionId");
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "ERROR",
                "message", "Missing required parameter: sessionId"
            ));
        }

        // Generate a unique job ID for tracking progress
        String jobId = UUID.randomUUID().toString();
        log.info("Starting import job {} for {} with session {}", jobId, objectType, sessionId);

        // Process import asynchronously and get job ID for tracking
        importJobService.processImportAsync(objectType, sessionId, jobId);

        // Return job ID immediately for frontend to start polling
        return ResponseEntity.ok().body(Map.of(
            "status", "SUCCESS", 
            "message", "Import started",
            "jobId", jobId
        ));
    }

    /**
     * Finalize import with progress tracking (legacy endpoint)
     * 
     * @deprecated Use {@link #processChunksAndImport} instead
     */
    @PostMapping("/{objectType}/finalize-import-with-progress")
    public ResponseEntity<?> finalizeImportWithProgress(
            @PathVariable("objectType") String objectType,
            @RequestBody Map<String, Object> request) {
        return processChunksAndImport(objectType, request);
    }

    /**
     * Get import job progress
     * 
     * @param jobId The job ID to get progress for
     * @return The current job status
     */
    @GetMapping("/import-progress")
    @CrossOrigin
    public ResponseEntity<?> getImportProgress(@RequestParam String jobId) {
        Map<String, Object> jobStatus = progressTracker.getJobStatus(jobId);
        
        if (jobStatus == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Schedule cleanup for completed or failed jobs
        progressTracker.scheduleJobCleanup(jobId);
        
        return ResponseEntity.ok(jobStatus);
    }
}
