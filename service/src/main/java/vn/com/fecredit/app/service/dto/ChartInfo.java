package vn.com.fecredit.app.service.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chart data and configuration for statistics display
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartInfo {
    
    /**
     * Name of the chart
     */
    private String chartName;
    
    /**
     * Type of chart (e.g., bar, pie, line)
     */
    private String chartType;
    
    /**
     * Data for the chart, organized by series name
     * Each series contains a list of data points
     */
    private Map<String, List<String>> chartData;
}
