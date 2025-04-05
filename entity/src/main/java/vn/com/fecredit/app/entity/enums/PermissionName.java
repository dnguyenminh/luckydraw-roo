package vn.com.fecredit.app.entity.enums;

/**
 * Enum defining specific permissions available in the system.
 * Each permission corresponds to a specific operation on a specific resource.
 */
public enum PermissionName {
    /**
     * Permission to create user accounts
     */
    CREATE_USER,
    
    /**
     * Permission to view user accounts
     */
    READ_USER,
    
    /**
     * Permission to modify user accounts
     */
    UPDATE_USER,
    
    /**
     * Permission to delete user accounts
     */
    DELETE_USER,
    
    /**
     * Permission to create events
     */
    CREATE_EVENT,
    
    /**
     * Permission to view events
     */
    READ_EVENT,
    
    /**
     * Permission to modify events
     */
    UPDATE_EVENT,
    
    /**
     * Permission to delete events
     */
    DELETE_EVENT,
    
    /**
     * Permission to create participants
     */
    CREATE_PARTICIPANT,
    
    /**
     * Permission to view participants
     */
    READ_PARTICIPANT,
    
    /**
     * Permission to modify participants
     */
    UPDATE_PARTICIPANT,
    
    /**
     * Permission to delete participants
     */
    DELETE_PARTICIPANT,
    
    /**
     * Permission to create system configurations
     */
    CREATE_CONFIGURATION,
    
    /**
     * Permission to view system configurations
     */
    READ_CONFIGURATION,
    
    /**
     * Permission to modify system configurations
     */
    UPDATE_CONFIGURATION,
    
    /**
     * Permission to delete system configurations
     */
    DELETE_CONFIGURATION
}
