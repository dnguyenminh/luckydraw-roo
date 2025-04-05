package vn.com.fecredit.app.enums;

/**
 * Enum representing the application's role names.
 * Defines the standard roles available for assignment to users.
 */
public enum RoleName {
    /**
     * Administrator with full system access
     */
    ADMIN,
    
    /**
     * Manager who can oversee operations
     */
    MANAGER,
    
    /**
     * Operator who can run day-to-day activities
     */
    OPERATOR,
    
    /**
     * Regular user with standard permissions
     */
    USER,
    
    /**
     * Guest with limited read-only access
     */
    GUEST;
    
    /**
     * Find a role by its name
     * @param name the role name to search for
     * @return the matching RoleName or null if not found
     */
    public static RoleName fromName(String name) {
        try {
            return name != null ? valueOf(name.toUpperCase()) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}