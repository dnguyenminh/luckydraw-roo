package vn.com.fecredit.app.entity.base;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface for composite key classes to handle serialization and deserialization.
 * Provides default methods to automatically serialize/deserialize fields using reflection,
 * allowing implementations to use these methods without custom code unless needed.
 * The AuditLog entity controls the serialized string format, ensuring consistency.
 */
public interface SerializableKey extends Serializable {
    /**
     * Returns a map of field names to their values for serialization.
     * The default implementation uses reflection to include all declared fields.
     * Field values must be serializable (e.g., Long, String, or SerializableKey).
     * Implementations can override this for custom serialization logic.
     *
     * @return A map containing the key's field names and values.
     */
    default Map<String, Object> getFieldValues() {
        Map<String, Object> values = new HashMap<>();
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value instanceof SerializableKey) {
                    // Recursively serialize nested SerializableKey fields
                    values.put(field.getName(), ((SerializableKey) value).getFieldValues());
                } else {
                    values.put(field.getName(), value);
                }
                field.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to serialize key fields for " + this.getClass().getName(), e);
        }
        return values;
    }

    /**
     * Reconstructs the composite key from a map of field names to values.
     * The default implementation uses reflection to set field values.
     * Implementations can override this for custom deserialization logic.
     *
     * @param fieldValues A map containing the field names and their values.
     * @return The deserialized composite key instance.
     * @throws IllegalArgumentException If the map is invalid or missing required fields.
     */
    default SerializableKey fromFieldValues(Map<String, Object> fieldValues) {
        if (fieldValues == null) {
            throw new IllegalArgumentException("Field values map cannot be null for " + this.getClass().getName());
        }
        try {
            // Create a new instance of the same class
            SerializableKey instance = (SerializableKey) this.getClass().getDeclaredConstructor().newInstance();
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (!fieldValues.containsKey(field.getName())) {
                    throw new IllegalArgumentException("Missing field " + field.getName() + " for " + this.getClass().getName());
                }
                Object value = fieldValues.get(field.getName());
                if (value instanceof Map && SerializableKey.class.isAssignableFrom(field.getType())) {
                    // Recursively deserialize nested SerializableKey fields
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nestedMap = (Map<String, Object>) value;
                    SerializableKey nestedKey = (SerializableKey) field.getType().getDeclaredConstructor().newInstance();
                    field.set(instance, nestedKey.fromFieldValues(nestedMap));
                } else if (value != null && !field.getType().isAssignableFrom(value.getClass())) {
                    throw new IllegalArgumentException("Invalid type for field " + field.getName() + ": expected " +
                        field.getType().getName() + ", got " + value.getClass().getName());
                } else {
                    field.set(instance, value);
                }
                field.setAccessible(false);
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to deserialize key for " + this.getClass().getName(), e);
        }
    }
}
