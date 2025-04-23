package vn.com.fecredit.app.service.dto;

/**
 * Enum representing the types of objects in the system.
 * These object types are used to identify different entities throughout the application.
 * Each value corresponds to a specific entity type in the data model.
 */
public enum ObjectType {
    /**
     * Represents an Event entity
     */
    Event,

    /**
     * Represents a Region entity
     */
    Region,

    /**
     * Represents a Province entity
     */
    Province,

    /**
     * Represents a Reward entity
     */
    Reward,

    /**
     * Represents a RewardEvent entity
     */
    RewardEvent,

    /**
     * Represents a GoldenHour entity
     */
    GoldenHour,

    /**
     * Represents a SpinHistory entity
     */
    SpinHistory,

    /**
     * Represents an AuditLog entity
     */
    AuditLog,

    /**
     * Represents Statistics information
     */
    Statistics,

    /**
     * Represents a User entity
     */
    User,

    /**
     * Represents a Role entity
     */
    Role,

    /**
     * Represents a Permission entity
     */
    Permission,

    /**
     * Represents a Configuration entity
     */
    Configuration,

    /**
     * Represents a BlacklistedToken entity
     */
    BlacklistedToken,

    /**
     * Represents an EventLocation entity
     */
    EventLocation,

    /**
     * Represents a Participant entity
     */
    Participant,

    /**
     * Represents a ParticipantEvent entity
     */
    ParticipantEvent
}
