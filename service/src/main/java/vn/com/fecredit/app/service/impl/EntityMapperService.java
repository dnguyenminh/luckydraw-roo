package vn.com.fecredit.app.service.impl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractPersistableEntity;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

/**
 * Service for handling entity to/from data mapping operations
 */
@Service
@Slf4j
public class EntityMapperService {

    /**
     * Create an entity from TableRow data
     */
    public <T extends AbstractPersistableEntity<?>> T createEntityFromData(
            TableRow tableRow, Class<T> entityClass) throws Exception {

        if (tableRow == null || tableRow.getData() == null) {
            throw new IllegalArgumentException("Entity data cannot be null");
        }

        return createEntityFromMap(tableRow.getData(), entityClass);
    }

    /**
     * Create an entity from a map of field values
     */
    public <T extends AbstractPersistableEntity<?>> T createEntityFromMap(
            Map<String, Object> data, Class<T> entityClass) throws Exception {

        T entity = entityClass.getDeclaredConstructor().newInstance();

        // Update fields
        updateEntityFromMap(entity, data);

        return entity;
    }

    /**
     * Update entity fields from TableRow data
     */
    public <T extends AbstractPersistableEntity<?>> void updateEntityFromData(
            T entity, TableRow tableRow) throws Exception {

        if (tableRow == null || tableRow.getData() == null) {
            throw new IllegalArgumentException("Update data cannot be null");
        }

        // Update fields
        updateEntityFromMap(entity, tableRow.getData());
    }

    /**
     * Update entity fields from a map of field values
     */
    public <T> void updateEntityFromMap(T entity, Map<String, Object> data) throws Exception {
        // Get all fields from entity class and its superclasses
        List<Field> allFields = getAllFields(entity.getClass());

        for (Field field : allFields) {
            String fieldName = field.getName();

            if (data.containsKey(fieldName)) {
                field.setAccessible(true);
                Object value = data.get(fieldName);

                // Skip null values and id field for existing entities
                if (value == null || fieldName.equals("id")) {
                    continue;
                }

                // Convert value to appropriate type if needed
                value = convertValueToFieldType(value, field.getType());

                field.set(entity, value);
            }
        }
    }

    /**
     * Save an entity using the appropriate repository
     */
    public <T extends AbstractPersistableEntity<?>> T saveEntity(T entity, RepositoryFactory repositoryFactory) {
        @SuppressWarnings("unchecked")
        Class<T> entityClass = (Class<T>) entity.getClass();

        try {
            // Get repository for this entity type
            var repository = repositoryFactory.getRepositoryForClass(entityClass);

            // Save the entity
            return repository.save(entity);
        } catch (Exception e) {
            log.error("Error saving entity", e);
            throw new RuntimeException("Error saving entity: " + e.getMessage(), e);
        }
    }

    /**
     * Convert an entity to a TableRow
     */
    public TableRow convertEntityToTableRow(Object entity) {
        // Create a new TableRow
        TableRow tableRow = new TableRow();
        Map<String, Object> data = new HashMap<>();

        try {
            // Get all fields from entity class and its superclasses
            for (Field field : getAllFields(entity.getClass())) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(entity);

                // Skip complex objects
                if (value != null &&
                        !isPrimitiveOrWrapper(field.getType()) &&
                        !field.getType().equals(String.class) &&
                        !field.getType().isEnum() &&
                        !(value instanceof LocalDateTime)) {
                    continue;
                }

                data.put(fieldName, value);
            }

            tableRow.setData(data);
        } catch (Exception e) {
            log.error("Error converting entity to TableRow", e);
            throw new RuntimeException("Error converting entity to TableRow: " + e.getMessage(), e);
        }

        return tableRow;
    }

    /**
     * Get all fields from a class including its superclasses
     */
    public List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(List.of(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * Convert a value to the appropriate field type
     */
    public Object convertValueToFieldType(Object value, Class<?> fieldType) {
        if (value == null) {
            return null;
        }

        // Already the correct type
        if (fieldType.isInstance(value)) {
            return value;
        }

        String stringValue = value.toString();

        if (String.class.equals(fieldType)) {
            return stringValue;
        } else if (Long.class.equals(fieldType) || long.class.equals(fieldType)) {
            return Long.valueOf(stringValue);
        } else if (Integer.class.equals(fieldType) || int.class.equals(fieldType)) {
            return Integer.valueOf(stringValue);
        } else if (Double.class.equals(fieldType) || double.class.equals(fieldType)) {
            return Double.valueOf(stringValue);
        } else if (Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) {
            return Boolean.valueOf(stringValue);
        } else if (LocalDateTime.class.equals(fieldType)) {
            if (stringValue.isEmpty()) {
                return null;
            }
            if (stringValue.contains("T")) {
                return LocalDateTime.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                return LocalDateTime.parse(stringValue + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        } else if (fieldType.isEnum()) {
            // Handle enum conversion
            try {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                Object enumValue = Enum.valueOf((Class<Enum>) fieldType, stringValue);
                return enumValue;
            } catch (Exception e) {
                log.warn("Failed to convert value to enum: {}", stringValue);
                return null;
            }
        }

        return value;
    }

    /**
     * Check if a class is a primitive or wrapper type
     */
    public boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type == Integer.class ||
                type == Long.class ||
                type == Float.class ||
                type == Double.class ||
                type == Boolean.class ||
                type == Character.class ||
                type == Byte.class ||
                type == Short.class;
    }
}
