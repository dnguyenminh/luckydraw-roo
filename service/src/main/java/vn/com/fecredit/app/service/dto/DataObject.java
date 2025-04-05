package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a data object with metadata and its actual data.
 * Used for linking related objects to a primary data table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataObject {
    
    /**
     * Type of the object
     */
    private ObjectType objectType;
    
    /**
     * Mapping of field names to column information
     */
    @Builder.Default
    private Map<String, ColumnInfo> fieldNameMap = new HashMap<>();
    
    /**
     * Description of this data object
     */
    private String description;
    
    /**
     * The actual data as key-value pairs
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();
    
    /**
     * Display order of this object
     */
    private Integer order;
}
