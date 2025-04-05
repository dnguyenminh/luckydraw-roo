package vn.com.fecredit.app.entity.listener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;

import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.event.EntityAuditEvent;

/**
 * JPA Entity Listener that tracks changes to entities and creates audit logs
 */
@Configurable
@Slf4j
public class EntityAuditListener {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    private static final List<String> IGNORED_FIELDS = Arrays.asList(
        "createdAt", "createdBy", "updatedAt", "updatedBy", "version"
    );
    
    private static final String ORIGINAL_STATE_KEY = "ENTITY_ORIGINAL_STATE";
    
    @PostLoad
    public void postLoad(Object entity) {
        if (!(entity instanceof AbstractStatusAwareEntity)) {
            return;
        }
        
        try {
            // Store original state for later comparison
            AbstractStatusAwareEntity statusEntity = (AbstractStatusAwareEntity) entity;
            statusEntity.getTemporaryAttributes().put(ORIGINAL_STATE_KEY, cloneFieldValues(statusEntity));
        } catch (Exception e) {
            log.error("Error in postLoad auditing", e);
        }
    }
    
    @PrePersist
    public void prePersist(Object entity) {
        if (!(entity instanceof AbstractStatusAwareEntity)) {
            return;
        }
        
        // For new entities, store current state to avoid null pointer in postPersist
        AbstractStatusAwareEntity statusEntity = (AbstractStatusAwareEntity) entity;
        statusEntity.getTemporaryAttributes().put(ORIGINAL_STATE_KEY, new ArrayList<>());
    }
    
    @PostPersist
    public void postPersist(Object entity) {
        if (!(entity instanceof AbstractStatusAwareEntity)) {
            return;
        }
        
        AbstractStatusAwareEntity statusEntity = (AbstractStatusAwareEntity) entity;
        
        try {
            publishAuditEvent(statusEntity, null, AuditLog.ActionType.CREATED);
        } catch (Exception e) {
            log.error("Error in postPersist auditing", e);
        }
    }
    
    @PreUpdate
    public void preUpdate(Object entity) {
        // No action needed before update
    }
    
    @PostUpdate
    public void postUpdate(Object entity) {
        if (!(entity instanceof AbstractStatusAwareEntity)) {
            return;
        }
        
        AbstractStatusAwareEntity statusEntity = (AbstractStatusAwareEntity) entity;
        
        try {
            @SuppressWarnings("unchecked")
            List<FieldChange> originalState = (List<FieldChange>) statusEntity
                .getTemporaryAttributes().get(ORIGINAL_STATE_KEY);
            
            if (originalState == null) {
                log.warn("No original state found for entity: {}", entity.getClass().getSimpleName());
                return;
            }
            
            List<FieldChange> currentState = cloneFieldValues(statusEntity);
            List<FieldChange> changes = detectChanges(originalState, currentState);
            
            if (!changes.isEmpty()) {
                // Determine if status changed from active to inactive
                AuditLog.ActionType actionType = AuditLog.ActionType.MODIFIED;
                for (FieldChange change : changes) {
                    if ("status".equals(change.fieldName) && 
                        CommonStatus.INACTIVE.name().equals(change.newValue)) {
                        actionType = AuditLog.ActionType.DEACTIVATED;
                        break;
                    } else if ("status".equals(change.fieldName) && 
                        CommonStatus.ACTIVE.name().equals(change.newValue)) {
                        actionType = AuditLog.ActionType.ACTIVATED;
                        break;
                    }
                }
                
                publishAuditEvent(statusEntity, changes, actionType);
            }
            
            // Update original state for future comparisons
            statusEntity.getTemporaryAttributes().put(ORIGINAL_STATE_KEY, currentState);
            
        } catch (Exception e) {
            log.error("Error in postUpdate auditing", e);
        }
    }
    
    @PreRemove
    public void preRemove(Object entity) {
        if (!(entity instanceof AbstractStatusAwareEntity)) {
            return;
        }
        
        AbstractStatusAwareEntity statusEntity = (AbstractStatusAwareEntity) entity;
        
        try {
            publishAuditEvent(statusEntity, null, AuditLog.ActionType.DELETED);
        } catch (Exception e) {
            log.error("Error in preRemove auditing", e);
        }
    }
    
    private List<FieldChange> cloneFieldValues(AbstractStatusAwareEntity entity) {
        List<FieldChange> fieldValues = new ArrayList<>();
        
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (shouldIgnoreField(field)) {
                    continue;
                }
                
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    fieldValues.add(new FieldChange(
                        field.getName(),
                        value != null ? value.toString() : null,
                        value != null ? value.getClass().getSimpleName() : "null"
                    ));
                } catch (Exception e) {
                    log.error("Error accessing field: " + field.getName(), e);
                }
            }
            clazz = clazz.getSuperclass();
        }
        
        return fieldValues;
    }
    
    private boolean shouldIgnoreField(Field field) {
        return field.isSynthetic() || 
               IGNORED_FIELDS.contains(field.getName()) ||
               field.getName().startsWith("_") ||
               field.getType().isAssignableFrom(List.class) ||
               field.getType().isAssignableFrom(java.util.Set.class);
    }
    
    private List<FieldChange> detectChanges(List<FieldChange> originalState, List<FieldChange> currentState) {
        List<FieldChange> changes = new ArrayList<>();
        
        for (FieldChange currentField : currentState) {
            FieldChange originalField = findField(originalState, currentField.fieldName);
            
            if (originalField == null) {
                // New field
                currentField.oldValue = null;
                changes.add(currentField);
            } else if (!Objects.equals(originalField.newValue, currentField.newValue)) {
                // Changed field
                currentField.oldValue = originalField.newValue;
                changes.add(currentField);
            }
        }
        
        return changes;
    }
    
    private FieldChange findField(List<FieldChange> fields, String fieldName) {
        for (FieldChange field : fields) {
            if (field.fieldName.equals(fieldName)) {
                return field;
            }
        }
        return null;
    }
    
    private void publishAuditEvent(AbstractStatusAwareEntity entity, List<FieldChange> changes, AuditLog.ActionType actionType) {
        if (entity == null) {
            log.warn("Cannot publish audit event for null entity");
            return;
        }
        
        try {
            String entityType = entity.getClass().getSimpleName();
            Long entityId = entity.getId();
            
            if (entityId == null) {
                log.debug("Skipping audit event for entity without ID: {}", entityType);
                return;
            }
            
            String username = getCurrentUsername();
            
            EntityAuditEvent event = new EntityAuditEvent(
                entity,
                entityType,
                entityId,
                changes,
                username,
                actionType
            );
            
            // Check if event publisher is available before publishing
            if (eventPublisher == null) {
                log.debug("ApplicationEventPublisher is null, audit event for {} ID={} not published", 
                    entityType, entityId);
                return;
            }
            
            eventPublisher.publishEvent(event);
            log.debug("Published audit event for {} ID={}", entityType, entityId);
        } catch (Exception e) {
            log.warn("Error publishing audit event: {}", e.getMessage());
        }
    }
    
    private String getCurrentUsername() {
        try {
            // Try to get Spring Security context if available
            Class<?> securityContextHolderClass = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object context = securityContextHolderClass.getMethod("getContext").invoke(null);
            Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);
            
            if (authentication == null) {
                return "system";
            }
            
            return authentication.getClass().getMethod("getName").invoke(authentication).toString();
        } catch (Exception e) {
            // Either Spring Security isn't available or there's no authentication
            return "system";
        }
    }
    
    /**
     * Class representing a field change
     */
    public static class FieldChange {
        private String fieldName;
        private String oldValue;
        private String newValue;
        private String valueType;
        
        public FieldChange(String fieldName, String value, String valueType) {
            this.fieldName = fieldName;
            this.newValue = value;
            this.valueType = valueType;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public String getOldValue() {
            return oldValue;
        }
        
        public String getNewValue() {
            return newValue;
        }
        
        public String getValueType() {
            return valueType;
        }
    }
}
