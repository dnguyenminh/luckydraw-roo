package vn.com.fecredit.app.service.impl.action;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.impl.ExportService;
import vn.com.fecredit.app.service.impl.TableDataServiceImpl;

/**
 * Handler for EXPORT actions
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExportActionHandler implements TableActionHandler {

    private final ExportService exportService;
    private final TableDataServiceImpl tableDataService;

    @Override
    public TableActionResponse handle(TableActionRequest request) {
        return exportService.processExportAction(request, tableDataService);
    }
}
