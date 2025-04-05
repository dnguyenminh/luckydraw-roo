package vn.com.fecredit.app.service.dto;

/**
 * Enumeration of object types that can be fetched and displayed.
 * Used to identify the type of data being handled.
 */
public enum ObjectType {
    /**
     * Event object type
     */
    Event,
    
    /**
     * Region object type
     */
    Region,
    
    /**
     * Province object type
     */
    Province,
    
    /**
     * Reward object type
     */
    Reward,
    
    /**
     * Golden hour object type
     */
    GoldenHour,
    
    /**
     * Spin history object type
     */
    SpinHistory,
    
    /**
     * Audit log object type
     */
    AuditLog,
    
    /**
     * Statistics object type
     */
    Statistics,
    
    /**
     * User object type
     */
    User,
    
    /**
     * Role object type
     */
    Role,
    
    /**
     * Permission object type
     */
    Permission,
    
    /**
     * Configuration object type
     */
    Configuration,
    
    /**
     * Blacklisted token object type
     */
    BlacklistedToken,
    
    /**
     * Event location object type
     */
    EventLocation,
    
    /**
     * Participant object type
     */
    Participant,
    
    /**
     * ParticipantEvent object type
     */
    ParticipantEvent
}
