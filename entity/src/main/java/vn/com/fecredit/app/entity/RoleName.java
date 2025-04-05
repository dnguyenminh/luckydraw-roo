package vn.com.fecredit.app.entity;

/**
 * Predefined roles in the system with their display names and hierarchy levels.
 * Provides role management functionality including privilege comparisons and role-based permissions.
 */
public enum RoleName {
    /**
     * System administrator with highest privileges
     */
    SYSTEM_ADMIN("System Administrator", 1),
    
    /**
     * Administrator responsible for event management
     */
    EVENT_ADMIN("Event Administrator", 2),
    
    /**
     * Manager with permissions to manage but not administer events
     */
    EVENT_MANAGER("Event Manager", 3),
    
    /**
     * Staff members who operate events
     */
    EVENT_STAFF("Event Staff", 4),
    
    /**
     * Users who participate in events
     */
    PARTICIPANT("Event Participant", 5),
    
    /**
     * Guest users with limited view access
     */
    GUEST("Guest User", 6);

    private final String displayName;
    private final int level;

    /**
     * Constructor for role with display name and privilege level
     * @param displayName Human-readable name for the role
     * @param level Numeric level for permission hierarchy (lower = higher privilege)
     */
    RoleName(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    /**
     * Get the display name of this role
     * @return User-friendly role name for display
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the privilege level of this role
     * @return Numeric level (lower values indicate higher privileges)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Check if this is an administrator role
     * @return true if this is a system or event admin role
     */
    public boolean isAdminRole() {
        return this == SYSTEM_ADMIN || this == EVENT_ADMIN;
    }

    /**
     * Check if this role has event management permissions
     * @return true if role can manage events
     */
    public boolean canManageEvents() {
        return level <= EVENT_MANAGER.level;
    }

    /**
     * Check if this role can operate events (staff duties)
     * @return true if role can perform event operations
     */
    public boolean canOperateEvent() {
        return level <= EVENT_STAFF.level;
    }

    /**
     * Check if this role is for participants
     * @return true if this is a participant role
     */
    public boolean canParticipate() {
        return this == PARTICIPANT;
    }

    /**
     * Check if this is a system role (not guest or participant)
     * @return true if this is a system role
     */
    public boolean isSystemRole() {
        return this != GUEST && this != PARTICIPANT;
    }

    /**
     * Compare privilege level with another role
     * @param other The role to compare with
     * @return true if this role has higher privileges than the other
     */
    public boolean hasHigherPrivilegeThan(RoleName other) {
        return this.level < other.level;
    }

    /**
     * Compare privilege level with another role
     * @param other The role to compare with
     * @return true if this role has lower privileges than the other
     */
    public boolean hasLowerPrivilegeThan(RoleName other) {
        return this.level > other.level;
    }

    /**
     * Compare privilege level with another role
     * @param other The role to compare with
     * @return true if this role has the same or higher privileges than the other
     */
    public boolean hasSameOrHigherPrivilegeThan(RoleName other) {
        return this.level <= other.level;
    }
}
