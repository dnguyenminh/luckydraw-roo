package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class representing a validation error during import with row information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportValidationError {
    /**
     * Row number where error occurred
     */
    private int rowNumber;
    
    /**
     * Name of the field with error
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
}
