package vn.com.fecredit.app.service.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ImportError;
import vn.com.fecredit.app.service.dto.ImportValidator;
import vn.com.fecredit.app.service.dto.ObjectType;

/**
 * Base abstract implementation of ImportValidator providing common validation logic
 * that specific entity validators can extend.
 */
@Slf4j
public abstract class BaseImportValidator implements ImportValidator {

    /**
     * Validates that all required headers are present
     * 
     * @param headers List of headers from the file
     * @param requiredHeaders Set of required header names
     * @return List of validation errors
     */
    protected List<ImportError> validateRequiredHeaders(List<String> headers, Set<String> requiredHeaders) {
        List<ImportError> errors = new ArrayList<>();
        Set<String> missingHeaders = new HashSet<>(requiredHeaders);
        
        // Check headers for presence and remove from missing set
        for (String header : headers) {
            missingHeaders.remove(header.trim());
        }
        
        // Any headers still in missingHeaders were not found
        for (String missing : missingHeaders) {
            errors.add(ImportError.builder()
                .field("header")
                .value(missing)
                .message("Required header field missing: " + missing)
                .build());
        }
        
        return errors;
    }
    
    /**
     * Validate that required fields have values
     * 
     * @param rowData Map of field values
     * @param requiredFields Set of required field names
     * @return List of validation errors
     */
    protected List<ImportError> validateRequiredFields(Map<String, String> rowData, Set<String> requiredFields) {
        List<ImportError> errors = new ArrayList<>();
        
        for (String field : requiredFields) {
            String value = rowData.get(field);
            if (!StringUtils.hasText(value)) {
                errors.add(ImportError.builder()
                    .field(field)
                    .value("")
                    .message("Required field cannot be empty: " + field)
                    .build());
            }
        }
        
        return errors;
    }
    
    /**
     * Get object type this validator handles
     */
    @Override
    public abstract ObjectType getObjectType();
}
