package vn.com.fecredit.app.service.dto;

/**
 * Enumeration of filter operation types.
 * Defines the comparison operations available for filtering data.
 */
public enum FilterType {
    /**
     * Equal to operation
     */
    EQUALS,
    
    /**
     * Not equal to operation
     */
    NOT_EQUALS,
    
    /**
     * Less than operation
     */
    LESS_THAN,
    
    /**
     * Less than or equal to operation
     */
    LESS_THAN_OR_EQUALS,
    
    /**
     * Greater than operation
     */
    GREATER_THAN,
    
    /**
     * Greater than or equal to operation
     */
    GREATER_THAN_OR_EQUALS,
    
    /**
     * Between two values operation
     */
    BETWEEN,
    
    /**
     * In a set of values operation
     */
    IN,
    
    /**
     * Not in a set of values operation
     */
    NOT_IN
}
