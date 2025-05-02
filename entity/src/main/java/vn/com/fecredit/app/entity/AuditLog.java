package vn.com.fecredit.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;
import vn.com.fecredit.app.entity.base.SerializableKey;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Entity representing a system audit log entry.
 * <p>
 * AuditLog tracks changes to entities within the system, providing a comprehensive
 * history of who changed what and when. Each log entry records details about the
 * entity that was modified, which property changed, and the old and new values.
 * </p>
 * <p>
 * The audit system provides essential functionality for compliance, debugging,
 * and forensic analysis of system activities.
 * </p>
 * <p>
 * The default no-argument constructor is provided by Lombok's {@code @NoArgsConstructor}
 * annotation and is required by JPA for entity instantiation.
 * </p>
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
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Creates a default no-args constructor required by JPA
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AuditLog extends AbstractSimplePersistableEntity<Long> {

    /**
     * Type of entity that was modified (e.g., "User", "Event")
     * Identifies the class of object being audited
     */
    @NotBlank(message = "Object type is required")
    @Column(name = "object_type", nullable = false)
    private String objectType;

    /**
     * Identifier of the specific entity instance that was modified
     * Links the audit log to a specific entity record
     */
    @Column(name = "object_id")
    private String objectId;

    /**
     * Path to the specific property that was changed
     * Identifies which attribute within the entity was modified
     */
    @Column(name = "property_path")
    private String propertyPath;

    /**
     * Previous value of the property before the change
     * NULL for new entity creations
     */
    @Column(name = "old_value", length = 1000)
    private String oldValue;

    /**
     * New value of the property after the change
     * NULL for entity deletions
     */
    @Column(name = "new_value", length = 1000)
    private String newValue;

    /**
     * Data type of the property that was changed
     * Used for proper formatting and conversion
     */
    @Column(name = "value_type")
    private String valueType;

    /**
     * Timestamp when the change occurred
     * Records the exact time of the modification
     */
    @NotNull(message = "Update time is required")
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * Additional context information about the change
     * Provides supplementary details about the operation
     */
    @Column(name = "context")
    private String context;

    /**
     * Type of action that was performed (e.g., CREATED, MODIFIED)
     * Categorizes the type of change for filtering and analysis
     */
    @NotNull(message = "Action type is required")
    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    /**
     * Status of this audit log record
     */
    @Builder.Default
    @ToString.Exclude  // Add this annotation
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommonStatus status = CommonStatus.ACTIVE;

    /**
     * Enum defining the different types of actions that can be audited
     */
    public enum ActionType {
        /**
         * Entity was created
         */
        CREATED,

        /**
         * Entity was modified
         */
        MODIFIED,

        /**
         * Entity was deleted
         */
        DELETED,

        /**
         * Entity was viewed
         */
        VIEWED,

        /**
         * Entity was activated (status changed to ACTIVE)
         */
        ACTIVATED,

        /**
         * Entity was deactivated (status changed to INACTIVE)
         */
        DEACTIVATED,

        /**
         * User logged in
         */
        LOGIN,

        /**
         * User logged out
         */
        LOGOUT,

        /**
         * Other miscellaneous actions
         */
        OTHER
    }

    /**
     * Validate the state of this audit log
     *
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
     * @param objectType   the type of object being audited
     * @param objectId     the ID of the object being audited
     * @param oldValue     the old value
     * @param newValue     the new value
     * @param valueType    the type of the value
     * @param propertyPath the path to the property that changed
     * @param username     the user who performed the action
     * @return a new AuditLog instance
     * @throws IllegalArgumentException if required parameters are missing
     */
    public static AuditLog createAuditEntry(
        String objectType,
        Serializable objectId,
        String oldValue,
        String newValue,
        String valueType,
        String propertyPath,
        String username) {

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
            username = "system";
        }

        String serializedObjectId = serializeObjectId(objectId);
        LocalDateTime now = LocalDateTime.now();

        AuditLog auditLog = AuditLog.builder()
            .objectType(objectType)
            .objectId(serializedObjectId)
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

    private static String serializeObjectId(Serializable objectId) {
        if (objectId instanceof SerializableKey) {
            SerializableKey key = (SerializableKey) objectId;
            Map<String, Object> fieldValues = key.getFieldValues();
            Map<String, Object> serializedMap = new HashMap<>();
            serializedMap.put("class", key.getClass().getName());
            serializedMap.put("fields", fieldValues);
            return serializeMap(serializedMap);
        } else if (objectId instanceof Number) {
            return objectId.toString();
        } else {
            throw new IllegalArgumentException("Unsupported objectId type: " + objectId.getClass().getName());
        }
    }

    /**
     * Deserializes an object ID string into the appropriate type for an entity class.
     *
     * @param objectId    The serialized object ID as a string
     * @param entityClass The entity class to determine the ID type from
     * @return The deserialized object ID as a Serializable
     */
    public static Serializable deserializeObjectId(String objectId, Class<?> entityClass) {
        if (objectId == null || objectId.trim().isEmpty()) {
            throw new IllegalArgumentException("objectId cannot be null or empty");
        }

        // Handle simple numeric IDs
        if (isSimpleIdEntity(entityClass)) {
            try {
                return Long.parseLong(objectId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid numeric ID for " + entityClass.getName() + ": " + objectId);
            }
        }

        // Handle composite keys
        Map<String, Object> deserializedMap = deserializeMap(objectId);
        String className = (String) deserializedMap.get("class");
        if (className == null) {
            throw new IllegalArgumentException("Serialized key missing class name: " + objectId);
        }

        try {
            Class<?> keyClass = Class.forName(className);
            if (!SerializableKey.class.isAssignableFrom(keyClass)) {
                throw new IllegalArgumentException("Key class does not implement SerializableKey: " + className);
            }
            if (!className.startsWith("vn.com.fecredit.app.entity.")) {
                throw new IllegalArgumentException("Key class not in allowed package: " + className);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> fieldValues = (Map<String, Object>) deserializedMap.get("fields");
            if (fieldValues == null) {
                throw new IllegalArgumentException("Serialized key missing fields: " + objectId);
            }

            SerializableKey keyInstance = (SerializableKey) keyClass.getDeclaredConstructor().newInstance();
            return keyInstance.fromFieldValues(fieldValues);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Key class not found: " + className, e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to deserialize objectId: " + objectId, e);
        }
    }

    private static String serializeMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        List<String> entries = map.entrySet().stream()
            .map(entry -> {
                String key = escapeString(entry.getKey());
                Object value = entry.getValue();
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mapValue = (Map<String, Object>) value;
                    return "\"" + key + "\":" + serializeMap(mapValue);
                } else if (value instanceof String) {
                    return "\"" + key + "\":\"" + escapeString((String) value) + "\"";
                } else if (value == null) {
                    return "\"" + key + "\":null";
                } else {
                    return "\"" + key + "\":" + value.toString();
                }
            })
            .collect(Collectors.toList());
        sb.append(String.join(",", entries));
        sb.append("}");
        return sb.toString();
    }

    private static Map<String, Object> deserializeMap(String serialized) {
        if (serialized == null || !serialized.startsWith("{") || !serialized.endsWith("}")) {
            throw new IllegalArgumentException("Invalid serialized map format: " + serialized);
        }

        Map<String, Object> result = new HashMap<>();
        String content = serialized.substring(1, serialized.length() - 1).trim();
        if (content.isEmpty()) {
            return result;
        }

        int depth = 0;
        int start = 0;
        // String currentKey = null;
        boolean inQuotes = false;
        List<String> parts = new ArrayList<>();

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == '{' && !inQuotes) {
                depth++;
            } else if (c == '}' && !inQuotes) {
                depth--;
            } else if (c == ',' && !inQuotes && depth == 0) {
                parts.add(content.substring(start, i));
                start = i + 1;
            }
        }
        if (start < content.length()) {
            parts.add(content.substring(start));
        }

        for (String part : parts) {
            int colonIndex = -1;
            inQuotes = false;
            for (int i = 0; i < part.length(); i++) {
                char c = part.charAt(i);
                if (c == '"') {
                    inQuotes = !inQuotes;
                } else if (c == ':' && !inQuotes) {
                    colonIndex = i;
                    break;
                }
            }
            if (colonIndex == -1) {
                throw new IllegalArgumentException("Invalid key-value pair in serialized map: " + part);
            }

            String key = unescapeString(part.substring(0, colonIndex).trim().replaceAll("^\"|\"$", ""));
            String valueStr = part.substring(colonIndex + 1).trim();

            if (valueStr.startsWith("{") && valueStr.endsWith("}")) {
                result.put(key, deserializeMap(valueStr));
            } else if (valueStr.equals("null")) {
                result.put(key, null);
            } else if (valueStr.matches("^\".*\"$")) {
                result.put(key, unescapeString(valueStr.replaceAll("^\"|\"$", "")));
            } else {
                try {
                    result.put(key, Long.parseLong(valueStr));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid numeric value in serialized map: " + valueStr);
                }
            }
        }

        return result;
    }

    private static String escapeString(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\\", "\\\\");
    }

    private static String unescapeString(String input) {
        if (input == null) return "";
        return input.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static boolean isSimpleIdEntity(Class<?> entityClass) {
        return entityClass.equals(Participant.class) ||
            entityClass.equals(Reward.class) ||
            entityClass.equals(Event.class) ||
            entityClass.equals(Region.class) ||
            entityClass.equals(Province.class) ||
            entityClass.equals(AuditLog.class);
    }
}
