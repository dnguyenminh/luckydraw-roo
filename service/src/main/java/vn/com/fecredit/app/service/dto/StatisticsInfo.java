package vn.com.fecredit.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains statistical information related to table data.
 * Groups charts by categories or sections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsInfo {
    
    /**
     * Charts grouped by category or section name
     */
    @Builder.Default
    private Map<String, List<ChartInfo>> charts = new HashMap<>();
}
