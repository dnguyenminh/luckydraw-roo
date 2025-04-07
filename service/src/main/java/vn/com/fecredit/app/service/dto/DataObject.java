package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a data object with its metadata.
 * Contains comprehensive information about a data object including its type,
 * keys, fields, and actual data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataObject implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The type of the object
     */
    private ObjectType objectType;
    
    /**
     * The key that uniquely identifies this object
     */
    private DataObjectKey key;
    
    /**
     * Map of field names to their metadata
     */
    private Map<String, ColumnInfo> fieldNameMap;
    
    /**
     * Description of this data object
     */
    private String description;
    
    /**
     * The actual data of this object
     */
    private TableRow data;
    
    /**
     * Display order for this object (when shown in lists)
     */
    private Integer order;
}
