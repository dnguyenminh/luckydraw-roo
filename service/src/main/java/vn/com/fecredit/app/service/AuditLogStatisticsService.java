package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for analyzing audit logs and generating statistics.
 */
public interface AuditLogStatisticsService {
    
    /**
     * Get statistics on action types within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return statistics data by action type
     */
    Map<String, Long> getActionTypeStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get user activity statistics within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return statistics data by user
     */
    Map<String, Long> getUserStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get statistics on entity types within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return statistics data by entity type
     */
    Map<String, Long> getEntityTypeStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get hourly statistics within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return statistics data by hour
     */
    Map<Integer, Long> getHourlyStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Count actions by type within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return count of actions by type
     */
    Map<String, Long> countActionsByType(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get most active users within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @param limit maximum number of users to return
     * @return map of usernames to action counts
     */
    Map<String, Long> getMostActiveUsers(LocalDateTime startTime, LocalDateTime endTime, int limit);
    
    /**
     * Get activity timeline within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return timeline data
     */
    Map<LocalDateTime, Long> getActivityTimeline(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get most modified entities within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return entity modification counts
     */
    Map<String, Long> getMostModifiedEntities(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get action breakdown by type within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return action breakdown data
     */
    Map<String, Long> getActionBreakdown(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get user activity details within a time range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return user activity data by user and action type
     */
    Map<String, Map<String, Long>> getUserActivity(LocalDateTime startTime, LocalDateTime endTime);
}
