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
     * The filter value
     */
    private Object value;
    
    /**
     * The filter operator
     */
    private FilterOperator operator;
    
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
     * Constructor for simple filter with min value and max value
     * 
     * @param field The field to filter on
     * @param filterType The type of filter to apply
     * @param minValue The minimum value for the filter
     * @param maxValue The maximum value for the filter
     */
    public FilterRequest(String field, FilterType filterType, String minValue, String maxValue) {
        this.field = field;
        this.filterType = filterType;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    /**
     * Constructor for simple filter with min value
     * 
     * @param field The field to filter on
     * @param filterType The type of filter to apply
     * @param minValue The minimum value for the filter
     */
    public FilterRequest(String field, FilterType filterType, String minValue) {
        this(field, filterType, minValue, null);
    }
    
    /**
     * Constructor for simple filter with operator and value
     * 
     * @param field The field to filter on
     * @param operator The filter operator
     * @param value The filter value
     */
    public FilterRequest(String field, FilterOperator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
    
    /**
     * Maps FilterType to FilterOperator
     * 
     * @return the corresponding FilterOperator
     */
    public FilterOperator getOperator() {
        if (operator != null) {
            return operator;
        }
        
        // Map from FilterType to FilterOperator if operator is not directly specified
        if (filterType != null) {
            switch (filterType) {
                case EQUALS:
                    return FilterOperator.EQ;
                case NOT_EQUALS:
                    return FilterOperator.NE;
                case LESS_THAN:
                    return FilterOperator.LT;
                case LESS_THAN_OR_EQUALS:
                    return FilterOperator.LE;
                case GREATER_THAN:
                    return FilterOperator.GT;
                case GREATER_THAN_OR_EQUALS:
                    return FilterOperator.GE;
                case CONTAINS:
                    return FilterOperator.CONTAINS;
                case STARTS_WITH:
                    return FilterOperator.STARTS_WITH;
                case ENDS_WITH:
                    return FilterOperator.ENDS_WITH;
                case IN:
                    return FilterOperator.IN;
                case NOT_IN:
                    return FilterOperator.NOT_IN;
                case IS_NULL:
                    return FilterOperator.NULL;
                case IS_NOT_NULL:
                    return FilterOperator.NOT_NULL;
                case BETWEEN:
                    // BETWEEN doesn't map cleanly to a single operator
                    return null;
                default:
                    return FilterOperator.EQ;
            }
        }
        
        return FilterOperator.EQ; // Default
    }
    
    /**
     * Gets the filter value
     * 
     * @return the value to filter by
     */
    public Object getValue() {
        if (value != null) {
            return value;
        }
        
        if (filterType == FilterType.BETWEEN) {
            // Create a range object for BETWEEN
            if (minValue != null && maxValue != null) {
                return new Object[] { minValue, maxValue };
            } else if (minValue != null) {
                return minValue;
            } else if (maxValue != null) {
                return maxValue;
            }
        }
        
        // Use minValue as the default value for single-value filters
        return minValue;
    }
}
