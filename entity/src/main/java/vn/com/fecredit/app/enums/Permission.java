package vn.com.fecredit.app.enums;

/**
 * Enumeration of application permissions that control access to different features.
 * Defines the complete set of permissions that can be assigned to roles.
 */
public enum Permission {
    /**
     * Permission to view event listings and details
     */
    VIEW_EVENTS,
    
    /**
     * Permission to create new events
     */
    CREATE_EVENT,
    
    /**
     * Permission to modify existing events
     */
    EDIT_EVENT,
    
    /**
     * Permission to remove events from the system
     */
    DELETE_EVENT,
    
    /**
     * Permission to view participant listings and details
     */
    VIEW_PARTICIPANTS,
    
    /**
     * Permission to create new participants
     */
    CREATE_PARTICIPANT,
    
    /**
     * Permission to modify existing participants
     */
    EDIT_PARTICIPANT,
    
    /**
     * Permission to remove participants from the system
     */
    DELETE_PARTICIPANT,
    
    /**
     * Permission to view reward listings and details
     */
    VIEW_REWARDS,
    
    /**
     * Permission to create new rewards
     */
    CREATE_REWARD,
    
    /**
     * Permission to modify existing rewards
     */
    EDIT_REWARD,
    
    /**
     * Permission to remove rewards from the system
     */
    DELETE_REWARD,
    
    /**
     * Permission to view user listings and details
     */
    VIEW_USERS,
    
    /**
     * Permission to create new user accounts
     */
    CREATE_USER,
    
    /**
     * Permission to modify existing user accounts
     */
    EDIT_USER,
    
    /**
     * Permission to remove user accounts from the system
     */
    DELETE_USER,
    
    /**
     * Permission to view role listings and details
     */
    VIEW_ROLES,
    
    /**
     * Permission to create new roles
     */
    CREATE_ROLE,
    
    /**
     * Permission to modify existing roles
     */
    EDIT_ROLE,
    
    /**
     * Permission to remove roles from the system
     */
    DELETE_ROLE,
    
    /**
     * Permission to access system administration features
     */
    MANAGE_SYSTEM,
    
    /**
     * Permission to access and generate reports
     */
    VIEW_REPORTS;
    
    /**
     * Find a permission by its name
     * @param name the permission name to search for
     * @return the matching Permission or null if not found
     */
    public static Permission fromName(String name) {
        try {
            return name != null ? valueOf(name.toUpperCase()) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}