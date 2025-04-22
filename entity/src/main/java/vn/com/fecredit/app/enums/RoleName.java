package vn.com.fecredit.app.enums;

/**
 * Enumeration of standard roles available in the system.
 * Defines the hierarchy of access privileges for users.
 */
public enum RoleName {
    /**
     * Administrator with full system access
     */
    ADMIN,
    
    /**
     * Manager with elevated privileges for team management
     */
    MANAGER,
    
    /**
     * System operator with day-to-day operational access
     */
    OPERATOR,
    
    /**
     * Standard user with basic application access
     */
    USER,
    
    /**
     * Guest with limited read-only access
     */
    GUEST;

    /**
     * Finds a RoleName enum value by its string representation.
     *
     * @param name the string name to find
     * @return the matching RoleName or null if not found
     */
    public static RoleName fromName(String name) {
        for (RoleName role : RoleName.values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role name: " + name);
    }
}