package vn.com.fecredit.app.service.impl.action;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.service.dto.FetchStatus;
import vn.com.fecredit.app.service.dto.TableActionRequest;
import vn.com.fecredit.app.service.dto.TableActionResponse;
import vn.com.fecredit.app.service.dto.TableFetchRequest;
import vn.com.fecredit.app.service.dto.TableFetchResponse;
import vn.com.fecredit.app.service.impl.TableDataServiceImpl;

/**
 * Handler for VIEW actions
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ViewActionHandler implements TableActionHandler {

    private final TableDataServiceImpl tableDataService;

    @Override
    public TableActionResponse handle(TableActionRequest request) {
        try {
            // Convert TableActionRequest to TableFetchRequest
            TableFetchRequest fetchRequest = new TableFetchRequest();
            fetchRequest.setObjectType(request.getObjectType());
            fetchRequest.setEntityName(request.getEntityName());
            fetchRequest.setPage(0);
            fetchRequest.setSize(1);
            fetchRequest.setSorts(request.getSorts());
            fetchRequest.setFilters(request.getFilters());
            fetchRequest.setSearch(request.getSearch());

            // Use the existing tableDataService to fetch the data
            TableFetchResponse fetchResponse = tableDataService.fetchData(fetchRequest);

            // If we found data, return the first row
            if (fetchResponse.getStatus() == FetchStatus.SUCCESS &&
                    fetchResponse.getRows() != null &&
                    !fetchResponse.getRows().isEmpty()) {

                return TableActionResponse.success(
                        request,
                        "Successfully fetched " + request.getObjectType(),
                        fetchResponse.getRows().get(0));
            } else {
                return TableActionResponse.error(
                        request,
                        "No data found for the given criteria");
            }
        } catch (Exception e) {
            log.error("Error processing VIEW action", e);
            return TableActionResponse.error(request, "Failed to view entity: " + e.getMessage());
        }
    }
}
