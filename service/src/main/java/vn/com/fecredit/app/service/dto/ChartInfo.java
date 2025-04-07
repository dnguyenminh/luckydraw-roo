package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Contains information about a chart.
 * Provides data and configuration for a visualization chart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartInfo implements Serializable {
    /**
     * Serial Version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The name of the chart
     */
    private String chartName;
    
    /**
     * The type of chart (e.g., "bar", "pie", "line")
     */
    private String chartType;
    
    /**
     * The data for the chart, organized by series
     */
    private Map<String, List<String>> chartData;
}
