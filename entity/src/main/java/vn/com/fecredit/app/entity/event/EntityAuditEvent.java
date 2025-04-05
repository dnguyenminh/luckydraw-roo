package vn.com.fecredit.app.entity.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.listener.EntityAuditListener.FieldChange;

/**
 * Event that is published when an entity is created, updated, or deleted
 */
public class EntityAuditEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    private final String entityType;
    private final Long entityId;
    private final List<FieldChange> changes;
    private final String username;
    private final AuditLog.ActionType actionType;
    
    public EntityAuditEvent(
            AbstractStatusAwareEntity entity,
            String entityType, 
            Long entityId, 
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

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public List<FieldChange> getChanges() {
        return changes;
    }
    
    public String getUsername() {
        return username;
    }
    
    public AuditLog.ActionType getActionType() {
        return actionType;
    }
}
