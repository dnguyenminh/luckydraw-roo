package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a row in a data table.
 * Contains both the actual data of the row and metadata about the table.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TableRow implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The row's data as a map of field names to values
     */
    private Map<String, Object> data;
    
    /**
     * Reference to the table information
     */
    private TableInfo tableInfo;

    /**
     * Constructor with just data
     *
     * @param data the row data as a map
     */
    public TableRow(Map<String, Object> data) {
        this.data = data;
    }
}
