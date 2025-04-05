package vn.com.fecredit.app.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Extension of TableRow that supports tab-based expansion with related tables.
 * Contains a list of related table names to render as tabs when the row is expanded.
 */
@Getter
@Setter
@NoArgsConstructor
public class TabTableRow extends TableRow {
    
    /**
     * List of table names to be rendered as tabs when this row is expanded
     */
    private List<String> relatedTables = new ArrayList<>();
    
    /**
     * Constructor with data
     * @param data the row data
     */
    public TabTableRow(Map<String, Object> data) {
        super(data);
    }
    
    /**
     * Add a related table to the list
     * @param tableName name of the related table
     * @return this instance for chaining
     */
    public TabTableRow addRelatedTable(String tableName) {
        if (tableName != null && !tableName.isEmpty()) {
            relatedTables.add(tableName);
        }
        return this;
    }
    
    /**
     * Check if this row has related tables
     * @return true if there are related tables
     */
    public boolean hasRelatedTables() {
        return relatedTables != null && !relatedTables.isEmpty();
    }
}
