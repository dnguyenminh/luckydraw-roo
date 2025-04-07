package vn.com.fecredit.app.service.dto;

/**
 * Enum representing the type of field in data tables.
 * Used for metadata and validation of field values.
 */
public enum FieldType {
    /**
     * String type field (text data)
     */
    STRING,
    
    /**
     * Numeric type field (integers, decimals)
     */
    NUMBER,
    
    /**
     * Boolean type field (true/false)
     */
    BOOLEAN,
    
    /**
     * Date type field (date without time)
     */
    DATE,
    
    /**
     * DateTime type field (date with time)
     */
    DATETIME,
    
    /**
     * Time type field (time without date)
     */
    TIME,
    
    /**
     * Email type field (validated email format)
     */
    EMAIL,
    
    /**
     * Object type field (complex nested objects)
     */
    OBJECT,
    
    /**
     * Array type field (collections)
     */
    ARRAY,
    
    /**
     * Binary type field (file data, etc.)
     */
    BINARY
}
