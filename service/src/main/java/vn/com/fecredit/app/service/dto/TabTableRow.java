package vn.com.fecredit.app.service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a table row with related tables information.
 * Extends the basic TableRow to include information about related tables.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TabTableRow extends TableRow {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * List of names of related tables
     */
    @Builder.Default
    private List<String> relatedTables = new ArrayList<>();
    
    /**
     * Constructor with data
     *
     * @param data the row data as a map
     */
    public TabTableRow(Map<String, Object> data) {
        super(data);
    }
    
    /**
     * Adds a related table to the list
     *
     * @param tableName the name of the related table to add
     */
    public void addRelatedTable(String tableName) {
        if (this.relatedTables == null) {
            this.relatedTables = new ArrayList<>();
        }
        this.relatedTables.add(tableName);
    }
}
