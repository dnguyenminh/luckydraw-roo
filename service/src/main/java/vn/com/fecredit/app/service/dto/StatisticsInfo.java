package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Contains statistical information.
 * Groups charts by category and provides data for visualization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsInfo implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Map of chart categories to lists of charts
     */
    private Map<String, List<ChartInfo>> charts;
}
