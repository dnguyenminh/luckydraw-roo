package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines a filter operation to be applied when fetching data.
 * Specifies field, filter type, and comparison values.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest {
    
    /**
     * Field/column name to apply the filter on
     */
    private String field;
    
    /**
     * Type of filter operation to apply
     */
    private FilterType filterType;
    
    /**
     * Minimum value for range operations
     */
    private String minValue;
    
    /**
     * Maximum value for range operations
     */
    private String maxValue;
}
