package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.enums.ActionType;
import vn.com.fecredit.app.repository.AuditLogRepository;
import vn.com.fecredit.app.service.AuditService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
public class AuditServiceImpl extends AbstractServiceImpl<AuditLog> implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        super(auditLogRepository);
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByStatus(CommonStatus status) {
        return auditLogRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public AuditLog logAction(ActionType actionType, String entityType, Long entityId, 
                             String username, String details) {
        LocalDateTime timestamp = LocalDateTime.now();
        
        AuditLog auditLog = AuditLog.builder()
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .username(username)
                .details(details)
                .timestamp(timestamp)
                .status(CommonStatus.ACTIVE)
                .build();
                
        // Set auditing fields
        auditLog.setCreatedBy(username);
        auditLog.setUpdatedBy(username);
        auditLog.setCreatedAt(timestamp);
        auditLog.setUpdatedAt(timestamp);
        
        return auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByActionType(ActionType actionType) {
        return auditLogRepository.findByActionType(actionType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByEntityTypeAndId(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findRecentActions(int limit) {
        // Handle zero or negative limit values
        if (limit <= 0) {
            return Collections.emptyList(); // Return empty list for non-positive limits
        }
        
        // Continue with standard implementation for positive limits
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findAll(pageRequest).getContent();
    }
    
    @Override
    @Transactional
    public List<AuditLog> saveAll(List<AuditLog> auditLogs) {
        return auditLogRepository.saveAll(auditLogs);
    }
}
