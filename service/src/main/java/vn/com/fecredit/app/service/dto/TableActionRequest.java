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

    // New fields for handling chunked file uploads
    private String filePath; // Path to the imported file on disk
    private String sessionId;
    private Integer totalChunks;
    private String fileName;

    // Getters and setters for new fields
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
