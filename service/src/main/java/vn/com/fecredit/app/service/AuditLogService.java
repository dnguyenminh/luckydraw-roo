package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import vn.com.fecredit.app.entity.AuditLog;

/**
 * Service interface for audit log operations.
 * Provides methods to create, retrieve, and query audit logs.
 */
public interface AuditLogService {
    
    /**
     * Save an audit log entry
     * @param auditLog the audit log to save
     * @return the saved audit log
     */
    AuditLog save(AuditLog auditLog);
    
    /**
     * Find audit log by ID
     * @param id the ID to search for
     * @return the matching audit log or empty if not found
     */
    Optional<AuditLog> findById(Long id);
    
    /**
     * Find all audit logs
     * @return all audit logs
     */
    List<AuditLog> findAll();
    
    /**
     * Find audit logs for a specific object
     * @param objectType the type of object
     * @param objectId the object's ID
     * @return matching audit logs
     */
    List<AuditLog> findByObjectTypeAndObjectId(String objectType, Long objectId);
    
    /**
     * Find audit logs for a specific property of an object
     * @param objectType the type of object
     * @param objectId the object's ID
     * @param propertyPath the property path that was changed
     * @return matching audit logs
     */
    List<AuditLog> findByObjectTypeAndObjectIdAndPropertyPath(
            String objectType, Long objectId, String propertyPath);
    
    /**
     * Find audit logs within a date range
     * @param startTime start of time range
     * @param endTime end of time range
     * @return matching audit logs
     */
    List<AuditLog> findByUpdateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find audit logs by user
     * @param username the username to search for
     * @return audit logs created or updated by this user
     */
    List<AuditLog> findByUsername(String username);
    
    /**
     * Create an audit log entry
     * @param objectType type of object being audited
     * @param objectId ID of the object
     * @param oldValue value before change
     * @param newValue value after change
     * @param valueType type of the value
     * @param propertyPath path to the changed property
     * @param username user who made the change
     * @return the created audit log
     */
    AuditLog createAuditEntry(
            String objectType, Long objectId, String oldValue, String newValue, 
            String valueType, String propertyPath, String username);
}
