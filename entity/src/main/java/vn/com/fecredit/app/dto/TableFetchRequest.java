package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for fetching table data, as defined in CommonAPIRequestAndResponse.puml
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableFetchRequest {
    /**
     * The type of object to fetch
     */
    private ObjectType objectType;
    
    /**
     * Page number (0-based)
     */
    private int page;
    
    /**
     * Page size
     */
    private int size;
    
    /**
     * Sort specifications
     */
    private List<SortRequest> sorts;
    
    /**
     * Filter criteria
     */
    private Map<String, Object> filters;
    
    /**
     * Legacy fields for backward compatibility
     */
    @Deprecated
    private String entityName;
    
    @Deprecated
    private int pageSize;
    
    @Deprecated
    private List<String> sortBy;
    
    @Deprecated
    private boolean ascending;
}
