package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Contains information about a data table.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfo implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The key that identifies this table
     */
    private DataObjectKey key;
    
    /**
     * The status of the fetch operation
     */
    private FetchStatus status;
    
    /**
     * A message describing the result of the operation
     */
    private String message;
    
    /**
     * The total number of pages available
     */
    private Integer totalPage;
    
    /**
     * The current page number (0-based)
     */
    private Integer currentPage;
    
    /**
     * The number of items per page
     */
    private Integer pageSize;
    
    /**
     * The total number of elements (rows) available
     */
    private Long totalElements;
    
    /**
     * The name of the table
     */
    private String tableName;
    
    /**
     * Map of field names to their metadata
     */
    private Map<String, ColumnInfo> fieldNameMap;
    
    /**
     * The list of rows in this table (for the current page)
     */
    private List<TableRow> rows;
    
    /**
     * Map of related object names to their metadata
     */
    private Map<ObjectType, DataObject> relatedLinkedObjects;
}
