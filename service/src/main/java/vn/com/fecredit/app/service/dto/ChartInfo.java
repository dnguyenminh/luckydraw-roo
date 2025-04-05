package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains information for rendering a chart.
 * Defines chart name, type, and the data to be visualized.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartInfo {
    
    /**
     * Name of the chart
     */
    private String chartName;
    
    /**
     * Type of chart (e.g., "bar", "line", "pie")
     */
    private String chartType;
    
    /**
     * Chart data as series of values
     */
    @Builder.Default
    private Map<String, List<String>> chartData = new HashMap<>();
}
