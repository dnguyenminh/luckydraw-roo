package vn.com.fecredit.app.enums;

/**
 * Enum representing the permissions available in the system.
 * Used for fine-grained access control.
 */
public enum Permission {
    /**
     * Permission to view events
     */
    VIEW_EVENTS,
    
    /**
     * Permission to create new events
     */
    CREATE_EVENT,
    
    /**
     * Permission to edit existing events
     */
    EDIT_EVENT,
    
    /**
     * Permission to delete events
     */
    DELETE_EVENT,
    
    /**
     * Permission to view participants
     */
    VIEW_PARTICIPANTS,
    
    /**
     * Permission to create new participants
     */
    CREATE_PARTICIPANT,
    
    /**
     * Permission to edit existing participants
     */
    EDIT_PARTICIPANT,
    
    /**
     * Permission to delete participants
     */
    DELETE_PARTICIPANT,
    
    /**
     * Permission to view rewards
     */
    VIEW_REWARDS,
    
    /**
     * Permission to create new rewards
     */
    CREATE_REWARD,
    
    /**
     * Permission to edit existing rewards
     */
    EDIT_REWARD,
    
    /**
     * Permission to delete rewards
     */
    DELETE_REWARD,
    
    /**
     * Permission to view users
     */
    VIEW_USERS,
    
    /**
     * Permission to create new users
     */
    CREATE_USER,
    
    /**
     * Permission to edit existing users
     */
    EDIT_USER,
    
    /**
     * Permission to delete users
     */
    DELETE_USER,
    
    /**
     * Permission to view roles
     */
    VIEW_ROLES,
    
    /**
     * Permission to create new roles
     */
    CREATE_ROLE,
    
    /**
     * Permission to edit existing roles
     */
    EDIT_ROLE,
    
    /**
     * Permission to delete roles
     */
    DELETE_ROLE,
    
    /**
     * Permission to manage system settings
     */
    MANAGE_SYSTEM,
    
    /**
     * Permission to view reports
     */
    VIEW_REPORTS;

    /**
     * Returns a Permission enum value from its string name
     *
     * @param name the string representation of the permission
     * @return the corresponding Permission enum value
     * @throws IllegalArgumentException if the name does not match any Permission
     */
    public static Permission fromName(String name) {
        for (Permission permission : Permission.values()) {
            if (permission.name().equalsIgnoreCase(name)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Invalid permission name: " + name);
    }
}