package vn.com.fecredit.app.service;

import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;

/**
 * Service interface for handling table action operations.
 * Provides methods to process different table actions like add, update, delete, export, and import.
 */
public interface TableActionService {

    /**
     * Process a table action request and return the appropriate response.
     * 
     * @param request The action request containing operation details
     * @return Response containing the result of the operation
     */
    TableActionResponse processAction(TableActionRequest request);
}
