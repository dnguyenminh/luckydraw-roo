package vn.com.fecredit.app.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.enums.ActionType;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.AuditLogRepository;
import vn.com.fecredit.app.service.AuditService;

/**
 * Implementation of the AuditService interface.
 * Provides audit logging and retrieval functionality.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public AuditLog logAction(ActionType actionType, String objectType, Long objectId, String details, String username) {
        log.debug("Logging action: {} on {} with ID {}", actionType, objectType, objectId);
        
        AuditLog auditLog = new AuditLog();
        auditLog.setObjectType(objectType);
        auditLog.setObjectId(objectId);
        auditLog.setPropertyPath("action");
        auditLog.setNewValue(actionType.toString() + (details != null ? ": " + details : ""));
        auditLog.setValueType("ActionType");
        auditLog.setCreatedBy(username);
        auditLog.setUpdatedBy(username);
        
        return save(auditLog);
    }
    
    @Override
    public List<AuditLog> findByEntityTypeAndId(String entityType, Long id) {
        log.debug("Finding audit logs for entity {} with ID {}", entityType, id);
        return auditLogRepository.findByObjectTypeAndObjectId(entityType, id);
    }
    
    @Override
    public List<AuditLog> findByUsername(String username) {
        log.debug("Finding audit logs for user {}", username);
        return auditLogRepository.findByUsername(username);
    }
    
    @Override
    public List<AuditLog> findByActionType(ActionType actionType) {
        log.debug("Finding audit logs by action type: {}", actionType);
        // Implementation depends on having a field in AuditLog for actionType
        // or parsing it from the newValue field
        return List.of(); // Placeholder - real implementation would query repository
    }
    
    @Override
    public List<AuditLog> findRecentActions(int limit) {
        log.debug("Finding {} most recent actions", limit);
        // Implementation would use repository to find most recent logs
        return auditLogRepository.findAll().stream().limit(limit).toList();
    }
    
    @Override
    public AuditLog save(AuditLog auditLog) {
        log.debug("Saving audit log: {}", auditLog);
        return auditLogRepository.save(auditLog);
    }
    
    @Override
    public List<AuditLog> saveAll(List<AuditLog> auditLogs) {
        log.debug("Saving {} audit logs", auditLogs.size());
        return auditLogRepository.saveAll(auditLogs);
    }
    
    @Override
    public Optional<AuditLog> findById(Long id) {
        log.debug("Finding audit log by ID: {}", id);
        return auditLogRepository.findById(id);
    }
    
    @Override
    public List<AuditLog> findAll() {
        log.debug("Finding all audit logs");
        return auditLogRepository.findAll();
    }
    
    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        log.debug("Finding audit logs with pagination: {}", pageable);
        return auditLogRepository.findAll(pageable);
    }

    @Override
    public List<AuditLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Finding audit logs between {} and {}", startTime, endTime);
        return auditLogRepository.findByCreatedAtBetween(startTime, endTime);
    }

    @Override
    public List<AuditLog> findByStatus(CommonStatus status) {
        log.debug("Finding audit logs with status: {}", status);
        return auditLogRepository.findByStatus(status);
    }
}
