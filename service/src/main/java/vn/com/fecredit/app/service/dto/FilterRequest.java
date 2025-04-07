package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a request to filter data.
 * Contains information about which field to filter and how to apply the filter.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The field to filter on
     */
    private String field;
    
    /**
     * The type of filter to apply
     */
    private FilterType filterType;
    
    /**
     * The minimum value for range filters
     */
    private String minValue;
    
    /**
     * The maximum value for range filters
     */
    private String maxValue;
    
    /**
     * Constructor with field and filter type
     *
     * @param field the field to filter on
     * @param filterType the type of filter to apply
     */
    public FilterRequest(String field, FilterType filterType) {
        this.field = field;
        this.filterType = filterType;
    }
    
    /**
     * Constructor for value-based filters
     *
     * @param field the field to filter on
     * @param filterType the type of filter to apply
     * @param minValue the filter value (or minimum value for range filters)
     */
    public FilterRequest(String field, FilterType filterType, String minValue) {
        this.field = field;
        this.filterType = filterType;
        this.minValue = minValue;
    }
}
