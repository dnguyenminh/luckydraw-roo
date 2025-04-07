package vn.com.fecredit.app.service.dto;

/**
 * Enum representing the type of sorting to apply.
 * Used in sort requests to specify the direction of sorting.
 */
public enum SortType {
    /**
     * Sort in ascending order (A→Z, 0→9)
     */
    ASCENDING,
    
    /**
     * Sort in descending order (Z→A, 9→0)
     */
    DESCENDING,
    
    /**
     * No specific sort order
     */
    NONE
}
