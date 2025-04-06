package vn.com.fecredit.app.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;

/**
 * Unit test for TableDataController that doesn't require Spring context
 */
public class TableDataControllerUnitTest {

    private TableDataController tableDataController;
    private TableDataService tableDataService;

    private TableFetchRequest request;
    private TableFetchResponse successResponse;

    @BeforeEach
    void setUp() {
        // Mock service
        tableDataService = mock(TableDataService.class);

        // Create controller with mock service
        tableDataController = new TableDataController(tableDataService);

        // Create a sample request
        request = new TableFetchRequest();
        request.setObjectType(ObjectType.User);
        request.setPage(0);
        request.setSize(10);
        request.setFilters(new ArrayList<>());
        request.setSorts(new ArrayList<>());
        request.setSearch(new HashMap<>());

        // Create a sample response
        successResponse = new TableFetchResponse();
        successResponse.setStatus(FetchStatus.SUCCESS);
        successResponse.setCurrentPage(0);
        successResponse.setPageSize(10);
        successResponse.setTotalElements(100L);
        successResponse.setTotalPage(10);
        successResponse.setRows(new ArrayList<>());
    }

    @Test
    void testFetchData() {
        // Setup mock
        when(tableDataService.fetchData(any(TableFetchRequest.class))).thenReturn(successResponse);

        // Call controller method directly
        ResponseEntity<TableFetchResponse> response = tableDataController.fetchTableData(request);

        // Verify response
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify response body
        TableFetchResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(FetchStatus.SUCCESS, body.getStatus());
        assertEquals(0, body.getCurrentPage());
        assertEquals(10, body.getPageSize());
        assertEquals(100L, body.getTotalElements());
        assertEquals(10, body.getTotalPage());
    }
}
