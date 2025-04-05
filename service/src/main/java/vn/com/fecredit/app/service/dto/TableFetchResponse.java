package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Response DTO for table data fetch operations.
 * Contains both the table information and associated statistics data.
 * Extends TableInfo to include all table-related properties.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TableFetchResponse extends TableInfo {
    
    /**
     * The original request that generated this response
     */
    private TableFetchRequest originalRequest;
    
    /**
     * Statistical information related to the fetched data
     */
    private StatisticsInfo statistics;
}
