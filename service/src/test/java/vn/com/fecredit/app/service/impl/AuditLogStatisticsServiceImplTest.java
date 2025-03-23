package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.AuditLog;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.enums.ActionType;
import vn.com.fecredit.app.repository.AuditLogRepository;
import vn.com.fecredit.app.service.dto.ActionCountDTO;
import vn.com.fecredit.app.service.dto.UserActivityDTO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogStatisticsServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogStatisticsServiceImpl statisticsService;

    private AuditLog log1;
    private AuditLog log2;
    private AuditLog log3;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        log1 = AuditLog.builder()
                .id(1L)
                .username("user1")
                .actionType(ActionType.LOGIN)
                .timestamp(now.minusHours(5))
                .status(CommonStatus.ACTIVE)
                .build();

        log2 = AuditLog.builder()
                .id(2L)
                .username("user1")
                .actionType(ActionType.UPDATE)
                .entityType("Participant")
                .entityId(1L)
                .timestamp(now.minusHours(4))
                .status(CommonStatus.ACTIVE)
                .build();

        log3 = AuditLog.builder()
                .id(3L)
                .username("user2")
                .actionType(ActionType.CREATE)
                .entityType("Event")
                .entityId(1L)
                .timestamp(now.minusHours(3))
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void countActionsByType_ShouldReturnCountsGroupedByType() {
        // Given
        when(auditLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(log1, log2, log3));

        // When
        Map<ActionType, Long> result = statisticsService.countActionsByType(
                now.minusHours(6), now);

        // Then
        assertEquals(3, result.size());
        assertEquals(1L, result.get(ActionType.LOGIN));
        assertEquals(1L, result.get(ActionType.UPDATE));
        assertEquals(1L, result.get(ActionType.CREATE));
        verify(auditLogRepository).findByTimestampBetween(now.minusHours(6), now);
    }

    @Test
    void getActionBreakdown_ShouldReturnBreakdownDTOs() {
        // Given
        when(auditLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(log1, log2, log3));

        // When
        List<ActionCountDTO> result = statisticsService.getActionBreakdown(
                now.minusHours(6), now);

        // Then
        assertEquals(3, result.size());
        
        // Verify the DTOs contain correct action type and count
        assertTrue(result.stream()
                .anyMatch(dto -> dto.getActionType() == ActionType.LOGIN && dto.getCount() == 1));
        assertTrue(result.stream()
                .anyMatch(dto -> dto.getActionType() == ActionType.UPDATE && dto.getCount() == 1));
        assertTrue(result.stream()
                .anyMatch(dto -> dto.getActionType() == ActionType.CREATE && dto.getCount() == 1));
                
        verify(auditLogRepository).findByTimestampBetween(now.minusHours(6), now);
    }

    @Test
    void getUserActivity_ShouldReturnUserActivityDTOs() {
        // Given
        when(auditLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(log1, log2, log3));

        // When
        List<UserActivityDTO> result = statisticsService.getUserActivity(
                now.minusHours(6), now);

        // Then
        assertEquals(2, result.size());
        
        // Find user1's activity
        UserActivityDTO user1Activity = result.stream()
                .filter(dto -> dto.getUsername().equals("user1"))
                .findFirst()
                .orElseThrow();
                
        // Find user2's activity
        UserActivityDTO user2Activity = result.stream()
                .filter(dto -> dto.getUsername().equals("user2"))
                .findFirst()
                .orElseThrow();
                
        assertEquals(2, user1Activity.getActionCount());
        assertEquals(1, user2Activity.getActionCount());
        
        verify(auditLogRepository).findByTimestampBetween(now.minusHours(6), now);
    }

    @Test
    void getMostActiveUsers_ShouldReturnUsersWithActivityCounts() {
        // Given
        when(auditLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(log1, log2, log3));

        // When
        List<UserActivityDTO> result = statisticsService.getMostActiveUsers(
                now.minusHours(6), now, 10);

        // Then
        assertEquals(2, result.size());
        
        // First user should be user1 with 2 actions
        assertEquals("user1", result.get(0).getUsername());
        assertEquals(2, result.get(0).getActionCount());
        
        // Second user should be user2 with 1 action
        assertEquals("user2", result.get(1).getUsername());
        assertEquals(1, result.get(1).getActionCount());
        
        verify(auditLogRepository).findByTimestampBetween(now.minusHours(6), now);
    }

    @Test
    void getMostModifiedEntities_ShouldReturnEntitiesWithModificationCounts() {
        // Given
        when(auditLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(log1, log2, log3));

        // When
        Map<String, Long> result = statisticsService.getMostModifiedEntities(
                now.minusHours(6), now);

        // Then
        assertEquals(2, result.size());
        assertEquals(1L, result.get("Participant"));
        assertEquals(1L, result.get("Event"));
        
        verify(auditLogRepository).findByTimestampBetween(now.minusHours(6), now);
    }

    @Test
    void getActivityTimeline_ShouldReturnHourlyActivityCounts() {
        // Given
        when(auditLogRepository.findByTimestampBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(log1, log2, log3));

        // When
        Map<Integer, Long> result = statisticsService.getActivityTimeline(
                now.minusHours(6), now);

        // Then
        // Assuming logs are at different hours
        assertEquals(3, result.size());
        
        // Verify hours with activity
        int hour1 = log1.getTimestamp().getHour();
        int hour2 = log2.getTimestamp().getHour();
        int hour3 = log3.getTimestamp().getHour();
        
        // If logs are in different hours
        if (hour1 != hour2 && hour2 != hour3 && hour1 != hour3) {
            assertEquals(1L, result.get(hour1));
            assertEquals(1L, result.get(hour2));
            assertEquals(1L, result.get(hour3));
        }
        
        verify(auditLogRepository).findByTimestampBetween(now.minusHours(6), now);
    }
}
