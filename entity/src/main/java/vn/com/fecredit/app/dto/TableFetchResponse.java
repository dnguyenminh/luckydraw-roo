package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for table data fetch operations, as defined in CommonAPIRequestAndResponse.puml
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableFetchResponse {
    /**
     * Success flag to indicate if the request was successful
     */
    private boolean success;
    
    /**
     * Error message in case of failure
     */
    private String errorMessage;
    
    /**
     * The rows of data fetched from the table
     */
    private List<Map<String, Object>> data;
    
    /**
     * The total number of rows in the data set (before pagination)
     */
    private long totalRows;
    
    /**
     * The total number of pages available
     */
    private int pageCount;
    
    /**
     * The current page number (0-based)
     */
    private int currentPage;
    
    /**
     * Additional metadata about the data set
     */
    private Map<String, Object> metadata;
}
