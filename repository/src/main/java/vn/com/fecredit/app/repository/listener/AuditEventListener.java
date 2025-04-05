package vn.com.fecredit.app.repository.listener;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.event.EntityAuditEvent;
import vn.com.fecredit.app.entity.listener.EntityAuditListener;
import vn.com.fecredit.app.repository.AuditLogRepository;

/**
 * Application event listener that processes entity audit events.
 * Creates and persists audit logs based on entity state changes.
 */
@Component
public class AuditEventListener {

    private static final Logger logger = Logger.getLogger(AuditEventListener.class.getName());
    
    private final AuditLogRepository auditLogRepository;
    
    /**
     * Constructor with required repository
     */
    public AuditEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    /**
     * Constructor for test contexts that don't need a repository
     */
    public AuditEventListener() {
        this.auditLogRepository = null;
    }
    
    /**
     * Handle entity audit events
     * @param event the entity audit event
     */
    @EventListener
    public void handleEntityAuditEvent(EntityAuditEvent event) {
        if (event == null) {
            logger.warning("Received null audit event");
            return;
        }
        
        if (auditLogRepository == null) {
            logger.fine("Audit repository is null - skipping audit logging");
            return;
        }
        
        try {
            createAndSaveAuditLogs(event);
        } catch (Exception e) {
            // Log but don't throw - audit failures shouldn't break application flow
            logger.log(Level.SEVERE, "Error handling audit event", e);
        }
    }
    
    /**
     * Create and save audit logs for entity changes
     * @param event the entity audit event
     */
    private void createAndSaveAuditLogs(EntityAuditEvent event) {
        if (event.getChanges() == null || event.getChanges().isEmpty()) {
            // For creates and deletes, we log a single entry
            String oldValue = null;
            String newValue = null;
            
            if (event.getActionType() == AuditLog.ActionType.CREATED) {
                newValue = "Created";
            } else if (event.getActionType() == AuditLog.ActionType.DELETED) {
                oldValue = "Deleted";
            }
            
            createAndSaveAuditLog(event, null, oldValue, newValue, "Entity", event.getActionType());
            return;
        }
        
        // For updates, we log each field change separately
        for (EntityAuditListener.FieldChange change : event.getChanges()) {
            try {
                createAndSaveAuditLog(
                    event,
                    change.getFieldName(),
                    change.getOldValue(),
                    change.getNewValue(),
                    change.getValueType(),
                    event.getActionType()
                );
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error handling audit event for entity type: " + 
                    event.getEntityType() + " with ID: " + event.getEntityId() + ": " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Create and save a single audit log entry
     * @param event the entity audit event
     * @param propertyPath the property path being changed
     * @param oldValue the old property value
     * @param newValue the new property value
     * @param valueType the property value type
     * @param actionType the action type
     */
    private void createAndSaveAuditLog(
            EntityAuditEvent event, 
            String propertyPath, 
            String oldValue, 
            String newValue, 
            String valueType,
            AuditLog.ActionType actionType) {
        
        AuditLog auditLog = AuditLog.builder()
                .objectType(event.getEntityType())
                .objectId(event.getEntityId())
                .oldValue(oldValue)
                .newValue(newValue)
                .valueType(valueType)
                .propertyPath(propertyPath)
                .updateTime(LocalDateTime.now())
                .actionType(actionType)
                .status(CommonStatus.ACTIVE)
                .createdBy(event.getUsername())
                .updatedBy(event.getUsername())
                .build();
        
        auditLogRepository.save(auditLog);
        logger.fine("Created audit log: " + auditLog);
    }
}
