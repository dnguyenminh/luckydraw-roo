package vn.com.fecredit.app.service.dto;

/**
 * Enum representing the type of filter to apply.
 * Used in filter requests to specify how the filtering should be performed.
 */
public enum FilterType {
    /**
     * Filter for values equal to the specified value
     */
    EQUALS,
    
    /**
     * Filter for values not equal to the specified value
     */
    NOT_EQUALS,
    
    /**
     * Filter for values less than the specified value
     */
    LESS_THAN,
    
    /**
     * Filter for values less than or equal to the specified value
     */
    LESS_THAN_OR_EQUALS,
    
    /**
     * Filter for values greater than the specified value
     */
    GREATER_THAN,
    
    /**
     * Filter for values greater than or equal to the specified value
     */
    GREATER_THAN_OR_EQUALS,
    
    /**
     * Filter for values within a specified range (inclusive)
     */
    BETWEEN,
    
    /**
     * Filter for values present in a specified list
     */
    IN,
    
    /**
     * Filter for values not present in a specified list
     */
    NOT_IN
}
