package vn.com.fecredit.app.controller.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.com.fecredit.app.dto.ObjectType;
import vn.com.fecredit.app.dto.SortRequest;
import vn.com.fecredit.app.dto.SortType;
import vn.com.fecredit.app.dto.TableFetchRequest;
import vn.com.fecredit.app.dto.TableFetchResponse;
import vn.com.fecredit.app.service.TableDataService;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the TableDataController class
 */
@WebMvcTest(TableDataController.class)
class TableDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private TableDataService tableDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    /**
     * Test for fetching table data
     */
    @Test
    @WithMockUser
    void testFetchTableData() throws Exception {
        // Create mock request and response
        TableFetchRequest request = new TableFetchRequest();
        request.setObjectType(ObjectType.PARTICIPANT);
        request.setPage(0);
        request.setSize(10);
        request.setSorts(Arrays.asList(SortRequest.builder()
                .field("name")
                .type(SortType.ASCENDING)
                .build()));
        request.setFilters(new HashMap<>());

        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1L);
        row.put("name", "Test Participant");
        row.put("code", "P001");
        data.add(row);

        TableFetchResponse response = TableFetchResponse.builder()
                .data(data)
                .totalRows(1)
                .pageCount(1)
                .currentPage(0)
                .metadata(new HashMap<>())
                .success(true)
                .build();

        when(tableDataService.fetchTableData(any(TableFetchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/data/table")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Test Participant")))
                .andExpect(jsonPath("$.totalRows", is(1)))
                .andExpect(jsonPath("$.pageCount", is(1)))
                .andExpect(jsonPath("$.currentPage", is(0)));
    }

    /**
     * Test for fetching related table data
     */
    @Test
    @WithMockUser
    void testFetchRelatedTableData() throws Exception {
        // Create mock request and response
        TableFetchRequest request = new TableFetchRequest();
        request.setEntityName("Event");
        request.setPage(0);
        request.setPageSize(10);
        request.setSortBy(Collections.singletonList("name"));
        request.setAscending(true);
        request.setFilters(new HashMap<>());

        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1L);
        row.put("name", "Related Event");
        data.add(row);

        TableFetchResponse response = TableFetchResponse.builder()
                .data(data)
                .totalRows(1)
                .pageCount(1)
                .currentPage(0)
                .metadata(new HashMap<>())
                .build();

        String entityName = "Participant";
        Long entityId = 1L;
        String relationName = "events";

        when(tableDataService.fetchRelatedTableData(
                eq(entityName), 
                eq(entityId), 
                eq(relationName), 
                any(TableFetchRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/data/table/{entityName}/{entityId}/{relationName}", 
                    entityName, entityId, relationName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Related Event")));
    }

    /**
     * Test handling of empty result set
     */
    @Test
    @WithMockUser
    void testFetchTableDataWithNoResults() throws Exception {
        TableFetchRequest request = new TableFetchRequest();
        request.setEntityName("Participant");
        request.setPage(0);
        request.setPageSize(10);

        TableFetchResponse response = TableFetchResponse.builder()
                .data(Collections.emptyList())
                .totalRows(0)
                .pageCount(0)
                .currentPage(0)
                .build();

        when(tableDataService.fetchTableData(any(TableFetchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/data/table")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.totalRows", is(0)))
                .andExpect(jsonPath("$.pageCount", is(0)));
    }

    /**
     * Test validation errors when request is invalid
     */
    @Test
    @WithMockUser
    void testFetchTableDataWithInvalidRequest() throws Exception {
        // Create a request with null entityName which should fail validation
        TableFetchRequest request = new TableFetchRequest();
        request.setPage(-1); // Invalid page number
        request.setPageSize(0); // Invalid page size

        mockMvc.perform(post("/api/data/table")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test unauthorized access
     */
    @Test
    void testFetchTableDataUnauthorized() throws Exception {
        TableFetchRequest request = new TableFetchRequest();
        request.setEntityName("Participant");

        mockMvc.perform(post("/api/data/table")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
