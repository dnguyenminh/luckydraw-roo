package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents detailed information about a data table.
 * Contains pagination metadata, field definitions, and row data.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfo {
    
    /**
     * Status of the operation
     */
    private FetchStatus status;
    
    /**
     * Optional message providing additional information about the operation
     */
    private String message;
    
    /**
     * Total number of pages available
     */
    private Integer totalPage;
    
    /**
     * Current page number (0-based)
     */
    private Integer currentPage;
    
    /**
     * Number of items per page
     */
    private Integer pageSize;
    
    /**
     * Total number of elements across all pages
     */
    private Long totalElements;
    
    /**
     * Name of the table
     */
    private String tableName;
    
    /**
     * Mapping of field names to column information
     */
    @Builder.Default
    private Map<String, ColumnInfo> fieldNameMap = new HashMap<>();
    
    /**
     * List of rows in the current page
     */
    @Builder.Default
    private List<TableRow> rows = new ArrayList<>();
    
    /**
     * Related objects linked to this table
     */
    @Builder.Default
    private Map<String, DataObject> relatedLinkedObjects = new HashMap<>();
}
