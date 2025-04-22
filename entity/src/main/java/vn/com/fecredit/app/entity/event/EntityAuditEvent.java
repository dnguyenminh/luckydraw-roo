package vn.com.fecredit.app.entity.event;

import java.io.Serializable;
import java.util.List;

import org.springframework.context.ApplicationEvent;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.listener.EntityAuditListener.FieldChange;

/**
 * Event that is published when an entity is created, updated, or deleted.
 * This event carries information about entity changes for audit logging.
 * It's designed to be consumed by audit processors that record entity
 * modifications in a persistent audit log.
 */
public class EntityAuditEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    /**
     * The type of entity that was modified (e.g., User, Event)
     * Used for categorizing audit entries
     */
    private final String entityType;
    
    /**
     * The database ID of the modified entity
     * Allows tracing back to the specific entity instance
     */
    private final Serializable entityId;
    
    /**
     * List of field changes captured during this event
     * Contains the before and after values for modified fields
     */
    private final List<FieldChange> changes;
    
    /**
     * Username of the user who performed the action
     * Records who made the change for accountability
     */
    private final String username;
    
    /**
     * Type of action performed (CREATED, MODIFIED, DEACTIVATED, DELETED)
     * Categorizes the nature of the change
     */
    private final AuditLog.ActionType actionType;
    
    /**
     * Constructs a new entity audit event with detailed change information
     * 
     * @param entity The entity that was modified
     * @param entityType The type name of the entity
     * @param entityId The database ID of the entity
     * @param changes List of field changes with before/after values
     * @param username The user who performed the action
     * @param actionType The type of action performed
     */
    public EntityAuditEvent(
            AbstractStatusAwareEntity entity,
            String entityType, 
            Serializable entityId, 
            List<FieldChange> changes, 
            String username,
            AuditLog.ActionType actionType) {
        super(entity != null ? entity : new Object()); // Fix NullPointerException by providing a fallback
        this.entityType = entityType;
        this.entityId = entityId;
        this.changes = changes;
        this.username = username != null ? username : "system"; // Default to "system" if username is null
        this.actionType = actionType;
    }

    /**
     * Gets the entity type name for this audit event.
     *
     * @return the entity type name as a string
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Gets the entity ID for this audit event.
     *
     * @return the ID of the entity that was modified
     */
    public Serializable getEntityId() {
        return entityId;
    }

    /**
     * Gets the list of field changes recorded in this audit event.
     *
     * @return list of field changes with before and after values
     */
    public List<FieldChange> getChanges() {
        return changes;
    }
    
    /**
     * Gets the username of the user who performed the action.
     *
     * @return the username of the user who triggered this audit event
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the type of action performed in this audit event.
     *
     * @return the action type (created, modified, deleted, etc.)
     */
    public AuditLog.ActionType getActionType() {
        return actionType;
    }
}
