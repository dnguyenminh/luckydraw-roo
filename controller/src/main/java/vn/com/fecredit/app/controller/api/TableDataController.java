package vn.com.fecredit.app.controller.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            response.getDownloadFile().setFileContent(null);

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
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<ByteArrayResource> downloadFile(
        @PathVariable String filename,
        @RequestParam(name = "token", required = true) String token) {

        log.debug("REST request to download file {} with token {}", filename, token);

        // Check if the token is valid
        if (token == null || !downloadTokens.containsKey(token)) {
            return ResponseEntity.badRequest().build();
        }

        // Get the file from our token map
        UploadFile file = downloadTokens.get(token);

        // Check if the filename matches
        if (!filename.equals(file.getFileName())) {
            return ResponseEntity.badRequest().build();
        }

        // Remove the token after use
        downloadTokens.remove(token);

        // Create ByteArrayResource from the file content
        ByteArrayResource resource = new ByteArrayResource(file.getFileContent());

        // Set up headers for the download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        // Return the file as a download
        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(file.getFileContent().length)
            .body(resource);
    }

//    /**
//     * Fetch entity data with filters and pagination
//     */
//    @GetMapping("/fetch")
//    public ResponseEntity<Map<String, Object>> fetchEntityData(
//            @RequestParam("entityName") String entityName,
//            @PageableDefault(size = 10) Pageable pageable,
//            @RequestParam(required = false) Map<String, String> filterParams) {
//
//        // Extract dedicated parameters that shouldn't be treated as filters
//        Map<String, String> dedicatedParams = new HashMap<>();
//        Map<String, String> actualFilters = new HashMap<>();
//
//        // Separate the filter parameters from dedicated parameters
//        for (Map.Entry<String, String> entry : filterParams.entrySet()) {
//            if (entry.getKey().startsWith("_")) {
//                dedicatedParams.put(entry.getKey().substring(1), entry.getValue());
//            } else if (!entry.getKey().equals("entityName") &&
//                      !entry.getKey().equals("page") &&
//                      !entry.getKey().equals("size") &&
//                      !entry.getKey().equals("sort")) {
//                actualFilters.put(entry.getKey(), entry.getValue());
//            }
//        }
//
//        Map<String, Object> result = tableDataService.fetchData(
//                entityName, pageable, actualFilters, dedicatedParams);
//
//        return ResponseEntity.ok(result);
//    }
}
