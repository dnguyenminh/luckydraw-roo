package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Column metadata information for UI rendering
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfo {
    /**
     * Field name
     */
    private String fieldName;
    
    /**
     * Field data type
     */
    private String fieldType;
    
    /**
     * Sort type for the column
     */
    private SortType sortType;
    
    /**
     * Flag indicating if the field is editable
     */
    private boolean editable = true;
    
    /**
     * Constructor without editable flag (defaults to true)
     */
    public ColumnInfo(String fieldName, String fieldType, SortType sortType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.sortType = sortType;
        this.editable = true;
    }
    
    /**
     * Static factory method to create a non-editable column
     */
    public static ColumnInfo createNonEditable(String fieldName, String fieldType, SortType sortType) {
        return new ColumnInfo(fieldName, fieldType, sortType, false);
    }
}
