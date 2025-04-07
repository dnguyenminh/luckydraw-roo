package vn.com.fecredit.app.service.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;

/**
 * Validator for TableFetchRequest objects.
 * Ensures the request contains proper values for entity fetch operations.
 */
@Component("tableFetchRequestValidator") // Add explicit bean name
public class TableFetchRequestValidator implements Validator {

    private static final Set<String> VALID_ENTITY_NAMES;
    
    static {
        // Initialize the set of valid entity names from ObjectType enum values
        VALID_ENTITY_NAMES = new HashSet<>();
        Arrays.stream(ObjectType.values())
            .forEach(type -> VALID_ENTITY_NAMES.add(type.name()));
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return TableFetchRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TableFetchRequest request = (TableFetchRequest) target;
        
        // Check that at least one of objectType or entityName is provided
        if (request.getObjectType() == null && (request.getEntityName() == null || request.getEntityName().isEmpty())) {
            errors.reject("invalidRequest", "Either objectType or entityName must be provided");
        }
        
        // If entityName is provided, validate it against known entity names
        if (request.getEntityName() != null && !request.getEntityName().isEmpty()) {
            if (!VALID_ENTITY_NAMES.contains(request.getEntityName()) && 
                !VALID_ENTITY_NAMES.contains(request.getEntityName().toUpperCase())) {
                errors.rejectValue("entityName", "invalidEntityName", 
                    "Entity name must be one of: " + String.join(", ", VALID_ENTITY_NAMES));
            }
        }
        
        // Validate pagination parameters
        if (request.getPage() < 0) {
            errors.rejectValue("page", "invalidPage", "Page number must be non-negative");
        }
        
        if (request.getSize() <= 0) {
            errors.rejectValue("size", "invalidSize", "Page size must be positive");
        }
    }
}
