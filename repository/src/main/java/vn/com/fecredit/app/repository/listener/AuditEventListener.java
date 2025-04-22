package vn.com.fecredit.app.repository.listener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.base.SerializableKey;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.event.EntityAuditEvent;
import vn.com.fecredit.app.entity.listener.EntityAuditListener;
import vn.com.fecredit.app.repository.AuditLogRepository;

/**
 * Application event listener that processes entity audit events.
 * Creates and persists audit logs based on entity state changes.
 */
@Component
@AllArgsConstructor
public class AuditEventListener {

    private static final Logger logger = Logger.getLogger(AuditEventListener.class.getName());

    private final AuditLogRepository auditLogRepository;

    private final EntityManager entityManager;

//    /**
//     * Constructor with required repository
//     */
//    public AuditEventListener(AuditLogRepository auditLogRepository) {
//        this.auditLogRepository = auditLogRepository;
//    }
//
//    /**
//     * Constructor for test contexts that don't need a repository
//     */
//    public AuditEventListener() {
//        this.auditLogRepository = null;
//    }

    /**
     * Handle entity audit events
     *
     * @param event the entity audit event
     */
    @EventListener
    public void handleEntityAuditEvent(EntityAuditEvent event) {
        if (event == null) {
            logger.warning("Received null audit event");
            return;
        }

//        if (auditLogRepository == null) {
//            logger.fine("Audit repository is null - skipping audit logging");
//            return;
//        }

        try {
            createAndSaveAuditLogs(event);
        } catch (Exception e) {
            // Log but don't throw - audit failures shouldn't break application flow
            logger.log(Level.SEVERE, "Error handling audit event", e);
        }
    }

    /**
     * Create and save audit logs for entity changes
     *
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
     *
     * @param event        the entity audit event
     * @param propertyPath the property path being changed
     * @param oldValue     the old property value
     * @param newValue     the new property value
     * @param valueType    the property value type
     * @param actionType   the action type
     */
    private void createAndSaveAuditLog(
        EntityAuditEvent event,
        String propertyPath,
        String oldValue,
        String newValue,
        String valueType,
        AuditLog.ActionType actionType) {

        String serializedObjectId = serializeObjectId(event.getEntityId());

        AuditLog auditLog = AuditLog.builder()
            .objectType(event.getEntityType())
            .objectId(serializedObjectId)
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

    public static Serializable deserializeObjectId(EntityManager entityManager, String objectId, Class<?> entityClass) {
        if (objectId == null || objectId.trim().isEmpty()) {
            throw new IllegalArgumentException("objectId cannot be null or empty");
        }

        // Handle simple numeric IDs
        if (isSimpleIdEntity(entityManager, entityClass)) {
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
                    return "\"" + key + "\":" + serializeMap((Map<String, Object>) value);
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
        String currentKey = null;
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

    private static boolean isSimpleIdEntity(EntityManager entityManager, Class<?> entityClass) {
        // Assume EntityManager is injected or accessible
        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<?> entityType = metamodel.entity(entityClass);
        SingularAttribute<?, ?> idAttribute = entityType.getId(entityType.getIdType().getJavaType());
        Class<?> idType = idAttribute.getJavaType();
        return !SerializableKey.class.isAssignableFrom(idType);
    }
}
