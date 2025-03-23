package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.enums.ActionType;
import vn.com.fecredit.app.service.dto.ActionCountDTO;
import vn.com.fecredit.app.service.dto.UserActivityDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for analyzing audit logs and generating statistics.
 */
public interface AuditLogStatisticsService {
    
    /**
     * Count actions by type within a given time range.
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return a map of action types to their counts
     */
    Map<ActionType, Long> countActionsByType(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get a breakdown of actions with counts within a time range.
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return a list of DTOs containing action types and their counts
     */
    List<ActionCountDTO> getActionBreakdown(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get user activity statistics within a time range.
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return a list of DTOs containing username and action counts
     */
    List<UserActivityDTO> getUserActivity(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get the most active users within a time range.
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @param limit the maximum number of users to return
     * @return a list of DTOs containing username and action counts, sorted by count descending
     */
    List<UserActivityDTO> getMostActiveUsers(LocalDateTime startTime, LocalDateTime endTime, int limit);
    
    /**
     * Get the most modified entities within a time range.
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return a map of entity types to their modification counts
     */
    Map<String, Long> getMostModifiedEntities(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get an hourly activity timeline within a time range.
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return a map of hours to action counts
     */
    Map<Integer, Long> getActivityTimeline(LocalDateTime startTime, LocalDateTime endTime);
}
