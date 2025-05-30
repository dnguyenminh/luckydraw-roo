package vn.com.fecredit.app.service.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.validator.TableFetchRequestValidator;

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
        String errorMessage = errors.getAllErrors().get(0).getDefaultMessage();
        assertNotNull(errorMessage);
        assertTrue(errorMessage.contains("Entity name must be one of"));
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
        var pageError = errors.getFieldError("page");
        assertNotNull(pageError, "Should have error on page field");
        assertEquals("Page number must be non-negative",
                pageError.getDefaultMessage());
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
        var sizeError = errors.getFieldError("size");
        assertNotNull(sizeError, "Should have error on size field");
        assertEquals("Page size must be positive",
                sizeError.getDefaultMessage());
    }
}
