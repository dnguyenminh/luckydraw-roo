package vn.com.fecredit.app.service.impl.action;

import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;

/**
 * Interface for handling different types of table actions
 */
public interface TableActionHandler {
    /**
     * Process a table action request
     * @param request The action request to process
     * @return The response after processing the request
     */
    TableActionResponse handle(TableActionRequest request);
}
