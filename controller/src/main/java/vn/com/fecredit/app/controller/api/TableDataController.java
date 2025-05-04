package vn.com.fecredit.app.controller.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.config.FileStorageProperties;
import vn.com.fecredit.app.service.TableActionService;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.dto.UploadFile;

/**
 * REST controller for handling table data operations.
 * This controller provides API endpoints for fetching table data
 * and performing actions like add, update, delete, export, and import.
 */
@RestController
@RequestMapping("/api/table-data")
@RequiredArgsConstructor
@Slf4j
public class TableDataController {

    private final TableDataService tableDataService;
    private final TableActionService tableActionService;
    private final FileStorageProperties fileStorageProperties;

    // In-memory storage for download tokens (in production, use a more robust solution)
    private final Map<String, UploadFile> downloadTokens = new HashMap<>();

    // Map to convert plural entity names from URLs to ObjectType enum values
    private static final Map<String, ObjectType> ENTITY_NAME_MAP = new HashMap<>();

    // Map to convert enum names from frontend to ObjectType enum values
    private static final Map<String, ObjectType> ENUM_NAME_MAP = new HashMap<>();

    static {
        ENTITY_NAME_MAP.put("events", ObjectType.Event);
        ENTITY_NAME_MAP.put("participants", ObjectType.Participant);
        ENTITY_NAME_MAP.put("rewards", ObjectType.Reward);
        ENTITY_NAME_MAP.put("rewardEvents", ObjectType.RewardEvent);
        ENTITY_NAME_MAP.put("regions", ObjectType.Region);
        ENTITY_NAME_MAP.put("provinces", ObjectType.Province);
        ENTITY_NAME_MAP.put("goldenHours", ObjectType.GoldenHour);
        ENTITY_NAME_MAP.put("users", ObjectType.User);
        ENTITY_NAME_MAP.put("roles", ObjectType.Role);
        ENTITY_NAME_MAP.put("auditLogs", ObjectType.AuditLog);
        ENTITY_NAME_MAP.put("spinHistories", ObjectType.SpinHistory);
        ENTITY_NAME_MAP.put("eventLocations", ObjectType.EventLocation);

        // Frontend enum mappings
        ENUM_NAME_MAP.put("EVENT", ObjectType.Event);
        ENUM_NAME_MAP.put("PARTICIPANT", ObjectType.Participant);
        ENUM_NAME_MAP.put("REWARD", ObjectType.Reward);
        ENUM_NAME_MAP.put("REGION", ObjectType.Region);
        ENUM_NAME_MAP.put("PROVINCE", ObjectType.Province);
        ENUM_NAME_MAP.put("GOLDEN_HOUR", ObjectType.GoldenHour);
        ENUM_NAME_MAP.put("USER", ObjectType.User);
        ENUM_NAME_MAP.put("ROLE", ObjectType.Role);
        ENUM_NAME_MAP.put("AUDIT_LOG", ObjectType.AuditLog);
        ENUM_NAME_MAP.put("SPIN_HISTORY", ObjectType.SpinHistory);
        ENUM_NAME_MAP.put("EVENT_LOCATION", ObjectType.EventLocation);
    }

    /**
     * Path-based endpoint for fetching entity data
     */
    @PostMapping("/fetch/{entityName}")
    public ResponseEntity<TableFetchResponse> fetchEntityData(@RequestBody TableFetchRequest request) {

        log.debug("REST request to fetch {} data: {}", request.getObjectType(), request);

        // Convert the entity name to the appropriate ObjectType
        ObjectType objectType = request.getObjectType();

        if (objectType == null) {
            log.error("Unknown entity type from path: {}", objectType);
            return ResponseEntity.badRequest().build();
        }

        TableFetchResponse response = tableDataService.fetchData(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Process table actions like add, update, delete, export, and import
     */
    @PostMapping("/action/{entityName}")
    public ResponseEntity<TableActionResponse> processAction(@RequestBody TableActionRequest request) {
        log.debug("REST request to process action {}: {}", request.getAction(), request);

        TableActionResponse response = tableActionService.executeAction(request);

        // For export operations, store the file for download and return a download token
        if (response.getDownloadFile() != null) {
            // Generate a download token
            String token = UUID.randomUUID().toString();

            // Store the file in our token map
            downloadTokens.put(token, response.getDownloadFile());

            // Don't include the file content in the response
            response.setDownloadFile(
                UploadFile.builder()
                    .fileName(response.getDownloadFile().getFileName())
                    .fileContent(null)
                    .build()
            );

            // Add the download token to the response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("downloadToken", token);
            responseData.put("fileName", response.getDownloadFile().getFileName());

            // Create a data object if needed
            if (response.getData() == null) {
                response.setData(new TableRow());
            }

            // Update the data with download information
            response.getData().setData(responseData);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Download exported file using a token
     * Supports both GET (for actual download) and HEAD (for status check) requests
     */
    @RequestMapping(value = "/download/{filename}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<ByteArrayResource> downloadFile(
        @PathVariable String filename,
        @RequestParam(name = "token", required = true) String token,
        HttpMethod method) {

        log.debug("REST request to {} file {} with token {}", method, filename, token);

        // Validate token - required for all approaches
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_PLAIN)
                .body(method == HttpMethod.HEAD ? null : new ByteArrayResource("Missing required token parameter".getBytes()));
        }

        // Check file-based download
        Path tempDir = fileStorageProperties.getExportsPath();

        // First, ensure the directory exists
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
                log.info("Created export directory: {}", tempDir);
            }
        } catch (IOException e) {
            log.error("Could not create export directory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body(method == HttpMethod.HEAD ? null : new ByteArrayResource(("Error creating export directory: " + e.getMessage()).getBytes()));
        }

        // Validate that the token matches the filename
        boolean isTokenValid = validateFileToken(filename, token);
        if (!isTokenValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.TEXT_PLAIN)
                .body(method == HttpMethod.HEAD ? null : new ByteArrayResource("Invalid or expired token".getBytes()));
        }

        // Check for the completed file FIRST since it's most likely to be ready
        Path completedPath = tempDir.resolve(filename);
        if (Files.exists(completedPath)) {
            try {
                // For HEAD requests, just return OK status with content length but no body
                if (method == HttpMethod.HEAD) {
                    long fileSize = Files.size(completedPath);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
                    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                    headers.add("Pragma", "no-cache");
                    headers.add("Expires", "0");

                    return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .contentLength(fileSize)
                        .body(null);
                }

                // For GET requests, return the actual file content
                byte[] fileContent = Files.readAllBytes(completedPath);
                ByteArrayResource resource = new ByteArrayResource(fileContent);

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
                headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                headers.add("Pragma", "no-cache");
                headers.add("Expires", "0");
                try {
                    Files.delete(completedPath);
                    log.info("Deleted completed export file: {}", completedPath);
                } catch (IOException e) {
                    log.error("Error deleting completed export file: {}", e.getMessage());
                }
                return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);
            } catch (IOException e) {
                log.error("Error reading export file", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(method == HttpMethod.HEAD ? null : new ByteArrayResource("Error reading export file.".getBytes()));
            }
        }

        // Check for the "extracting" file
        Path extractingPath = tempDir.resolve(filename.replace(".xlsx", ".extracting.xlsx"));
        if (Files.exists(extractingPath)) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header("Retry-After", "2")  // Suggest client retry in 2 seconds
                .contentType(MediaType.TEXT_PLAIN)
                .body(method == HttpMethod.HEAD ? null : new ByteArrayResource("File is still being prepared. Please try again shortly.".getBytes()));
        }

        // Check for the "failed" file
        Path failedPath = tempDir.resolve(filename.replace(".xlsx", ".failed.txt"));
        if (Files.exists(failedPath)) {
            try {
                String errorMessage = Files.readString(failedPath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(method == HttpMethod.HEAD ? null : new ByteArrayResource(errorMessage.getBytes()));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(method == HttpMethod.HEAD ? null : new ByteArrayResource("Export failed, but error details couldn't be retrieved.".getBytes()));
            }
        }

        // If the file is not found in ANY form, check if export was just started
        // We'll check the token map to see if this is a valid recent export request
        if (downloadTokens.containsKey(token) &&
            downloadTokens.get(token).getFileName().equals(filename)) {
            // The token is valid but file isn't ready yet - probably just started
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header("Retry-After", "2")  // Suggest client retry in 2 seconds
                .contentType(MediaType.TEXT_PLAIN)
                .body(method == HttpMethod.HEAD ? null : new ByteArrayResource("Export started. Please try again in a moment.".getBytes()));
        }

        // File not found in any form and no valid token match
        return ResponseEntity.notFound()
            .header("X-Error-Details", "File not found in any state")
            .build();
    }

    /**
     * Validates that the token is acceptable for downloading the specified filename
     * For security, this maps between tokens and allowed files
     *
     * @param filename The file to be downloaded
     * @param token    The authentication token
     * @return true if the token is valid for downloading this file
     */
    private boolean validateFileToken(String filename, String token) {
        if (token == null || filename == null) {
            log.warn("Token validation failed: null token or filename");
            return false;
        }

        try {
            // Log detailed token information for debugging
            log.debug("Validating token: {} for file: {}", token, filename);
            log.debug("Active tokens in memory: {}", downloadTokens.keySet());

            // // Simple validation approach: verify token is a valid UUID format
            // UUID uuid = UUID.fromString(token);

            // For file-based tokens, check against our filename-token mapping
            if (downloadTokens.containsKey(token)) {
                UploadFile fileInfo = downloadTokens.get(token);
                // Check if this token allows access to the requested file
                boolean isValid = filename.equals(fileInfo.getFileName());
                log.debug("Token found in memory. Validation result: {}", isValid);
                return isValid;
            }

            // If the token is valid but not in memory, log it
            log.warn("Token {} not found in memory for file {}", token, filename);

            // // DEVELOPMENT MODE: For testing/development, we'll be more permissive
            // // In production, this should be removed and only use the strict validation above
            // boolean isDevelopment = true; // TO DO: Use a proper environment check
            // if (isDevelopment) {
            //     log.debug("Development mode: Accepting valid UUID token format");
            //     return true;  // Accept any valid UUID format token in development
            // }

            // For simple validation during development,
            // allow tokens that match the expected pattern
            return token.length() >= 32;  // Minimum UUID length check
        } catch (IllegalArgumentException e) {
            // Not a valid UUID
            log.warn("Invalid token format (not a UUID): {}", token);
            return false;
        }
    }
}
