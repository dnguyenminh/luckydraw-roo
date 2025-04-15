package vn.com.fecredit.app.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling table data fetch requests.
 * This controller provides an API endpoint for fetching table data
 * based on the provided request parameters.
 */
@RestController
@RequestMapping("/api/table-data")
@RequiredArgsConstructor
@Slf4j
public class TableDataController {

    private final TableDataService tableDataService;

    // Map to convert plural entity names from URLs to ObjectType enum values
    private static final Map<String, ObjectType> ENTITY_NAME_MAP = new HashMap<>();

    // Map to convert enum names from frontend to ObjectType enum values
    private static final Map<String, ObjectType> ENUM_NAME_MAP = new HashMap<>();

    static {
        ENTITY_NAME_MAP.put("events", ObjectType.Event);
        ENTITY_NAME_MAP.put("participants", ObjectType.Participant);
        ENTITY_NAME_MAP.put("rewards", ObjectType.Reward);
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

//    /**
//     * Generic endpoint for fetching table data
//     */
//    @PostMapping("/fetch")
//    public ResponseEntity<TableFetchResponse> fetchData(@RequestBody TableFetchRequest request) {
//        log.debug("REST request to fetch data: {}", request);
//
//        // If request's objectType is null, try to determine it from entityName
//        if (request.getObjectType() == null && request.getEntityName() != null) {
//            ObjectType objectType = ENTITY_NAME_MAP.get(request.getEntityName());
//            if (objectType == null) {
//                // Try enum format
//                objectType = ENUM_NAME_MAP.get(request.getEntityName().toUpperCase());
//            }
//
//            if (objectType != null) {
//                request.setObjectType(objectType);
//            } else {
//                log.error("Unknown entity type: {}", request.getEntityName());
//                return ResponseEntity.badRequest().build();
//            }
//        }
//
//        // Validate that objectType is set
//        if (request.getObjectType() == null) {
//            log.error("Object type is required");
//            return ResponseEntity.badRequest().build();
//        }
//
//        TableFetchResponse response = tableDataService.fetchData(request);
//        return ResponseEntity.ok(response);
//    }

    /**
     * Path-based endpoint for fetching entity data
     */
    @PostMapping("/fetch/{entityName}")
    public ResponseEntity<TableFetchResponse> fetchEntityData(@RequestBody TableFetchRequest request) {
        String entityName = request.getEntityName();

        log.debug("REST request to fetch {} data: {}", entityName, request);

        // Convert the entity name to the appropriate ObjectType
        ObjectType objectType = ObjectType.valueOf(entityName);

        if (objectType == null) {
            log.error("Unknown entity type from path: {}", entityName);
            return ResponseEntity.badRequest().build();
        }

        // Set the entity name in the request too
        request.setEntityName(entityName);

        // Override the objectType in the request with the one from the URL
        request.setObjectType(objectType);

        TableFetchResponse response = tableDataService.fetchData(request);
        return ResponseEntity.ok(response);
    }
}
