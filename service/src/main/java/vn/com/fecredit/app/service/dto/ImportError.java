package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an error during import validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportError {
    
    /**
     * Row number where the error occurred (1-based for user display)
     */
    private int rowNumber;
    
    /**
     * Field/column name with the error
     */
    private String field;
    
    /**
     * Value that caused the error
     */
    private String value;
    
    /**
     * Error message
     */
    private String message;
    
    /**
     * Create a new error with just a message
     * 
     * @param message Error message
     * @return ImportError instance
     */
    public static ImportError of(String message) {
        return ImportError.builder().message(message).build();
    }
    
    /**
     * Create a new error with row number and message
     * 
     * @param rowNumber Row number (1-based)
     * @param message Error message
     * @return ImportError instance
     */
    public static ImportError of(int rowNumber, String message) {
        return ImportError.builder()
            .rowNumber(rowNumber)
            .message(message)
            .build();
    }
    
    /**
     * Create a new error with field-specific details
     * 
     * @param rowNumber Row number (1-based)
     * @param field Field/column name
     * @param value Invalid value
     * @param message Error message
     * @return ImportError instance
     */
    public static ImportError of(int rowNumber, String field, String value, String message) {
        return ImportError.builder()
            .rowNumber(rowNumber)
            .field(field)
            .value(value)
            .message(message)
            .build();
    }
}
