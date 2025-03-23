package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.enums.ActionType;
import vn.com.fecredit.app.service.base.AbstractService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing audit logs.
 */
public interface AuditService extends AbstractService<AuditLog> {
    
    /**
     * Log an action performed by a user.
     *
     * @param actionType the type of action performed
     * @param entityType the type of entity affected
     * @param entityId the ID of the entity affected
     * @param username the username of the user who performed the action
     * @param details additional details about the action
     * @return the created audit log entry
     */
    AuditLog logAction(ActionType actionType, String entityType, Long entityId, 
                       String username, String details);
    
    /**
     * Find audit logs by username.
     *
     * @param username the username to search for
     * @return a list of audit logs for the specified user
     */
    List<AuditLog> findByUsername(String username);
    
    /**
     * Find audit logs by action type.
     *
     * @param actionType the action type to search for
     * @return a list of audit logs for the specified action type
     */
    List<AuditLog> findByActionType(ActionType actionType);
    
    /**
     * Find audit logs by entity type and ID.
     *
     * @param entityType the entity type to search for
     * @param entityId the entity ID to search for
     * @return a list of audit logs for the specified entity
     */
    List<AuditLog> findByEntityTypeAndId(String entityType, Long entityId);
    
    /**
     * Find audit logs within a time range.
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return a list of audit logs within the specified time range
     */
    List<AuditLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find the most recent audit logs.
     *
     * @param limit the maximum number of logs to retrieve
     * @return a list of the most recent audit logs
     */
    List<AuditLog> findRecentActions(int limit);
    
    /**
     * Find audit logs with the given status.
     *
     * @param status the status to search for
     * @return a list of audit logs with the specified status
     */
    List<AuditLog> findByStatus(CommonStatus status);
    
    /**
     * Save a collection of audit logs.
     *
     * @param auditLogs the audit logs to save
     * @return the saved audit logs
     */
    List<AuditLog> saveAll(List<AuditLog> auditLogs);
}
