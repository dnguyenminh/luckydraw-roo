package vn.com.fecredit.app.service.impl.table;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.service.dto.SortRequest;
import vn.com.fecredit.app.service.dto.SortType;
import vn.com.fecredit.app.service.dto.TableFetchRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to handle pagination-related operations
 */
@Component
@RequiredArgsConstructor
public class PaginationHelper {

    /**
     * Create a pageable object from the request for sorting and pagination
     * 
     * @param request The table fetch request containing pagination and sorting info
     * @return A configured Pageable object
     */
    public Pageable createPageable(TableFetchRequest request) {
        List<Order> orders = new ArrayList<>();

        // Process sort requests if provided
        if (request.getSorts() != null && !request.getSorts().isEmpty()) {
            for (SortRequest sortRequest : request.getSorts()) {
                Direction direction = Direction.ASC;
                if (sortRequest.getSortType() == SortType.DESCENDING) {
                    direction = Direction.DESC;
                }
                orders.add(new Order(direction, sortRequest.getField()));
            }
        }

        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
    
    /**
     * Normalize page and size values to prevent invalid pagination parameters
     * 
     * @param page The requested page number
     * @param size The requested page size
     * @return An array with normalized [page, size] values
     */
    public int[] normalizePageableParams(int page, int size) {
        // Ensure page is not negative
        int normalizedPage = Math.max(0, page);
        
        // Ensure size is reasonable (between 1 and 1000)
        int normalizedSize = Math.min(1000, Math.max(1, size));
        
        return new int[]{ normalizedPage, normalizedSize };
    }
}
