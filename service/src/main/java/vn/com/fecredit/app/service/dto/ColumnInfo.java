package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Column metadata information for UI rendering
 */
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnInfo {
    /**
     * Field object type
     */
    private ObjectType objectType;
    /**
     * Field name
     */
    private String fieldName;

    /**
     * Display name for the column
     */
    private String displayName;

    /**
     * Field data type
     */
    private FieldType fieldType;

    /**
     * Sort type for the column
     */
    private SortType sortType;

    /**
     * Flag indicating if the field is editable
     */
    @Builder.Default
    private boolean editable = true;

    /**
     * Constructor without editable flag (defaults to true)
     */
    public ColumnInfo(String fieldName, FieldType fieldType, SortType sortType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.sortType = sortType;
        this.editable = true;
    }

    /**
     * Static factory method to create a non-editable column
     */
    public static ColumnInfo createNonEditable(String fieldName, FieldType fieldType, SortType sortType) {
        return ColumnInfo.builder()
                .fieldName(fieldName)
                .fieldType(fieldType)
                .sortType(sortType)
                .editable(false)
                .build();
    }
}
