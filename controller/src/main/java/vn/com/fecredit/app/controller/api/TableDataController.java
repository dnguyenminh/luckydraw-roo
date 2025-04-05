package vn.com.fecredit.app.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;

/**
 * REST controller for handling table data requests.
 * Provides endpoints for fetching paginated, sorted, and filtered data.
 */
@RestController
@RequestMapping("/api/table-data")
@RequiredArgsConstructor
public class TableDataController {
    
    private final TableDataService tableDataService;
    
    /**
     * Fetch table data based on request parameters
     * @param request the table fetch request
     * @return response containing the requested data
     */
    @PostMapping("/fetch")
    public ResponseEntity<TableFetchResponse> fetchTableData(@RequestBody TableFetchRequest request) {
        TableFetchResponse response = tableDataService.fetchData(request);
        return ResponseEntity.ok(response);
    }
}