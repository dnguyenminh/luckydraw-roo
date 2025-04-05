package vn.com.fecredit.app.service;

import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;

/**
 * Service interface for fetching table data based on various criteria.
 * Provides methods for retrieving paginated, filtered, and sorted data.
 */
public interface TableDataService {
    
    /**
     * Fetch data based on request parameters
     * @param request the request containing search criteria, pagination, and sort information
     * @return response containing the requested data and metadata
     */
    TableFetchResponse fetchData(TableFetchRequest request);
}