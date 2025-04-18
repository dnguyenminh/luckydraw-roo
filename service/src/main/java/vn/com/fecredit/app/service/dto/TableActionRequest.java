package vn.com.fecredit.app.service.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for table actions like add, update, delete, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableActionRequest {
    
    /**
     * The object type for this action
     */
    private ObjectType objectType;
    
    /**
     * Entity name (used as fallback if objectType is not set)
     */
    private String entityName;
    
    /**
     * Action to perform on the table
     */
    private TableAction action;
    
    /**
     * Data object for add/update operations
     */
    private TableRow data;
    
    /**
     * File for import/export operations
     */
    private UploadFile uploadFile;
    
    /**
     * Sort operations to apply
     */
    private List<SortRequest> sorts;
    
    /**
     * Filter operations to apply
     */
    private List<FilterRequest> filters;
    
    /**
     * Search context containing related objects
     */
    private Map<ObjectType, DataObject> search;
}
