package vn.com.fecredit.app.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.service.TableActionService;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableAction;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.TableRow;
import vn.com.fecredit.app.service.dto.UploadFile;
import vn.com.fecredit.app.service.factory.RepositoryFactory;

/**
 * Implementation of the TableActionService for processing table actions.
 * Supports actions like adding, updating, deleting, exporting, and importing
 * entities.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TableActionServiceImpl implements TableActionService {

    @PersistenceContext
    private EntityManager entityManager;

    private final RepositoryFactory repositoryFactory;

    private final TableDataServiceImpl tableDataService;

    @Override
    @Transactional
    public TableActionResponse processAction(TableActionRequest request) {
        if (request == null) {
            return TableActionResponse.error(null, "Request cannot be null");
        }

        try {
            // Check which action to process
            switch (request.getAction()) {
                case ADD:
                    return processAddAction(request);
                case UPDATE:
                    return processUpdateAction(request);
                case DELETE:
                    return processDeleteAction(request);
                case EXPORT:
                    return processExportAction(request);
                case IMPORT:
                    return processImportAction(request);
                case VIEW:
                    // For VIEW, we can reuse the table data service
                    return processViewAction(request);
                default:
                    return TableActionResponse.error(request, "Unsupported action: " + request.getAction());
            }
        } catch (Exception e) {
            log.error("Error processing table action", e);
            return TableActionResponse.error(request, "Error processing action: " + e.getMessage());
        }
    }

    /**
     * Process an ADD action request
     */
    @Transactional
    private <T extends AbstractStatusAwareEntity> TableActionResponse processAddAction(TableActionRequest request) {
        try {
            // Get the entity class
            Class<T> entityClass = repositoryFactory.getEntityClass(request.getObjectType());

            // Create a new instance of the entity
            T entity = createEntityFromData(request.getData(), entityClass);

            // Save the entity
            entity = saveEntity(entity);

            // Convert the saved entity to a TableRow
            TableRow savedRow = convertEntityToTableRow(entity);

            return TableActionResponse.success(
                    request,
                    "Successfully created " + request.getObjectType() + " with ID: " + entity.getId(),
                    savedRow);
        } catch (Exception e) {
            log.error("Error processing ADD action", e);
            return TableActionResponse.error(request, "Failed to add entity: " + e.getMessage());
        }
    }

    /**
     * Process an UPDATE action request
     */
    @Transactional
    private <T extends AbstractStatusAwareEntity> TableActionResponse processUpdateAction(TableActionRequest request) {
        try {
            // Get the entity ID from the request data
            Map<String, Object> data = request.getData().getData();
            if (data == null || !data.containsKey("id")) {
                return TableActionResponse.error(request, "Update request must include entity ID");
            }

            Long id = Long.valueOf(data.get("id").toString());

            // Get the entity class
            Class<T> entityClass = repositoryFactory.getEntityClass(request.getObjectType());

            // Find the existing entity
            T existingEntity = entityManager.find(entityClass, id);
            if (existingEntity == null) {
                return TableActionResponse.error(
                        request,
                        "Entity not found with ID: " + id);
            }

            // Update the entity fields
            updateEntityFromData(existingEntity, request.getData(), entityClass);

            // Save the updated entity
            existingEntity = saveEntity(existingEntity);

            // Convert the updated entity to a TableRow
            TableRow updatedRow = convertEntityToTableRow(existingEntity);

            return TableActionResponse.success(
                    request,
                    "Successfully updated " + request.getObjectType() + " with ID: " + id,
                    updatedRow);
        } catch (Exception e) {
            log.error("Error processing UPDATE action", e);
            return TableActionResponse.error(request, "Failed to update entity: " + e.getMessage());
        }
    }

    /**
     * Process a DELETE action request
     */
    @Transactional
    private <T extends AbstractStatusAwareEntity> TableActionResponse processDeleteAction(TableActionRequest request) {
        try {
            // Get the entity ID from the request data
            Map<String, Object> data = request.getData().getData();
            if (data == null || !data.containsKey("id")) {
                return TableActionResponse.error(request, "Delete request must include entity ID");
            }

            Long id = Long.valueOf(data.get("id").toString());

            // Get the entity class
            Class<T> entityClass = repositoryFactory.getEntityClass(request.getObjectType());

            // Find the entity to delete
            T entityToDelete = entityManager.find(entityClass, id);
            if (entityToDelete == null) {
                return TableActionResponse.error(
                        request,
                        "Entity not found with ID: " + id);
            }

            // For soft delete, we can set status to INACTIVE/DELETED instead of physically
            // removing
            if (data.containsKey("softDelete") && Boolean.TRUE.equals(data.get("softDelete"))) {
                entityToDelete.setStatus(CommonStatus.INACTIVE);
                entityManager.merge(entityToDelete);

                return TableActionResponse.success(
                        request,
                        "Successfully deactivated " + request.getObjectType() + " with ID: " + id,
                        convertEntityToTableRow(entityToDelete));
            } else {
                // Hard delete
                entityManager.remove(entityToDelete);

                // Create simple response row with just the ID
                TableRow deletedRow = new TableRow();
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("id", id);
                deletedRow.setData(responseData);

                return TableActionResponse.success(
                        request,
                        "Successfully deleted " + request.getObjectType() + " with ID: " + id,
                        deletedRow);
            }

        } catch (Exception e) {
            log.error("Error processing DELETE action", e);
            return TableActionResponse.error(request, "Failed to delete entity: " + e.getMessage());
        }
    }

    /**
     * Process an EXPORT action request
     */
    private TableActionResponse processExportAction(TableActionRequest request) {
        try {
            // First fetch the data to export using TableDataService
            TableFetchRequest fetchRequest = new TableFetchRequest();
            fetchRequest.setObjectType(request.getObjectType());
            fetchRequest.setEntityName(request.getEntityName());
            fetchRequest.setPage(0);
            // Setting a large page size for export
            fetchRequest.setSize(10000);
            fetchRequest.setSorts(request.getSorts());
            fetchRequest.setFilters(request.getFilters());
            fetchRequest.setSearch(request.getSearch());

            TableFetchResponse fetchResponse = tableDataService.fetchData(fetchRequest);

            if (fetchResponse.getStatus() != FetchStatus.SUCCESS &&
                    fetchResponse.getStatus() != FetchStatus.NO_DATA) {
                return TableActionResponse.error(
                        request,
                        "Failed to fetch data for export: " + fetchResponse.getMessage());
            }

            // Create Excel workbook
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(request.getObjectType().toString());

                // Create header row with column names
                Map<String, String> columnMap = createHeaderRow(workbook, sheet, fetchResponse);

                // Add data rows
                addDataRows(workbook, sheet, fetchResponse, columnMap);

                // Auto-size columns
                for (int i = 0; i < columnMap.size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // Write to byte array
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);

                // Create the download file
                UploadFile downloadFile = new UploadFile();

                // Generate a filename with timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                downloadFile.setFileName(request.getObjectType() + "_" + timestamp + ".xlsx");
                downloadFile.setFileContent(outputStream.toByteArray());

                return TableActionResponse.withFile(request, downloadFile);
            }
        } catch (Exception e) {
            log.error("Error processing EXPORT action", e);
            return TableActionResponse.error(request, "Failed to export data: " + e.getMessage());
        }
    }

    /**
     * Process an IMPORT action request
     */
    @Transactional
    private <T extends AbstractStatusAwareEntity> TableActionResponse processImportAction(TableActionRequest request) {
        try {
            // Check if we have an upload file
            if (request.getUploadFile() == null ||
                    request.getUploadFile().getFileContent() == null ||
                    request.getUploadFile().getFileContent().length == 0) {
                return TableActionResponse.error(request, "Import request must include a file");
            }

            // Get the entity class
            Class<T> entityClass = repositoryFactory.getEntityClass(request.getObjectType());

            // Get column mapping
            TableFetchRequest emptyRequest = new TableFetchRequest();
            emptyRequest.setObjectType(request.getObjectType());
            emptyRequest.setPage(0);
            emptyRequest.setSize(1);

            TableFetchResponse metadataResponse = tableDataService.fetchData(emptyRequest);
            Map<String, Object> columnMapping = new HashMap<>();

            if (metadataResponse.getFieldNameMap() != null) {
                metadataResponse.getFieldNameMap().forEach((key, info) -> {
                    columnMapping.put(info.getFieldName().toLowerCase(), key);
                });
            }

            // Parse the Excel file
            List<Map<String, Object>> importedRecords = parseExcelFile(
                    request.getUploadFile().getFileContent(),
                    columnMapping);

            // Process the imported records
            List<T> savedEntities = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (Map<String, Object> record : importedRecords) {
                try {
                    T entity = createEntityFromMap(record, entityClass);
                    entity = saveEntity(entity);
                    savedEntities.add(entity);
                } catch (Exception e) {
                    errors.add("Error importing record: " + e.getMessage() + ", Data: " + record);
                }
            }

            // Create response
            String message = String.format(
                    "Import completed. Imported %d records successfully. %d records failed.",
                    savedEntities.size(), errors.size());

            // Add first error if any
            if (!errors.isEmpty()) {
                message += " First error: " + errors.get(0);
            }

            TableRow resultRow = new TableRow();
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("totalRecords", importedRecords.size());
            resultData.put("successCount", savedEntities.size());
            resultData.put("errorCount", errors.size());
            resultData.put("errors", errors);
            resultRow.setData(resultData);

            return new TableActionResponse(
                    request,
                    errors.isEmpty() ? FetchStatus.SUCCESS : FetchStatus.ERROR,
                    message,
                    resultRow,
                    null);
        } catch (Exception e) {
            log.error("Error processing IMPORT action", e);
            return TableActionResponse.error(request, "Failed to import data: " + e.getMessage());
        }
    }

    /**
     * Process a VIEW action request
     */
    private TableActionResponse processViewAction(TableActionRequest request) {
        try {
            // Convert TableActionRequest to TableFetchRequest
            TableFetchRequest fetchRequest = new TableFetchRequest();
            fetchRequest.setObjectType(request.getObjectType());
            fetchRequest.setEntityName(request.getEntityName());
            fetchRequest.setPage(0);
            fetchRequest.setSize(1);
            fetchRequest.setSorts(request.getSorts());
            fetchRequest.setFilters(request.getFilters());
            fetchRequest.setSearch(request.getSearch());

            // Use the existing tableDataService to fetch the data
            TableFetchResponse fetchResponse = tableDataService.fetchData(fetchRequest);

            // If we found data, return the first row
            if (fetchResponse.getStatus() == FetchStatus.SUCCESS &&
                    fetchResponse.getRows() != null &&
                    !fetchResponse.getRows().isEmpty()) {

                return TableActionResponse.success(
                        request,
                        "Successfully fetched " + request.getObjectType(),
                        fetchResponse.getRows().get(0));
            } else {
                return TableActionResponse.error(
                        request,
                        "No data found for the given criteria");
            }
        } catch (Exception e) {
            log.error("Error processing VIEW action", e);
            return TableActionResponse.error(request, "Failed to view entity: " + e.getMessage());
        }
    }

    /**
     * Create a header row in the Excel sheet
     */
    private Map<String, String> createHeaderRow(Workbook workbook, Sheet sheet, TableFetchResponse response) {
        Map<String, String> columnMap = new HashMap<>();

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Create header row
        Row headerRow = sheet.createRow(0);

        // First collect all column keys from response
        if (response.getFieldNameMap() != null) {
            int colIndex = 0;
            for (Map.Entry<String, vn.com.fecredit.app.service.dto.ColumnInfo> entry : response.getFieldNameMap()
                    .entrySet()) {

                // Skip complex object fields
                if ("OBJECT".equals(entry.getValue().getFieldType())) {
                    continue;
                }

                String columnKey = entry.getKey();
                String displayName = entry.getValue().getFieldName();

                // Create header cell
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(displayName);
                cell.setCellStyle(headerStyle);

                // Map display name to field key
                columnMap.put(displayName, columnKey);
            }
        }
        return columnMap;
    }

    /**
     * Add data rows to the Excel sheet
     */
    private void addDataRows(Workbook workbook, Sheet sheet,
            TableFetchResponse response, Map<String, String> columnMap) {

        // Create normal style for data
        CellStyle dataStyle = workbook.createCellStyle();

        // Create date style for date fields
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));

        // Add data rows
        int rowIndex = 1;
        if (response.getRows() != null) {
            for (TableRow tableRow : response.getRows()) {
                Row row = sheet.createRow(rowIndex++);

                // Add cells for each column
                int colIndex = 0;
                for (Map.Entry<String, String> entry : columnMap.entrySet()) {
                    String displayName = entry.getKey();
                    String fieldName = entry.getValue();

                    Cell cell = row.createCell(colIndex++);

                    // Get value from tableRow data
                    Object value = tableRow.getData().get(fieldName);

                    setCellValue(cell, value);
                }
            }
        }
    }

    /**
     * Set cell value based on value type
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Parse Excel file into a list of records
     */
    private List<Map<String, Object>> parseExcelFile(byte[] fileContent, Map<String, Object> columnMapping) {
        List<Map<String, Object>> records = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileContent))) {
            Sheet sheet = workbook.getSheetAt(0);

            // Get header row
            Row headerRow = sheet.getRow(0);
            Map<Integer, String> headerMap = new HashMap<>();

            // Map column indices to field names
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerValue = cell.getStringCellValue();
                    headerMap.put(i, headerValue);
                }
            }

            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Map<String, Object> record = new HashMap<>();
                    boolean hasData = false;

                    for (int j = 0; j < headerMap.size(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            String headerName = headerMap.get(j);
                            // Get the field name from column mapping
                            Object fieldNameObj = columnMapping.get(headerName.toLowerCase());

                            if (fieldNameObj != null) {
                                String fieldName = fieldNameObj.toString();
                                Object value = getCellValue(cell);
                                if (value != null) {
                                    record.put(fieldName, value);
                                    hasData = true;
                                }
                            }
                        }
                    }

                    // Only add records that have data
                    if (hasData) {
                        records.add(record);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Excel file", e);
            throw new RuntimeException("Error parsing Excel file: " + e.getMessage(), e);
        }

        return records;
    }

    /**
     * Get cell value based on cell type
     */
    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * Create an entity from TableRow data
     */
    private <T extends AbstractStatusAwareEntity> T createEntityFromData(
            TableRow tableRow, Class<T> entityClass) throws Exception {

        if (tableRow == null || tableRow.getData() == null) {
            throw new IllegalArgumentException("Entity data cannot be null");
        }

        return createEntityFromMap(tableRow.getData(), entityClass);
    }

    /**
     * Create an entity from a map of field values
     */
    private <T extends AbstractStatusAwareEntity> T createEntityFromMap(
            Map<String, Object> data, Class<T> entityClass) throws Exception {

        T entity = entityClass.getDeclaredConstructor().newInstance();

        // Update fields
        updateEntityFromMap(entity, data);

        return entity;
    }

    /**
     * Update entity fields from TableRow data
     */
    private <T extends AbstractStatusAwareEntity> void updateEntityFromData(
            T entity, TableRow tableRow, Class<T> entityClass) throws Exception {

        if (tableRow == null || tableRow.getData() == null) {
            throw new IllegalArgumentException("Update data cannot be null");
        }

        // Update fields
        updateEntityFromMap(entity, tableRow.getData());
    }

    /**
     * Update entity fields from a map of field values
     */
    private <T> void updateEntityFromMap(T entity, Map<String, Object> data) throws Exception {
        // Get all fields from entity class and its superclasses
        List<Field> allFields = getAllFields(entity.getClass());

        for (Field field : allFields) {
            String fieldName = field.getName();

            if (data.containsKey(fieldName)) {
                field.setAccessible(true);
                Object value = data.get(fieldName);

                // Skip null values and id field for existing entities
                if (value == null || (fieldName.equals("id") && value.toString().isEmpty())) {
                    continue;
                }

                // Convert value to appropriate type if needed
                value = convertValueToFieldType(value, field.getType());

                field.set(entity, value);
            }
        }
    }

    /**
     * Get all fields from a class including its superclasses
     */
    private List<Field> getAllFields(Class<?> clazz) {
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
    private Object convertValueToFieldType(Object value, Class<?> fieldType) {
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
     * Save an entity using the appropriate repository
     */
    private <T extends AbstractStatusAwareEntity> T saveEntity(T entity) {
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
    private TableRow convertEntityToTableRow(Object entity) {
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
     * Check if a class is a primitive or wrapper type
     */
    private boolean isPrimitiveOrWrapper(Class<?> type) {
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
