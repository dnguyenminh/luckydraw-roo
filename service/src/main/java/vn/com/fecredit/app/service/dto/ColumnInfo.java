package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines metadata for a table column.
 * Includes name, data type, and default sort direction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfo {
    
    /**
     * Name of the field/column
     */
    private String fieldName;
    
    /**
     * Data type of the field as a string representation
     */
    private String fieldType;
    
    /**
     * Default sort direction for this column
     */
    private SortType sortType;
}
