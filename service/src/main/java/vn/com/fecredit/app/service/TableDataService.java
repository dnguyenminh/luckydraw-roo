package vn.com.fecredit.app.service;

import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;

/**
 * Service for fetching table data and scalar properties
 */
public interface TableDataService {

    /**
     * Fetch paginated table data based on request parameters
     * 
     * @param request the table fetch request containing filters, sorts and pagination
     * @return response with paginated table data
     */
    TableFetchResponse fetchData(TableFetchRequest request);
    
    /**
     * Fetch scalar properties (counts, aggregations) for the specified entity type
     * These properties provide summarized data about the entities rather than the entities themselves
     * 
     * @param request the fetch request containing entity type and filters
     * @return response containing scalar property values in the statistics field
     */
    TableFetchResponse fetchScalarProperties(TableFetchRequest request);
}
