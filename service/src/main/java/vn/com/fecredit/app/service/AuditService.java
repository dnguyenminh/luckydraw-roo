package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.enums.ActionType;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Service interface for audit logging and retrieval.
 * Provides methods for recording and querying system actions.
 */
public interface AuditService {
    
    /**
     * Log an action in the system
     * @param actionType the type of action
     * @param objectType the type of object
     * @param objectId the object's ID
     * @param details additional details
     * @param username the user who performed the action
     * @return the created audit log
     */
    AuditLog logAction(ActionType actionType, String objectType, Long objectId, String details, String username);
    
    /**
     * Find audit logs by entity type and ID
     * @param entityType the entity type
     * @param id the entity ID
     * @return matching audit logs
     */
    List<AuditLog> findByEntityTypeAndId(String entityType, Long id);
    
    /**
     * Find audit logs by username
     * @param username the username
     * @return matching audit logs
     */
    List<AuditLog> findByUsername(String username);
    
    /**
     * Find audit logs by action type
     * @param actionType the action type
     * @return matching audit logs
     */
    List<AuditLog> findByActionType(ActionType actionType);
    
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
    
    /**
     * Save an audit log
     * @param auditLog the audit log to save
     * @return the saved audit log
     */
    AuditLog save(AuditLog auditLog);
    
    /**
     * Find an audit log by ID
     * @param id the audit log ID
     * @return optional containing the audit log if found
     */
    Optional<AuditLog> findById(Long id);
    
    /**
     * Find all audit logs
     * @return list of all audit logs
     */
    List<AuditLog> findAll();
    
    /**
     * Find all audit logs with pagination
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AuditLog> findAll(Pageable pageable);
}
