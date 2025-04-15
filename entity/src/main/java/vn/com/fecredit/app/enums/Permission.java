package vn.com.fecredit.app.enums;

public enum Permission {
    VIEW_EVENTS,
    CREATE_EVENT,
    EDIT_EVENT,
    DELETE_EVENT,
    
    VIEW_PARTICIPANTS,
    CREATE_PARTICIPANT,
    EDIT_PARTICIPANT,
    DELETE_PARTICIPANT,
    
    VIEW_REWARDS,
    CREATE_REWARD,
    EDIT_REWARD,
    DELETE_REWARD,
    
    VIEW_USERS,
    CREATE_USER,
    EDIT_USER,
    DELETE_USER,
    
    VIEW_ROLES,
    CREATE_ROLE,
    EDIT_ROLE,
    DELETE_ROLE,
    
    MANAGE_SYSTEM,
    VIEW_REPORTS;
    
    public static Permission fromName(String name) {
        for (Permission permission : Permission.values()) {
            if (permission.name().equalsIgnoreCase(name)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Invalid permission name: " + name);
    }
}