package vn.com.fecredit.app.service.dto;

/**
 * Enumeration of data field types.
 * Used to properly format and validate field values.
 */
public enum FieldType {
    /**
     * String field type
     */
    STRING,
    
    /**
     * Numeric field type
     */
    NUMBER,
    
    /**
     * Boolean field type
     */
    BOOLEAN,
    
    /**
     * Date field type (without time)
     */
    DATE,
    
    /**
     * Date and time field type
     */
    DATETIME,
    
    /**
     * Time field type (without date)
     */
    TIME,
    
    /**
     * Complex object field type
     */
    OBJECT
}
