package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.enums.ActionType;
import vn.com.fecredit.app.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLog createAuditLog;
    private AuditLog updateAuditLog;
    private AuditLog deleteAuditLog;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        createAuditLog = AuditLog.builder()
                .id(1L)
                .username("user1")
                .actionType(ActionType.CREATE)
                .entityType("Participant")
                .entityId(1L)
                .timestamp(now.minusHours(2))
                .details("Created new participant")
                .status(CommonStatus.ACTIVE)
                .build();

        updateAuditLog = AuditLog.builder()
                .id(2L)
                .username("user1")
                .actionType(ActionType.UPDATE)
                .entityType("Participant")
                .entityId(1L)
                .timestamp(now.minusHours(1))
                .details("Updated participant details")
                .status(CommonStatus.ACTIVE)
                .build();

        deleteAuditLog = AuditLog.builder()
                .id(3L)
                .username("user2")
                .actionType(ActionType.DELETE)
                .entityType("Participant")
                .entityId(2L)
                .timestamp(now)
                .details("Deleted participant")
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void logAction_ShouldCreateAndSaveAuditLog() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AuditLog result = auditService.logAction(
            ActionType.CREATE, 
            "Event", 
            5L, 
            "admin", 
            "Created new event"
        );

        // Then
        assertNotNull(result);
        assertEquals(ActionType.CREATE, result.getActionType());
        assertEquals("Event", result.getEntityType());
        assertEquals(5L, result.getEntityId());
        assertEquals("admin", result.getUsername());
        assertEquals("Created new event", result.getDetails());
        assertEquals(CommonStatus.ACTIVE, result.getStatus());
        
        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        
        AuditLog capturedLog = logCaptor.getValue();
        assertEquals("admin", capturedLog.getCreatedBy());
        assertEquals("admin", capturedLog.getUpdatedBy());
        assertNotNull(capturedLog.getCreatedAt());
        assertNotNull(capturedLog.getUpdatedAt());
    }

    @Test
    void logAction_ShouldHandleNullDetails() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AuditLog result = auditService.logAction(
            ActionType.READ, 
            "Event", 
            5L, 
            "admin", 
            null
        );

        // Then
        assertNotNull(result);
        assertNull(result.getDetails());
        assertEquals(ActionType.READ, result.getActionType());
        
        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        
        AuditLog capturedLog = logCaptor.getValue();
        assertNull(capturedLog.getDetails());
    }

    @Test
    void findByUsername_ShouldReturnUserLogs() {
        // Given
        when(auditLogRepository.findByUsername("user1"))
                .thenReturn(Arrays.asList(createAuditLog, updateAuditLog));

        // When
        List<AuditLog> result = auditService.findByUsername("user1");

        // Then
        assertEquals(2, result.size());
        verify(auditLogRepository).findByUsername("user1");
    }

    @Test
    void findByUsername_ShouldReturnEmptyList_WhenUserHasNoLogs() {
        // Given
        when(auditLogRepository.findByUsername("nonexistent")).thenReturn(Collections.emptyList());

        // When
        List<AuditLog> result = auditService.findByUsername("nonexistent");

        // Then
        assertTrue(result.isEmpty());
        verify(auditLogRepository).findByUsername("nonexistent");
    }

    @Test
    void findByActionType_ShouldReturnFilteredLogs() {
        // Given
        when(auditLogRepository.findByActionType(ActionType.CREATE))
                .thenReturn(Collections.singletonList(createAuditLog));

        // When
        List<AuditLog> result = auditService.findByActionType(ActionType.CREATE);

        // Then
        assertEquals(1, result.size());
        assertEquals(ActionType.CREATE, result.get(0).getActionType());
        verify(auditLogRepository).findByActionType(ActionType.CREATE);
    }

    @Test
    void findByActionType_ShouldReturnEmptyList_WhenNoLogsOfType() {
        // Given
        when(auditLogRepository.findByActionType(ActionType.LOGOUT)).thenReturn(Collections.emptyList());

        // When
        List<AuditLog> result = auditService.findByActionType(ActionType.LOGOUT);

        // Then
        assertTrue(result.isEmpty());
        verify(auditLogRepository).findByActionType(ActionType.LOGOUT);
    }

    @Test
    void findByEntityTypeAndId_ShouldReturnFilteredLogs() {
        // Given
        String entityType = "Participant";
        Long entityId = 1L;
        when(auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId))
                .thenReturn(Arrays.asList(createAuditLog, updateAuditLog));

        // When
        List<AuditLog> result = auditService.findByEntityTypeAndId(entityType, entityId);

        // Then
        assertEquals(2, result.size());
        verify(auditLogRepository).findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Test
    void findByEntityTypeAndId_ShouldReturnEmptyList_WhenNoMatchingLogs() {
        // Given
        String entityType = "NonExistentEntity";
        Long entityId = 999L;
        when(auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId))
                .thenReturn(Collections.emptyList());

        // When
        List<AuditLog> result = auditService.findByEntityTypeAndId(entityType, entityId);

        // Then
        assertTrue(result.isEmpty());
        verify(auditLogRepository).findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Test
    void findByTimeRange_ShouldReturnLogsInRange() {
        // Given
        LocalDateTime startTime = now.minusHours(3);
        LocalDateTime endTime = now.plusHours(1);
        when(auditLogRepository.findByTimestampBetween(startTime, endTime))
                .thenReturn(Arrays.asList(createAuditLog, updateAuditLog, deleteAuditLog));

        // When
        List<AuditLog> result = auditService.findByTimeRange(startTime, endTime);

        // Then
        assertEquals(3, result.size());
        verify(auditLogRepository).findByTimestampBetween(startTime, endTime);
    }

    @Test
    void findByTimeRange_ShouldHandleEmptyTimeRange() {
        // Given
        LocalDateTime startTime = now.plusDays(1);
        LocalDateTime endTime = now.plusDays(2);
        when(auditLogRepository.findByTimestampBetween(startTime, endTime))
                .thenReturn(Collections.emptyList());

        // When
        List<AuditLog> result = auditService.findByTimeRange(startTime, endTime);

        // Then
        assertTrue(result.isEmpty());
        verify(auditLogRepository).findByTimestampBetween(startTime, endTime);
    }

    @Test
    void findByTimeRange_ShouldHandleStartTimeAfterEndTime() {
        // Given - Start time is after end time
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = now.plusDays(1);
        when(auditLogRepository.findByTimestampBetween(startTime, endTime))
                .thenReturn(Collections.emptyList());

        // When
        List<AuditLog> result = auditService.findByTimeRange(startTime, endTime);

        // Then
        assertTrue(result.isEmpty());
        verify(auditLogRepository).findByTimestampBetween(startTime, endTime);
    }

    @Test
    void findRecentActions_ShouldReturnLimitedLogs() {
        // Given
        int limit = 2;
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        when(auditLogRepository.findAll(pageRequest))
                .thenReturn(new PageImpl<>(Arrays.asList(deleteAuditLog, updateAuditLog)));

        // When
        List<AuditLog> result = auditService.findRecentActions(limit);

        // Then
        assertEquals(2, result.size());
        assertEquals(deleteAuditLog.getId(), result.get(0).getId()); // Most recent first
        assertEquals(updateAuditLog.getId(), result.get(1).getId());
        verify(auditLogRepository).findAll(pageRequest);
    }

    @Test
    void findRecentActions_ShouldHandleZeroLimit() {
        // Given
        int limit = 0;
        // No mocking needed since service should return empty list without calling repository
        
        // When
        List<AuditLog> result = auditService.findRecentActions(limit);
        
        // Then
        assertThat(result).isEmpty();
        verify(auditLogRepository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void findRecentActions_ShouldHandleNegativeLimit() {
        // Given
        int limit = -5;
        // No mocking needed since service should return empty list without calling repository
        
        // When
        List<AuditLog> result = auditService.findRecentActions(limit);
        
        // Then
        assertThat(result).isEmpty();
        verify(auditLogRepository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void findByStatus_ShouldReturnFilteredLogs() {
        // Given
        when(auditLogRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Arrays.asList(createAuditLog, updateAuditLog, deleteAuditLog));

        // When
        List<AuditLog> result = auditService.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertEquals(3, result.size());
        verify(auditLogRepository).findByStatus(CommonStatus.ACTIVE);
    }

    @Test
    void findByStatus_ShouldHandleNullStatus() {
        // Given
        when(auditLogRepository.findByStatus(null)).thenReturn(Collections.emptyList());

        // When
        List<AuditLog> result = auditService.findByStatus(null);

        // Then
        assertTrue(result.isEmpty());
        verify(auditLogRepository).findByStatus(null);
    }
    
    @Test
    void saveBatch_ShouldSaveMultipleAuditLogs() {
        // Given
        List<AuditLog> logsToSave = Arrays.asList(createAuditLog, updateAuditLog, deleteAuditLog);
        when(auditLogRepository.saveAll(logsToSave)).thenReturn(logsToSave);
        
        // When
        List<AuditLog> result = auditService.saveAll(logsToSave);
        
        // Then
        assertEquals(3, result.size());
        verify(auditLogRepository).saveAll(logsToSave);
    }
    
    @Test
    void saveBatch_ShouldHandleEmptyList() {
        // Given
        List<AuditLog> emptyList = Collections.emptyList();
        when(auditLogRepository.saveAll(emptyList)).thenReturn(emptyList);
        
        // When
        List<AuditLog> result = auditService.saveAll(emptyList);
        
        // Then
        assertTrue(result.isEmpty());
        verify(auditLogRepository).saveAll(emptyList);
    }
}
