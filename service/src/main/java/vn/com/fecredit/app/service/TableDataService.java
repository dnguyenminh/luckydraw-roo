package vn.com.fecredit.app.service;

import vn.com.fecredit.app.dto.TableFetchRequest;
import vn.com.fecredit.app.dto.TableFetchResponse;

/**
 * Service for handling table data operations
 */
public interface TableDataService {
    /**
     * Fetch table data based on the provided request
     * 
     * @param request the table fetch request
     * @return the table data response
     */
    TableFetchResponse fetchTableData(TableFetchRequest request);

    /**
     * Fetch related table data
     * 
     * @param entityName the entity name
     * @param entityId the entity ID
     * @param relationName the relation name
     * @param request the table fetch request
     * @return the related table data
     */
    TableFetchResponse fetchRelatedTableData(
        String entityName, 
        Long entityId, 
        String relationName, 
        TableFetchRequest request
    );
}