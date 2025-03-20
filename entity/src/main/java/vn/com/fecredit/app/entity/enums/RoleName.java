package vn.com.fecredit.app.entity.enums;

/**
 * Represents the different types of roles in the system.
 */
public enum RoleName {
    /**
     * System administrators with full access
     */
    ADMIN,

    /**
     * Event managers with event management capabilities
     */
    MANAGER,

    /**
     * Basic system users
     */
    USER,

    /**
     * Regular event participants
     */
    PARTICIPANT,

    /**
     * Premium participants with special privileges
     */
    VIP;

    /**
     * Check if this role has administrative privileges
     * @return true if the role is ADMIN or MANAGER
     */
    public boolean isAdministrative() {
        return this == ADMIN || this == MANAGER;
    }

    /**
     * Check if this role is a participant role
     * @return true if the role is PARTICIPANT or VIP
     */
    public boolean isParticipant() {
        return this == PARTICIPANT || this == VIP;
    }

    /**
     * Check if this role has premium privileges
     * @return true if the role is VIP or ADMIN
     */
    public boolean isPremium() {
        return this == VIP || this == ADMIN;
    }
}