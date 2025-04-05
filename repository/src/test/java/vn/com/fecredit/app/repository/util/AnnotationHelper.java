package vn.com.fecredit.app.repository.util;

/**
 * Helper class to handle annotation compatibility issues in tests.
 * Provides methods to safely handle non-null annotations.
 */
public final class AnnotationHelper {
    
    private AnnotationHelper() {
        // Utility class, not meant to be instantiated
    }
    
    /**
     * Safe replacement for @NonNull annotation on return values.
     * Use this method instead of the annotation in test mock implementations.
     * 
     * @param <T> the type of the value
     * @param value the potentially null value
     * @return the same value (never null if properly used)
     */
    public static <T> T nonNull(T value) {
        if (value == null) {
            throw new IllegalStateException("Value must not be null");
        }
        return value;
    }
    
    /**
     * Safe replacement for @NonNull annotation on parameters.
     * Use this method to validate parameters instead of the annotation.
     * 
     * @param <T> the type of the parameter
     * @param value the parameter to check
     * @param name the name of the parameter for error messages
     * @return the same value for method chaining
     * @throws IllegalArgumentException if value is null
     */
    public static <T> T requireNonNull(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException("Parameter '" + name + "' must not be null");
        }
        return value;
    }
}
