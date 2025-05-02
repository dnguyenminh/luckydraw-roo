package vn.com.fecredit.app.service.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;

public class TableFetchRequestValidatorTest {

    private TableFetchRequestValidator validator;
    
    @BeforeEach
    public void setUp() {
        validator = new TableFetchRequestValidator();
    }
    
    @Test
    public void testSupports() {
        assertTrue(validator.supports(TableFetchRequest.class));
        assertFalse(validator.supports(Object.class));
    }
    
    @Test
    public void testValidRequest_withObjectType() {
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(0)
                .size(10)
                .build();
        
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    public void testValidRequest_withEntityName() {
        TableFetchRequest request = TableFetchRequest.builder()
                .entityName("User")
                .page(0)
                .size(10)
                .build();
        
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        
        assertFalse(errors.hasErrors());
    }
    
    @Test
    public void testInvalidRequest_noObjectTypeOrEntityName() {
        TableFetchRequest request = TableFetchRequest.builder()
                .page(0)
                .size(10)
                .build();
        
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        
        assertTrue(errors.hasErrors());
        assertEquals("Either objectType or entityName must be provided", 
                errors.getAllErrors().get(0).getDefaultMessage());
    }
    
    @Test
    public void testInvalidRequest_invalidEntityName() {
        TableFetchRequest request = TableFetchRequest.builder()
                .entityName("InvalidEntity")
                .page(0)
                .size(10)
                .build();
        
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        
        assertTrue(errors.hasErrors());
        String defaultMessage = errors.getAllErrors().get(0).getDefaultMessage();
        assertFalse(defaultMessage == null);
        assertTrue(defaultMessage.contains("Entity name must be one of"));
    }
    
    @Test
    public void testInvalidRequest_negativePage() {
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(-1)
                .size(10)
                .build();
        
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("page"));
        org.springframework.validation.FieldError pageError = errors.getFieldError("page");
        assertEquals("Page number must be non-negative", 
                pageError != null ? pageError.getDefaultMessage() : null);
    }
    
    @Test
    public void testInvalidRequest_zeroSize() {
        TableFetchRequest request = TableFetchRequest.builder()
                .objectType(ObjectType.User)
                .page(0)
                .size(0)
                .build();
        
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        
        assertTrue(errors.hasErrors());
        org.springframework.validation.FieldError sizeError = errors.getFieldError("size");
        assertEquals("Page size must be positive", 
                sizeError != null ? sizeError.getDefaultMessage() : null);
    }
}