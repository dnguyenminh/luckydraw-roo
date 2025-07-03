package vn.com.fecredit.app.service.dto;

/**
 * Enum representing the operator to use in filtering operations.
 * Used in predicate building to define comparison operations.
 */
public enum FilterOperator {
    /**
     * Equal to
     */
    EQ,
    
    /**
     * Not equal to
     */
    NE,
    
    /**
     * Greater than
     */
    GT,
    
    /**
     * Greater than or equal to
     */
    GE,
    
    /**
     * Less than
     */
    LT,
    
    /**
     * Less than or equal to
     */
    LE,
    
    /**
     * Contains the specified string (case insensitive)
     */
    CONTAINS,
    
    /**
     * Starts with the specified string (case insensitive)
     */
    STARTS_WITH,
    
    /**
     * Ends with the specified string (case insensitive)
     */
    ENDS_WITH,
    
    /**
     * Is null
     */
    NULL,
    
    /**
     * Is not null
     */
    NOT_NULL,
    
    /**
     * In a collection of values
     */
    IN,
    
    /**
     * Not in a collection of values
     */
    NOT_IN
}
