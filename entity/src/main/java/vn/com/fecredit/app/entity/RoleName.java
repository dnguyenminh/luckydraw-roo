package vn.com.fecredit.app.entity;

/**
 * Predefined roles in the system
 */
public enum RoleName {
    SYSTEM_ADMIN("System Administrator", 1),
    EVENT_ADMIN("Event Administrator", 2),
    EVENT_MANAGER("Event Manager", 3),
    EVENT_STAFF("Event Staff", 4),
    PARTICIPANT("Event Participant", 5),
    GUEST("Guest User", 6);

    private final String displayName;
    private final int level;

    RoleName(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public boolean isAdminRole() {
        return this == SYSTEM_ADMIN || this == EVENT_ADMIN;
    }

    public boolean canManageEvents() {
        return level <= EVENT_MANAGER.level;
    }

    public boolean canOperateEvent() {
        return level <= EVENT_STAFF.level;
    }

    public boolean canParticipate() {
        return this == PARTICIPANT;
    }

    public boolean isSystemRole() {
        return this != GUEST && this != PARTICIPANT;
    }

    public boolean hasHigherPrivilegeThan(RoleName other) {
        return this.level < other.level;
    }

    public boolean hasLowerPrivilegeThan(RoleName other) {
        return this.level > other.level;
    }

    public boolean hasSameOrHigherPrivilegeThan(RoleName other) {
        return this.level <= other.level;
    }
}
