package vn.com.fecredit.app.service.dto;

/**
 * Functional interface for reporting progress during long-running operations.
 */
@FunctionalInterface
public interface ProgressCallback {
    
    /**
     * Called to report progress during an operation.
     * 
     * @param current The current number of processed items
     * @param total The total number of items to process
     */
    void onProgress(int current, int total);
}
