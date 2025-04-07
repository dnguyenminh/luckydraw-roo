package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents information about a data column.
 * Contains metadata about a column in a table, including its name, type, and sort settings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfo implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The name of the field
     */
    private String fieldName;
    
    /**
     * The data type of the field
     */
    private String fieldType;
    
    /**
     * The default sort direction for this field
     */
    private SortType sortType;
}
