package vn.com.fecredit.app.service;


import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;

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
    TableFetchResponse fetchData(TableFetchRequest request);


}
