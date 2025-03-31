package vn.com.fecredit.app.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.fecredit.app.dto.TableFetchRequest;
import vn.com.fecredit.app.dto.TableFetchResponse;
import vn.com.fecredit.app.service.TableDataService;

import jakarta.validation.Valid;

/**
 * REST Controller for handling table data requests
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class TableDataController {

    private final TableDataService tableDataService;

    /**
     * Fetch table data based on the provided request
     * 
     * @param request the table fetch request
     * @return the table data response
     */
    @PostMapping("/table")
    public ResponseEntity<TableFetchResponse> fetchTableData(@Valid @RequestBody TableFetchRequest request) {
        TableFetchResponse response = tableDataService.fetchTableData(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch related table data for an entity
     * 
     * @param entityName the entity name
     * @param entityId the entity ID
     * @param relationName the relation name
     * @param request the table fetch request
     * @return the related table data
     */
    @PostMapping("/table/{entityName}/{entityId}/{relationName}")
    public ResponseEntity<TableFetchResponse> fetchRelatedTableData(
            @PathVariable String entityName,
            @PathVariable Long entityId,
            @PathVariable String relationName,
            @Valid @RequestBody TableFetchRequest request) {
        TableFetchResponse response = tableDataService.fetchRelatedTableData(entityName, entityId, relationName, request);
        return ResponseEntity.ok(response);
    }
}