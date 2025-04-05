package vn.com.fecredit.app.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.repository.AuditLogRepository;
import vn.com.fecredit.app.service.AuditLogService;

/**
 * Implementation of the AuditLogService interface.
 * Provides audit logging operations and queries.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    
    @Override
    public AuditLog save(AuditLog auditLog) {
        log.debug("Saving audit log: {}", auditLog);
        return auditLogRepository.save(auditLog);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLog> findById(Long id) {
        log.debug("Finding audit log by ID: {}", id);
        return auditLogRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findAll() {
        log.debug("Finding all audit logs");
        return auditLogRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByObjectTypeAndObjectId(String objectType, Long objectId) {
        log.debug("Finding audit logs for object {} with ID {}", objectType, objectId);
        return auditLogRepository.findByObjectTypeAndObjectId(objectType, objectId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByObjectTypeAndObjectIdAndPropertyPath(
            String objectType, Long objectId, String propertyPath) {
        log.debug("Finding audit logs for object {} with ID {} and property {}", 
                  objectType, objectId, propertyPath);
        return auditLogRepository.findByObjectTypeAndObjectIdAndPropertyPath(
                objectType, objectId, propertyPath);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByUpdateTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Finding audit logs between {} and {}", startTime, endTime);
        return auditLogRepository.findByUpdateTimeBetween(startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByUsername(String username) {
        log.debug("Finding audit logs for user {}", username);
        return auditLogRepository.findByUsername(username);
    }
    
    @Override
    public AuditLog createAuditEntry(
            String objectType, Long objectId, String oldValue, String newValue, 
            String valueType, String propertyPath, String username) {
        
        log.debug("Creating audit entry for {} {}, property: {}", objectType, objectId, propertyPath);
        
        // Use the factory method from AuditLog entity
        AuditLog auditLog = AuditLog.createAuditEntry(
                objectType, objectId, oldValue, newValue, valueType, propertyPath, username);
        
        return save(auditLog);
    }
}
