package vn.com.fecredit.app.service.impl.action;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.service.dto.TableAction;
import vn.com.fecredit.app.service.dto.TableActionResponse;

/**
 * Factory for creating appropriate TableActionHandler based on action type
 */
@Component
@RequiredArgsConstructor
public class TableActionFactory {
    private final AddActionHandler addActionHandler;
    private final UpdateActionHandler updateActionHandler;
    private final DeleteActionHandler deleteActionHandler;
    private final ExportActionHandler exportActionHandler;
    private final ImportActionHandler importActionHandler;
    private final ViewActionHandler viewActionHandler;
    
    /**
     * Get the appropriate handler for the given action type
     */
    public TableActionHandler getHandler(TableAction actionType) {
        if (actionType == null) {
            return request -> TableActionResponse.error(request, "Action type cannot be null");
        }
        
        switch (actionType) {
            case ADD:
                return addActionHandler;
            case UPDATE:
                return updateActionHandler;
            case DELETE:
                return deleteActionHandler;
            case EXPORT:
                return exportActionHandler;
            case IMPORT:
                return importActionHandler;
            case VIEW:
                return viewActionHandler;
            default:
                return request -> TableActionResponse.error(request, "Unsupported action: " + request.getAction());
        }
    }
}
