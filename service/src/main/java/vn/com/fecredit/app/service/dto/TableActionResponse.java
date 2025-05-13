package vn.com.fecredit.app.service.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Response object for table actions (add, update, delete, etc.)
 */
@Data
@Builder
public class TableActionResponse {
    /**
     * Original request that generated this response
     */
    private TableActionRequest originalRequest;
    
    /**
     * Status of the response
     */
    private FetchStatus status;
    
    /**
     * Message describing the result of the action
     */
    private String message;
    
    /**
     * Whether the action was successful
     */
    private boolean success;
    
    /**
     * Data returned from the action (e.g. the added/updated record)
     */
    private TableRow data;
    
    /**
     * File to download (for export actions)
     */
    private UploadFile downloadFile;
    
    /**
     * List of errors that occurred during processing
     */
    private List<Object> errors;

    /**
     * Create a success response
     */
    public static TableActionResponse success(TableActionRequest request, String message, TableRow data) {
        return TableActionResponse.builder()
                .originalRequest(request)
                .status(FetchStatus.SUCCESS)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Create an error response
     */
    public static TableActionResponse error(TableActionRequest request, String message) {
        return TableActionResponse.builder()
                .originalRequest(request)
                .status(FetchStatus.ERROR)
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * Create an error response with specific errors
     */
    public static TableActionResponse error(TableActionRequest request, String message, List<Object> errors) {
        return TableActionResponse.builder()
                .originalRequest(request)
                .status(FetchStatus.ERROR)
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
