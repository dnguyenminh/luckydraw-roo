package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for fetching paginated and sorted table data.
 * Defines parameters for what data to fetch and how to present it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableFetchRequest {

    /**
     * Type of object to fetch
     */
    private ObjectType objectType;

    /**
     * Add entityName property to support URL path parameters
     */
    private String entityName;

    /**
     * Page number (0-based) for pagination
     */
    @Builder.Default
    private int page = 0;

    /**
     * Number of items per page
     */
    @Builder.Default
    private int size = 10;

    /**
     * List of sort specifications (field and direction)
     */
    @Builder.Default
    private List<SortRequest> sorts = new ArrayList<>();

    /**
     * List of filter criteria to apply
     */
    @Builder.Default
    private List<FilterRequest> filters = new ArrayList<>();

    /**
     * Search criteria as field name to search value mapping
     */
    @Builder.Default
    private Map<ObjectType, DataObject> search = new HashMap<>();

    /**
     * Add a sort request
     *
     * @param field    the field to sort by
     * @param sortType the sort direction
     * @return this request for chaining
     */
    public TableFetchRequest addSort(String field, SortType sortType) {
        sorts.add(new SortRequest(field, sortType));
        return this;
    }

    /**
     * Add a filter request
     *
     * @param field      the field to filter
     * @param filterType the filter operation
     * @param minValue   minimum value for range operations
     * @param maxValue   maximum value for range operations
     * @return this request for chaining
     */
    public TableFetchRequest addFilter(String field, FilterType filterType, String minValue, String maxValue) {
        filters.add(new FilterRequest(field, filterType, minValue, maxValue));
        return this;
    }

    /**
     * Add a search parameter
     *
     * @param objectType the field to search in
     * @param dataObject the value to search for
     * @return this request for chaining
     */
    public TableFetchRequest addSearch(ObjectType objectType, DataObject dataObject) {
        search.put(objectType, dataObject);
        return this;
    }

    /**
     * Map of related object names to their metadata
     */
    @Builder.Default
    private List<ColumnInfo> viewColumns = new ArrayList<>();

}
