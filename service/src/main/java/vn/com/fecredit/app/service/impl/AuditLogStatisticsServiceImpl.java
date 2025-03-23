package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.enums.ActionType;
import vn.com.fecredit.app.repository.AuditLogRepository;
import vn.com.fecredit.app.service.AuditLogStatisticsService;
import vn.com.fecredit.app.service.dto.ActionCountDTO;
import vn.com.fecredit.app.service.dto.UserActivityDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogStatisticsServiceImpl implements AuditLogStatisticsService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public Map<ActionType, Long> countActionsByType(LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditLog> logs = auditLogRepository.findByTimestampBetween(startTime, endTime);
        
        return logs.stream()
                .filter(log -> log.getActionType() != null)
                .collect(Collectors.groupingBy(
                        AuditLog::getActionType,
                        Collectors.counting()
                ));
    }

    @Override
    public List<ActionCountDTO> getActionBreakdown(LocalDateTime startTime, LocalDateTime endTime) {
        Map<ActionType, Long> actionCounts = countActionsByType(startTime, endTime);
        
        return actionCounts.entrySet().stream()
                .map(entry -> new ActionCountDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ActionCountDTO::getCount).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<UserActivityDTO> getUserActivity(LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditLog> logs = auditLogRepository.findByTimestampBetween(startTime, endTime);
        
        Map<String, Long> userActivityMap = logs.stream()
                .filter(log -> log.getUsername() != null && !log.getUsername().isEmpty())
                .collect(Collectors.groupingBy(
                        AuditLog::getUsername,
                        Collectors.counting()
                ));
                
        return userActivityMap.entrySet().stream()
                .map(entry -> new UserActivityDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserActivityDTO> getMostActiveUsers(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        List<UserActivityDTO> userActivity = getUserActivity(startTime, endTime);
        
        return userActivity.stream()
                .sorted(Comparator.comparing(UserActivityDTO::getActionCount).reversed())
                .limit(limit > 0 ? limit : 10)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getMostModifiedEntities(LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditLog> logs = auditLogRepository.findByTimestampBetween(startTime, endTime);
        
        return logs.stream()
                .filter(log -> log.getEntityType() != null && !log.getEntityType().isEmpty())
                .collect(Collectors.groupingBy(
                        AuditLog::getEntityType,
                        Collectors.counting()
                ));
    }

    @Override
    public Map<Integer, Long> getActivityTimeline(LocalDateTime startTime, LocalDateTime endTime) {
        List<AuditLog> logs = auditLogRepository.findByTimestampBetween(startTime, endTime);
        
        return logs.stream()
                .filter(log -> log.getTimestamp() != null)
                .collect(Collectors.groupingBy(
                        log -> log.getTimestamp().getHour(),
                        Collectors.counting()
                ));
    }
}
