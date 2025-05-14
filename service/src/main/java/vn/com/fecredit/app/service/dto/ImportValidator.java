package vn.com.fecredit.app.service.dto;

import java.util.List;
import java.util.Map;

/**
 * Interface for validating import data by entity type.
 * Implementations provide validation rules specific to each entity.
 */
public interface ImportValidator {
    
    /**
     * Validate header fields in the import file
     * 
     * @param headers List of header field names
     * @return List of validation errors (empty if validation passes)
     */
    List<ImportError> validateHeaders(List<String> headers);
    
    /**
     * Validate a row of data from the import file
     * 
     * @param rowData Map of field name to field value
     * @param rowNumber The row number in the file (for error reporting)
     * @return List of validation errors (empty if validation passes)
     */
    List<ImportError> validateRow(Map<String, String> rowData, int rowNumber);
    
    /**
     * Get the object type this validator handles
     * 
     * @return The entity type validated by this implementation
     */
    ObjectType getObjectType();
}
