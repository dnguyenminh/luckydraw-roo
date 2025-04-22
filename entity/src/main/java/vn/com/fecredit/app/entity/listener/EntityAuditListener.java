package vn.com.fecredit.app.entity.listener;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.base.AbstractPersistableEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.event.EntityAuditEvent;

/**
 * JPA Entity Listener that tracks and records entity changes for audit
 * purposes.
 * 
 * <p>
 * Automatically captures entity modifications during persistence lifecycle
 * events
 * and generates audit events for entity creations, updates, and deletions.
 * </p>
 */
@Configurable
@Slf4j
public class EntityAuditListener {

    /**
     * Default constructor.
     * Creates a new EntityAuditListener instance that will be used to track entity
     * changes.
     */
    public EntityAuditListener() {
        // Default constructor
    }

    /**
     * Event publisher for sending audit notifications to other components
     * Injected by Spring to allow decoupled event processing
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * List of field names to exclude from auditing
     * These fields are typically system-maintained metadata
     */
    private static final List<String> IGNORED_FIELDS = Arrays.asList(
            "createdAt", "createdBy", "updatedAt", "updatedBy", "version");

    /**
     * Key used to store original entity state in transient attributes
     * Used to compare before and after states during updates
     */
    private static final String ORIGINAL_STATE_KEY = "ENTITY_ORIGINAL_STATE";

    /**
     * Called after an entity is loaded from the database
     * Stores the original state for later comparison during updates
     * 
     * @param entity The entity that was just loaded
     */
    @PostLoad
    public void postLoad(Object entity) {
        if (!(entity instanceof AbstractPersistableEntity)) {
            return;
        }

        try {
            // Store original state for later comparison
            AbstractPersistableEntity<? extends Serializable> statusEntity = (AbstractPersistableEntity<? extends Serializable>) entity;
            statusEntity.getTemporaryAttributes().put(ORIGINAL_STATE_KEY, cloneFieldValues(statusEntity));
        } catch (Exception e) {
            log.error("Error in postLoad auditing", e);
        }
    }

    /**
     * Called before an entity is persisted (created)
     * Initializes tracking for new entities
     * 
     * @param entity The entity about to be persisted
     */
    @PrePersist
    public void prePersist(Object entity) {
        if (!(entity instanceof AbstractPersistableEntity)) {
            return;
        }

        // For new entities, store current state to avoid null pointer in postPersist
        AbstractPersistableEntity<? extends Serializable> statusEntity = (AbstractPersistableEntity<? extends Serializable>) entity;
        statusEntity.getTemporaryAttributes().put(ORIGINAL_STATE_KEY, new ArrayList<>());
    }

    /**
     * Called after an entity is persisted (created)
     * Creates audit record for new entities
     * 
     * @param entity The entity that was just persisted
     */
    @PostPersist
    public void postPersist(Object entity) {
        if (!(entity instanceof AbstractPersistableEntity)) {
            return;
        }

        AbstractPersistableEntity<? extends Serializable> statusEntity = (AbstractPersistableEntity<? extends Serializable>) entity;

        try {
            publishAuditEvent(statusEntity, null, AuditLog.ActionType.CREATED);
        } catch (Exception e) {
            log.error("Error in postPersist auditing", e);
        }
    }

    /**
     * JPA PreUpdate lifecycle callback.
     * Called before an entity is updated in the database.
     * Captures the original entity state for comparison.
     *
     * @param entity the entity being updated
     */
    @PreUpdate
    public void preUpdate(Object entity) {
        // No action needed before update
    }

    /**
     * JPA PostUpdate lifecycle callback.
     * Called after an entity has been updated in the database.
     * Creates audit records by comparing current state with original state.
     *
     * @param entity the entity that was updated
     */
    @PostUpdate
    public void postUpdate(Object entity) {
        if (!(entity instanceof AbstractPersistableEntity)) {
            return;
        }

        AbstractPersistableEntity<? extends Serializable> statusEntity = (AbstractPersistableEntity<? extends Serializable>) entity;

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

    /**
     * JPA PreRemove lifecycle callback.
     * Called before an entity is removed from the database.
     * Creates an audit record for the deletion event.
     *
     * @param entity the entity being removed
     */
    @PreRemove
    public void preRemove(Object entity) {
        if (!(entity instanceof AbstractPersistableEntity)) {
            return;
        }

        AbstractPersistableEntity<? extends Serializable> statusEntity = (AbstractPersistableEntity<? extends Serializable>) entity;

        try {
            publishAuditEvent(statusEntity, null, AuditLog.ActionType.DELETED);
        } catch (Exception e) {
            log.error("Error in preRemove auditing", e);
        }
    }

    private List<FieldChange> cloneFieldValues(AbstractPersistableEntity<? extends Serializable> entity) {
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
                            value != null ? value.getClass().getSimpleName() : "null"));
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

    private void publishAuditEvent(AbstractPersistableEntity<? extends Serializable> entity, List<FieldChange> changes,
            AuditLog.ActionType actionType) {
        if (entity == null) {
            log.warn("Cannot publish audit event for null entity");
            return;
        }

        try {
            String entityType = entity.getClass().getSimpleName();
            Serializable entityId = entity.getId();

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
                    actionType);

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
            Class<?> securityContextHolderClass = Class
                    .forName("org.springframework.security.core.context.SecurityContextHolder");
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
     * Inner class representing a field change for audit purposes.
     * Tracks the name of the field changed and its old/new values.
     */
    public static class FieldChange {
        private String fieldName;
        private String oldValue;
        private String newValue;
        private String valueType;

        /**
         * Creates a new field change record.
         *
         * @param fieldName the name of the changed field
         * @param value     the new value of the field
         * @param valueType the data type of the field
         */
        public FieldChange(String fieldName, String value, String valueType) {
            this.fieldName = fieldName;
            this.newValue = value;
            this.valueType = valueType;
        }

        /**
         * Gets the name of the changed field.
         *
         * @return the field name
         */
        public String getFieldName() {
            return fieldName;
        }

        /**
         * Gets the previous value of the field.
         *
         * @return the old value as a string
         */
        public String getOldValue() {
            return oldValue;
        }

        /**
         * Gets the new value of the field.
         *
         * @return the new value as a string
         */
        public String getNewValue() {
            return newValue;
        }

        /**
         * Gets the data type of the field value.
         *
         * @return the value's data type
         */
        public String getValueType() {
            return valueType;
        }
    }
}
