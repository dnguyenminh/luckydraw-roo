package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents the response to a table data fetch request.
 * Extends TableInfo and adds request and statistics information.
 * This is the main response object returned from table data APIs.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class TableFetchResponse extends TableInfo {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * The original request that generated this response
     */
    private TableFetchRequest originalRequest;

    /**
     * Statistical information about the data
     */
    private StatisticsInfo statistics;

}
