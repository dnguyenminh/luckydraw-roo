package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.ToString;

import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Entity representing audit log entries in the system.
 * Stores information about actions performed on entities.
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_object_type", columnList = "object_type"),
        @Index(name = "idx_audit_object_id", columnList = "object_id"),
        @Index(name = "idx_audit_property", columnList = "property_path"),
        @Index(name = "idx_audit_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_update_time", columnList = "update_time"),
        @Index(name = "idx_audit_created_by", columnList = "created_by"), // Added this index for username queries
        @Index(name = "idx_audit_status", columnList = "status")
    }
)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends AbstractStatusAwareEntity {

    /**
     * Type of object being audited
     */
    @NotBlank
    @Column(name = "object_type", nullable = false)
    private String objectType;
    
    /**
     * ID of the object being audited
     */
    @NotNull
    @Column(name = "object_id", nullable = false)
    private Long objectId;
    
    /**
     * Path to the property that changed
     */
    @Column(name = "property_path")
    private String propertyPath;
    
    /**
     * Previous value
     */
    @Column(name = "old_value", length = 4000)
    private String oldValue;
    
    /**
     * New value
     */
    @Column(name = "new_value", length = 4000)
    private String newValue;
    
    /**
     * Type of the value
     */
    @Column(name = "value_type")
    private String valueType;
    
    /**
     * Time when the update occurred
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    /**
     * Additional user-provided context for the action
     */
    @Column(name = "context", length = 4000)
    private String context;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType = ActionType.MODIFIED;

    /**
     * Status of this audit log record
     */
    @Builder.Default
    @ToString.Exclude  // Add this annotation
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommonStatus status = CommonStatus.ACTIVE;
    
    /**
     * Enumeration of different audit action types
     */
    public enum ActionType {
        CREATED,
        MODIFIED,
        DELETED,
        ACTIVATED,
        DEACTIVATED,
        VIEWED,
        LOGIN,
        LOGOUT,
        OTHER
    }

    /**
     * Validate the state of this audit log
     * @throws IllegalStateException if validation fails
     */
    public void validateState() {
        if (objectType == null || objectType.trim().isEmpty()) {
            throw new IllegalStateException("Object type is required");
        }
        
        if (objectId == null) {
            throw new IllegalStateException("Object ID is required");
        }
        
        if (updateTime == null) {
            // Set default update time if not provided
            updateTime = LocalDateTime.now();
        }
        
        if ((oldValue == null || oldValue.trim().isEmpty()) && 
            (newValue == null || newValue.trim().isEmpty())) {
            throw new IllegalStateException("Either old value or new value must be provided");
        }
    }
    
    /**
     * Create an audit log entry with the specified parameters
     * 
     * @param objectType the type of object being audited
     * @param objectId the ID of the object being audited
     * @param oldValue the old value
     * @param newValue the new value
     * @param valueType the type of the value
     * @param propertyPath the path to the property that changed
     * @param username the user who performed the action
     * @return a new AuditLog instance
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static AuditLog createAuditEntry(
        String objectType,
        Long objectId,
        String oldValue,
        String newValue,
        String valueType,
        String propertyPath,
        String username) {
        
        // Validate required parameters
        if (objectType == null || objectType.trim().isEmpty()) {
            throw new IllegalArgumentException("objectType is required");
        }
        
        if (objectId == null) {
            throw new IllegalArgumentException("objectId is required");
        }
        
        if ((oldValue == null || oldValue.trim().isEmpty()) && 
            (newValue == null || newValue.trim().isEmpty())) {
            throw new IllegalArgumentException("Either oldValue or newValue must be provided");
        }
        
        if (username == null || username.trim().isEmpty()) {
            username = "system"; // Default username if not provided
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        AuditLog auditLog = AuditLog.builder()
            .objectType(objectType)
            .objectId(objectId)
            .oldValue(oldValue)
            .newValue(newValue)
            .valueType(valueType)
            .propertyPath(propertyPath)
            .updateTime(now)
            .actionType(ActionType.MODIFIED)
            .status(CommonStatus.ACTIVE)
            .build();
        
        auditLog.setCreatedBy(username);
        auditLog.setUpdatedBy(username);
        auditLog.setCreatedAt(now);
        auditLog.setUpdatedAt(now);
        
        return auditLog;
    }
}
