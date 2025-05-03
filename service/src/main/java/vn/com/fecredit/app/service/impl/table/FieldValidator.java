package vn.com.fecredit.app.service.impl.table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates and helps with field resolution in entities
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FieldValidator {

    private final EntityManager entityManager;
    
    // Optimized caching with expiring entries
    private final Map<String, CacheEntry<Boolean>> fieldExistenceCache = new ConcurrentHashMap<>();
    
    // Cache timeout in milliseconds (30 minutes)
    private static final long CACHE_TIMEOUT = 30 * 60 * 1000;
    
    /**
     * Checks if a class has a field with the given name (with improved caching)
     */
    public boolean hasField(Class<?> entityClass, String fieldName) {
        if (entityClass == null || fieldName == null) {
            return false;
        }
        
        String cacheKey = entityClass.getName() + ":" + fieldName;
        
        CacheEntry<Boolean> cacheEntry = fieldExistenceCache.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            return cacheEntry.getValue();
        }
        
        boolean exists = checkFieldExists(entityClass, fieldName);
        fieldExistenceCache.put(cacheKey, new CacheEntry<>(exists));
        return exists;
    }
    
    /**
     * Checks if a field exists using multiple strategies
     */
    private boolean checkFieldExists(Class<?> entityClass, String fieldName) {
        try {
            // First try direct field access through JPA metamodel
            try {
                entityManager.getMetamodel().entity(entityClass).getAttribute(fieldName);
                return true;
            } catch (IllegalArgumentException e) {
                // Field not found directly, try alternatives
            }
            
            // Try Java reflection on declared fields
            try {
                entityClass.getDeclaredField(fieldName);
                return true;
            } catch (NoSuchFieldException e) {
                // Field not found, continue with other methods
            }
            
            // Try JavaBean property convention
            String capitalizedField = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                entityClass.getMethod("get" + capitalizedField);
                return true;
            } catch (NoSuchMethodException e) {
                // Getter not found, continue
            }
            
            try {
                entityClass.getMethod("is" + capitalizedField);
                return true;
            } catch (NoSuchMethodException e) {
                // Boolean getter not found, continue
            }
            
            // Try case-insensitive match with entity's attributes
            for (jakarta.persistence.metamodel.Attribute<?, ?> attr : 
                    entityManager.getMetamodel().entity(entityClass).getAttributes()) {
                if (attr.getName().equalsIgnoreCase(fieldName)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.debug("Error checking field existence: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Cache entry with expiration support
     */
    private static class CacheEntry<T> {
        private final T value;
        private final long timestamp;
        
        public CacheEntry(T value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
        
        public T getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TIMEOUT;
        }
    }

    /**
     * Converts a value to the appropriate type with proper error handling
     */
    public Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType == null || targetType.isInstance(value)) {
            return value;
        }
        
        String stringValue = value.toString().trim();
        
        try {
            // Handle numeric types
            if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(stringValue.replaceAll("[,_]", ""));
            }
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(stringValue.replaceAll("[,_]", ""));
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(stringValue.replaceAll("[,_]", ""));
            }
            if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(stringValue.replaceAll("[,_]", ""));
            }
            if (targetType == java.math.BigDecimal.class) {
                return new java.math.BigDecimal(stringValue.replaceAll("[,_]", ""));
            }
            
            // Handle boolean
            if (targetType == Boolean.class || targetType == boolean.class) {
                stringValue = stringValue.toLowerCase();
                return stringValue.matches("true|yes|1|on");
            }
            
            // Handle date/time
            if (targetType == LocalDateTime.class) {
                return parseDateTime(stringValue);
            }
            
            if (targetType == LocalDate.class) {
                return parseDate(stringValue);
            }
            
            // Handle enums
            if (targetType.isEnum()) {
                return parseEnum(targetType, stringValue);
            }
            
        } catch (Exception e) {
            log.warn("Failed to convert value '{}' to type {}: {}", 
                stringValue, targetType, e.getMessage());
        }
        
        return value;
    }
    
    /**
     * Attempts to parse a date string with multiple formats
     */
    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                // Try next format
            }
        }
        
        throw new IllegalArgumentException("Could not parse date: " + dateStr);
    }
    
    /**
     * Attempts to parse a datetime string with multiple formats
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception e) {
                // Try next format
            }
        }
        
        throw new IllegalArgumentException("Could not parse datetime: " + dateTimeStr);
    }
    
    /**
     * Attempts to parse a string as an enum value with case insensitivity
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object parseEnum(Class<?> enumType, String value) {
        try {
            // Try exact match first
            return Enum.valueOf((Class<Enum>) enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try case-insensitive search
            Enum<?>[] constants = (Enum<?>[]) enumType.getEnumConstants();
            return Arrays.stream(constants)
                .filter(constant -> constant.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No enum constant " + enumType.getName() + "." + value));
        }
    }
    
    /**
     * Clears the field existence cache - useful for testing
     */
    public void clearCache() {
        fieldExistenceCache.clear();
    }
}
