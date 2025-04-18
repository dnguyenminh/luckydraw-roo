package vn.com.fecredit.app.service.dto;

/**
 * Enum representing field data types for UI rendering and validation.
 */
public enum FieldType {
    /**
     * String type for text fields
     */
    STRING,
    
    /**
     * Number type for numeric fields
     */
    NUMBER,
    
    /**
     * Boolean type for true/false fields
     */
    BOOLEAN,
    
    /**
     * Date type for date fields
     */
    DATE,
    
    /**
     * DateTime type for date with time fields
     */
    DATETIME,
    
    /**
     * Time type for time-only fields
     */
    TIME,
    
    /**
     * Object type for complex nested objects
     */
    OBJECT
}
