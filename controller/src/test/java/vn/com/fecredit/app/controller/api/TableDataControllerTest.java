package vn.com.fecredit.app.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.com.fecredit.app.controller.config.TestConfig;
import vn.com.fecredit.app.controller.util.TestApplication;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.SortRequest;
import vn.com.fecredit.app.service.dto.SortType;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.TableRow;

@WebMvcTest(TableDataController.class)
@ContextConfiguration(classes = {TestApplication.class, TestConfig.class})
class TableDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TableDataService tableDataService;

    private TableFetchRequest fetchRequest;
    private TableFetchResponse fetchResponse;

    @BeforeEach
    void setUp() {
        // Set up test request
        fetchRequest = new TableFetchRequest();
        fetchRequest.setEntityName("testEntity");
        fetchRequest.setPage(0);
        fetchRequest.setSize(10);
        
        List<SortRequest> sorts = new ArrayList<>();
        sorts.add(new SortRequest("id", SortType.ASCENDING.toString()));
        fetchRequest.setSorts(sorts);
        
        // Set up test response with proper initialization
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1L);
        item.put("name", "Test Item");
        content.add(item);
        
        // Create response using available constructor
        fetchResponse = new TableFetchResponse();
        fetchResponse.setStatus(FetchStatus.SUCCESS);  // Updated to use enum
        fetchResponse.setTotalElements(1L);
        fetchResponse.setCurrentPage(0);
        fetchResponse.setPageSize(10);
        fetchResponse.setTotalPage(1);
        
        // Create the rows in the proper format
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(item);
        
        // Convert the List<Map<String, Object>> to List<TableRow>
        List<TableRow> tableRows = rows.stream()
            .map(map -> {
                TableRow row = new TableRow();
                row.setData(map);
                return row;
            })
            .collect(Collectors.toList());
        fetchResponse.setRows(tableRows);
    }

    @Test
    void fetchData_ShouldReturnTableData() throws Exception {
        // Given
        when(tableDataService.fetchData(any())).thenReturn(fetchResponse);  // Updated method name
        
        // When/Then
        mockMvc.perform(post("/api/table-data/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fetchRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Test Item"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }
}
