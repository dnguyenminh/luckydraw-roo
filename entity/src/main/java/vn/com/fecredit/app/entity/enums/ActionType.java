package vn.com.fecredit.app.entity.enums;

/**
 * Enum representing types of actions that can be audited in the system.
 * Used for tracking user activities and system events in audit logs.
 */
public enum ActionType {
    /**
     * Creation of a new entity
     */
    CREATE,
    
    /**
     * Reading or viewing an entity
     */
    READ,
    
    /**
     * Updating or modifying an existing entity
     */
    UPDATE,
    
    /**
     * Deletion of an entity
     */
    DELETE,
    
    /**
     * User login action
     */
    LOGIN,
    
    /**
     * User logout action
     */
    LOGOUT,
    
    /**
     * Spin action in the lucky draw
     */
    SPIN,
    
    /**
     * Winning a prize
     */
    WIN,
    
    /**
     * Claiming a reward
     */
    CLAIM_REWARD
}
