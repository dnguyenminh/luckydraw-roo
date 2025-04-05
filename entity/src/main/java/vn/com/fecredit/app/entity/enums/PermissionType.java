package vn.com.fecredit.app.entity.enums;

/**
 * Enum for permission types in the system.
 * Categorizes permissions by the type of operation they permit.
 */
public enum PermissionType {
    /**
     * Permission to view or access resources
     */
    READ,
    
    /**
     * Permission to modify existing resources
     */
    WRITE,
    
    /**
     * Permission to execute or process operations
     */
    EXECUTE,
    
    /**
     * Permission to manage system configurations
     */
    ADMIN
}
