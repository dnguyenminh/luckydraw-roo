package vn.com.fecredit.app.service.impl.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.ColumnInfo;
import vn.com.fecredit.app.service.dto.DataObject;
import vn.com.fecredit.app.service.dto.DataObjectKey;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.ObjectType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.dto.TableRow;

/**
 * Responsible for building response objects from query results
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ResponseBuilder {

    // private final RelatedTablesFactory relatedTablesFactory;

    /**
     * Create an error response with a message
     *
     * @param message Error message
     * @return Error response
     */
    public TableFetchResponse createErrorResponse(String message) {
        log.error("Table data fetch error: {}", message);

        TableFetchResponse response = new TableFetchResponse();
        response.setStatus(FetchStatus.ERROR);
        response.setMessage(message);
        response.setTotalPage(0);
        response.setCurrentPage(0);
        response.setPageSize(0);
        response.setTotalElements(0L);
        response.setRows(new ArrayList<>());
        response.setRelatedLinkedObjects(new HashMap<>());

        return response;
    }

    /**
     * Create a response for scalar property queries
     */
    public TableFetchResponse buildResponse(
            TableFetchRequest request, 
            List<Tuple> tuples, 
            Class<?> entityClass, 
            long totalCount, 
            String tableName) {
        
        try {
            // Create field mapping for view columns if provided
            Map<String, String> fieldAliasMapping = new HashMap<>();
            Map<String, ColumnInfo> fieldNameMap = new HashMap<>();
            
            // Populate field name map from view columns
            if (request.getViewColumns() != null) {
                for (ColumnInfo col : request.getViewColumns()) {
                    // Add to field name map
                    fieldNameMap.put(col.getFieldName(), col);
                    
                    // Map underscore version back to dot notation
                    String sqlSafeAlias = col.getFieldName().replace(".", "_");
                    fieldAliasMapping.put(sqlSafeAlias, col.getFieldName());
                }
            } else {
                // If no view columns specified, try to infer from first tuple
                if (!tuples.isEmpty()) {
                    Tuple firstTuple = tuples.get(0);
                    for (TupleElement<?> element : firstTuple.getElements()) {
                        String alias = element.getAlias();
                        Class<?> type = element.getJavaType();
                        
                        // Create column info based on Java type
                        String fieldType = determineFieldType(type);
                        ColumnInfo columnInfo = new ColumnInfo(alias, fieldType, vn.com.fecredit.app.service.dto.SortType.NONE);
                        fieldNameMap.put(alias, columnInfo);
                    }
                }
            }

            // Convert tuples to table rows
            List<TableRow> rows = convertTupleToRows(tuples, fieldAliasMapping);

            // Build response with proper status based on results
            TableFetchResponse response = new TableFetchResponse();
            response.setOriginalRequest(request);
            response.setTableName(tableName);
            response.setRows(rows);
            response.setTotalElements(totalCount);
            response.setCurrentPage(request.getPage());
            response.setPageSize(request.getSize());
            response.setTotalPage((int)(totalCount/Math.max(1, request.getSize()) + (totalCount % Math.max(1, request.getSize()) > 0 ? 1 : 0)));
            response.setFieldNameMap(fieldNameMap);  // Set the field name map in the response
            
            // Set status based on context
            if (rows == null || rows.isEmpty()) {
                // Check if this is a search with filters that might explain empty results
                boolean isSearchWithFilters = request.getSearch() != null && !request.getSearch().isEmpty();
                
                if (totalCount > 0 && isSearchWithFilters) {
                    // Success with filters - total data exists but filters caused empty results
                    response.setStatus(FetchStatus.SUCCESS);
                    response.setMessage("No results match the search criteria");
                    log.debug("Setting status to SUCCESS despite empty result rows because search filters applied and total count is {}", totalCount);
                } else {
                    // No data found
                    response.setStatus(FetchStatus.NO_DATA);
                    response.setMessage("No data found");
                    log.debug("Setting status to NO_DATA because result rows is empty");
                }
            } else {
                response.setStatus(FetchStatus.SUCCESS);
                log.debug("Setting status to SUCCESS with {} data rows", rows.size());
            }
            
            // Preserve original related linked objects if present
            if (request.getSearch() != null) {
                response.setRelatedLinkedObjects(request.getSearch());
            }
            
            log.info("Built response with status: {}, rows: {}, total: {}", 
                      response.getStatus(), rows != null ? rows.size() : 0, totalCount);
            
            return response;
        } catch (Exception e) {
            log.error("Error building response: {}", e.getMessage(), e);
            return createErrorResponse("Error building response: " + e.getMessage());
        }
    }

    /**
     * Determine field type name based on Java class
     */
    private String determineFieldType(Class<?> javaType) {
        if (javaType == null) {
            return "STRING";
        }
        
        if (Number.class.isAssignableFrom(javaType) || 
            javaType == int.class || 
            javaType == long.class || 
            javaType == double.class || 
            javaType == float.class) {
            return "NUMBER";
        }
        
        if (java.util.Date.class.isAssignableFrom(javaType) || 
            java.time.temporal.Temporal.class.isAssignableFrom(javaType)) {
            return "DATETIME";
        }
        
        if (Boolean.class.isAssignableFrom(javaType) || javaType == boolean.class) {
            return "BOOLEAN";
        }
        
        return "STRING";
    }

    /**
     * Convert tuple results to table rows, preserving the original field names when necessary
     */
    private List<TableRow> convertTupleToRows(List<Tuple> tuples, Map<String, String> fieldAliasMapping) {
        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<TableRow> rows = new ArrayList<>();

        for (Tuple tuple : tuples) {
            TableRow row = new TableRow();
            // Initialize the data map here to prevent NullPointerException
            row.setData(new HashMap<>());

            // Always include the viewId if available
            Object id = tuple.get("id");
            if (id != null) {
                row.getData().put("viewId", id.toString());
            }

            // Process all elements in the tuple
            for (TupleElement<?> element : tuple.getElements()) {
                String alias = element.getAlias();
                // Use the original field name (with dots) if available in mapping
                String fieldName = fieldAliasMapping.getOrDefault(alias, alias);

                // Add the value to the row data
                addFieldToRow(row, tuple, fieldName);
            }

            rows.add(row);
        }

        return rows;
    }

    /**
     * Maps field paths to proper column aliases for result processing
     */
    private String getColumnAlias(String fieldName) {
        // Convert dots to underscores for consistent handling of aliases
        return fieldName.replace('.', '_');
    }

    /**
     * Adds a field from a tuple result to a row
     */
    private void addFieldToRow(TableRow row, Tuple tuple, String fieldName) {
        try {
            // Ensure the data map is initialized
            if (row.getData() == null) {
                row.setData(new HashMap<>());
            }
            
            // Convert the field name to the expected alias format
            String alias = getColumnAlias(fieldName);
            Object value = tuple.get(alias);
            
            // Use original fieldName as the key in the row data
            row.getData().put(fieldName, value);
            log.debug("Added field: {} (alias: {}) with value: {}", fieldName, alias, value);
        } catch (Exception e) {
            log.warn("Error adding field {} from tuple: {}", fieldName, e.getMessage());
        }
    }

    /**
     * Build a response from entity results
     *
     * @param request       Original request
     * @param rows          Table rows
     * @param page          Page of entities
     * @param tableName     Table name
     * @param columnInfoMap Column info map
     * @return Response object
     */
    public TableFetchResponse buildEntityResponse(
            TableFetchRequest request,
            List<TableRow> rows,
            Page<?> page,
            String tableName,
            Map<String, ColumnInfo> columnInfoMap) {

        // Create response
        TableFetchResponse response = new TableFetchResponse();
        response.setStatus(page.isEmpty() ? FetchStatus.NO_DATA : FetchStatus.SUCCESS);
        response.setTotalElements(page.getTotalElements());
        response.setTotalPage(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTableName(tableName);
        response.setOriginalRequest(request);
        response.setRows(rows);
        response.setFieldNameMap(columnInfoMap);

        // Add related linked objects if search criteria exists
        response.setRelatedLinkedObjects(populateRelatedLinkedObjects(request));

        // Set key info
        DataObjectKey key = new DataObjectKey();
        key.setKeys(Collections.singletonList("id")); // Default key
        response.setKey(key);

        return response;
    }

    /**
     * Populate related linked objects from search criteria
     *
     * @param request The table fetch request
     * @return Map of related objects by type
     */
    private Map<ObjectType, DataObject> populateRelatedLinkedObjects(TableFetchRequest request) {
        Map<ObjectType, DataObject> relatedLinkedObjects = new HashMap<>();

        // Use search criteria as the basis for related linked objects
        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            for (Map.Entry<ObjectType, DataObject> entry : request.getSearch().entrySet()) {
                ObjectType objectType = entry.getKey();
                DataObject searchData = entry.getValue();

                if (objectType != null && searchData != null) {
                    // Ensure the DataObject has a properly initialized key
                    if (searchData.getKey() == null) {
                        DataObjectKey key = new DataObjectKey();
                        List<String> keyValues = new ArrayList<>();

                        // Extract primary key values from the data if available
                        if (searchData.getData() != null && searchData.getData().getData() != null) {
                            Map<String, Object> data = searchData.getData().getData();
                            if (data.containsKey("id")) {
                                keyValues.add("id");
                            }
                        }

                        key.setKeys(keyValues);
                        searchData.setKey(key);
                    }

                    // Add to related linked objects
                    relatedLinkedObjects.put(objectType, searchData);
                }
            }
        }

        return relatedLinkedObjects;
    }
}
