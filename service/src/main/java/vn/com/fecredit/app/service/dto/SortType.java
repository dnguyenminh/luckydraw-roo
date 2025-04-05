package vn.com.fecredit.app.service.dto;

/**
 * Enumeration for defining sorting directions.
 * Used in data table fetching and API requests to specify
 * how data should be ordered in query results.
 */
public enum SortType {
    /**
     * Sort in ascending order (A-Z, 0-9)
     */
    ASCENDING,
    
    /**
     * Sort in descending order (Z-A, 9-0)
     */
    DESCENDING,
    
    /**
     * No specific sorting direction
     */
    NONE;
    
    /**
     * Check if sort type is ascending
     * @return true if ascending, false otherwise
     */
    public boolean isAscending() {
        return this == ASCENDING;
    }
    
    /**
     * Check if sort type is descending
     * @return true if descending, false otherwise
     */
    public boolean isDescending() {
        return this == DESCENDING;
    }
    
    /**
     * Check if no sorting is specified
     * @return true if sort type is NONE
     */
    public boolean isNone() {
        return this == NONE;
    }
    
    /**
     * Parse string to SortType with case insensitivity
     * @param value string value to parse
     * @return matching SortType or NONE as default
     */
    public static SortType fromString(String value) {
        if (value == null) {
            return NONE;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // For backward compatibility
            if ("ASC".equalsIgnoreCase(value)) {
                return ASCENDING;
            } else if ("DESC".equalsIgnoreCase(value)) {
                return DESCENDING;
            }
            return NONE;
        }
    }
}
