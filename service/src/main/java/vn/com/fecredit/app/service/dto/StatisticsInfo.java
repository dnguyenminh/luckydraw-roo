package vn.com.fecredit.app.service.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Container for statistics information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsInfo {
    
    /**
     * Charts organized by chart category
     */
    private Map<String, List<ChartInfo>> charts;
}
