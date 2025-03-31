package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for table data fetch operations.
 * Contains the fetched data along with pagination and metadata information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableFetchResponse {
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
