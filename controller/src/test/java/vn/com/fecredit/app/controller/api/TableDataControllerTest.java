package vn.com.fecredit.app.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;

/**
 * Unit test for TableDataController
 * This approach does not load the full Spring context
 */
public class TableDataControllerTest {

    private MockMvc mockMvc;
    
    @Mock
    private TableDataService tableDataService;
    
    @InjectMocks
    private TableDataController tableDataController;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    private TableFetchRequest request;
    private TableFetchResponse successResponse;
    
    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        
        // Set up MockMvc
        mockMvc = MockMvcBuilders
                .standaloneSetup(tableDataController)
                .build();
        
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
    void testFetchData() throws Exception {
        // Setup mock
        when(tableDataService.fetchData(any(TableFetchRequest.class))).thenReturn(successResponse);
        
        // Perform test
        mockMvc.perform(post("/api/table-data/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.totalPage").value(10));
    }
}
