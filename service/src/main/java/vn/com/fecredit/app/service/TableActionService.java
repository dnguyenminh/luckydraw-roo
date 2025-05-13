package vn.com.fecredit.app.service;

import vn.com.fecredit.app.service.dto.ProgressCallback;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;

/**
 * Service for executing operations on table data.
 */
public interface TableActionService {
    
    /**
     * Execute a table action.
     * 
     * @param request The action request
     * @return The action response
     */
    TableActionResponse executeAction(TableActionRequest request);
    
    /**
     * Execute a table action with progress tracking.
     * 
     * @param request The action request
     * @param progressCallback Callback for progress updates
     * @return The action response
     */
    TableActionResponse executeActionWithProgress(
            TableActionRequest request, ProgressCallback progressCallback);
}
