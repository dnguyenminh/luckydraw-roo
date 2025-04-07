package vn.com.fecredit.app.service.dto;

/**
 * Enum representing the status of a data fetch operation.
 * Used to indicate the result of API calls fetching data from the system.
 */
public enum FetchStatus {
    /**
     * Indicates that the fetch operation completed successfully and returned data
     */
    SUCCESS,
    
    /**
     * Indicates that the fetch operation completed successfully but found no data
     */
    NO_DATA,
    
    /**
     * Indicates that an error occurred during the fetch operation
     */
    ERROR,
    
    /**
     * Indicates that the fetch request was invalid
     */
    INVALID_REQUEST,
    
    /**
     * Indicates that the user doesn't have sufficient permissions for the requested data
     */
    ACCESS_DENIED
}
