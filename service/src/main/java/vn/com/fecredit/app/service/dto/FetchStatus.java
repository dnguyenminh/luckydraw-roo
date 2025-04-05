package vn.com.fecredit.app.service.dto;

/**
 * Enumeration for table data fetch operation status.
 * Provides standardized status values for API responses.
 */
public enum FetchStatus {
    /**
     * Operation completed successfully
     */
    SUCCESS,
    
    /**
     * No data was found matching the criteria
     */
    NO_DATA,
    
    /**
     * Operation failed due to an error
     */
    ERROR,
    
    /**
     * Invalid request parameters were provided
     */
    INVALID_REQUEST,
    
    /**
     * Access was denied to the requested resource
     */
    ACCESS_DENIED;
    
    /**
     * Check if status represents a successful operation
     * @return true if this is a success status
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * Check if status represents a failed operation
     * @return true if this is an error status
     */
    public boolean isError() {
        return this == ERROR || this == INVALID_REQUEST || this == ACCESS_DENIED;
    }
}
