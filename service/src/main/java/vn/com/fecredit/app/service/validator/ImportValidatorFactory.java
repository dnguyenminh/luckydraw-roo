package vn.com.fecredit.app.service.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ImportValidator;
import vn.com.fecredit.app.service.dto.ObjectType;

/**
 * Factory for obtaining the appropriate validator for each entity type.
 * Automatically discovers all implementations of ImportValidator on startup.
 */
@Component
@Slf4j
public class ImportValidatorFactory {
    
    private final Map<ObjectType, ImportValidator> validatorMap = new HashMap<>();
    
    /**
     * Constructor that autowires all available ImportValidator implementations
     * and maps them by their supported ObjectType
     */
    @Autowired
    public ImportValidatorFactory(List<ImportValidator> validators) {
        for (ImportValidator validator : validators) {
            ObjectType type = validator.getObjectType();
            if (type != null) {
                validatorMap.put(type, validator);
                log.info("Registered import validator for {}: {}", 
                        type, validator.getClass().getSimpleName());
            } else {
                log.warn("Validator {} did not report an ObjectType, skipping", 
                        validator.getClass().getSimpleName());
            }
        }
        
        log.info("Import validator factory initialized with {} validators", validatorMap.size());
    }
    
    /**
     * Get the appropriate validator for a given entity type
     * 
     * @param objectType The entity type to get a validator for
     * @return The validator implementation, or null if no validator exists
     */
    public ImportValidator getValidator(ObjectType objectType) {
        ImportValidator validator = validatorMap.get(objectType);
        
        if (validator == null) {
            log.warn("No validator found for object type: {}", objectType);
        }
        
        return validator;
    }
}
