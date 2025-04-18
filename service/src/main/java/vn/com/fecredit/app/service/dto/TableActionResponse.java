package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for table action operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableActionResponse {
    
    /**
     * The original request that produced this response
     */
    private TableActionRequest originalRequest;
    
    /**
     * Status of the action
     */
    private FetchStatus status;
    
    /**
     * Message describing the result
     */
    private String message;
    
    /**
     * Result data if applicable
     */
    private TableRow data;
    
    /**
     * File result for export operations
     */
    private UploadFile downloadFile;
    
    /**
     * Static factory method for creating success responses
     */
    public static TableActionResponse success(TableActionRequest request, String message, TableRow data) {
        return new TableActionResponse(request, FetchStatus.SUCCESS, message, data, null);
    }
    
    /**
     * Static factory method for creating export responses with a file
     */
    public static TableActionResponse withFile(TableActionRequest request, UploadFile file) {
        request.getUploadFile().setFileContent(null);
        return new TableActionResponse(request, FetchStatus.SUCCESS, "Export completed successfully", null, file);
    }
    
    /**
     * Static factory method for creating error responses
     */
    public static TableActionResponse error(TableActionRequest request, String errorMessage) {
        return new TableActionResponse(request, FetchStatus.ERROR, errorMessage, null, null);
    }
}
