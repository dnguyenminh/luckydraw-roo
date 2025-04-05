package vn.com.fecredit.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.com.fecredit.app.controller.util.TestApplication;
import vn.com.fecredit.app.service.TableDataService;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.SortRequest;
import vn.com.fecredit.app.service.dto.SortType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.TableRow;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
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
        
        // Set up test response
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1L);
        item.put("name", "Test Item");
        
        fetchResponse = TableFetchResponse.builder()
            .status(FetchStatus.SUCCESS)
            .rows(List.of(new TableRow(item)))
            .build();
    }

    @Test
    void fetchData_ShouldReturnTableData() throws Exception {
        // Given
        when(tableDataService.fetchData(any(TableFetchRequest.class))).thenReturn(fetchResponse);
        
        // When/Then
        mockMvc.perform(post("/api/table-data/fetch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fetchRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(FetchStatus.SUCCESS.toString()))
            .andExpect(jsonPath("$.rows[0].data.id").value(1))
            .andExpect(jsonPath("$.rows[0].data.name").value("Test Item"));
    }
}
