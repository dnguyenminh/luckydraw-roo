package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single row in a data table.
 * Contains the actual data as a generic JSON object.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TableRow {
    
    /**
     * The data contained in this row, as key-value pairs
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();
}
