package vn.com.fecredit.app.entity.enums;

/**
 * Enum representing the different roles users can have in the system.
 * Used for authorization and access control.
 */
public enum RoleType {
    /**
     * Admin role with full system access and management privileges
     */
    ROLE_ADMIN,
    
    /**
     * Regular user role with standard application access
     */
    ROLE_USER,
    
    /**
     * Manager role with elevated privileges but less than admin
     */
    ROLE_MANAGER,
    
    /**
     * Participant role for those who participate in events
     */
    ROLE_PARTICIPANT,
    
    /**
     * Guest role with minimal access for unauthenticated users
     */
    ROLE_GUEST
}
