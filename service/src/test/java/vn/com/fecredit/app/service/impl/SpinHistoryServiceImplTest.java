package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpinHistoryServiceImplTest {

    @Mock
    private SpinHistoryRepository spinHistoryRepository;

    @InjectMocks
    private SpinHistoryServiceImpl spinHistoryService;

    private SpinHistory winSpin;
    private SpinHistory loseSpin;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        winSpin = SpinHistory.builder()
                .id(1L)
                .win(true)
                .spinTime(now)
                .multiplier(BigDecimal.valueOf(1.5))
                .status(CommonStatus.ACTIVE)
                .build();
                
        loseSpin = SpinHistory.builder()
                .id(2L)
                .win(false)
                .spinTime(now)
                .multiplier(BigDecimal.valueOf(1.0))
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void findByParticipantEventId_ShouldReturnSpinHistories() {
        // Given
        Long participantEventId = 1L;
        when(spinHistoryRepository.findByParticipantEventId(participantEventId))
                .thenReturn(Arrays.asList(winSpin, loseSpin));

        // When
        List<SpinHistory> result = spinHistoryService.findByParticipantEventId(participantEventId);

        // Then
        assertEquals(2, result.size());
        verify(spinHistoryRepository).findByParticipantEventId(participantEventId);
    }

    @Test
    void findByRewardId_ShouldReturnSpinHistories() {
        // Given
        Long rewardId = 1L;
        when(spinHistoryRepository.findByRewardId(rewardId))
                .thenReturn(Collections.singletonList(winSpin));

        // When
        List<SpinHistory> result = spinHistoryService.findByRewardId(rewardId);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isWin());
        verify(spinHistoryRepository).findByRewardId(rewardId);
    }

    @Test
    void findByGoldenHourId_ShouldReturnSpinHistories() {
        // Given
        Long goldenHourId = 1L;
        when(spinHistoryRepository.findByGoldenHourId(goldenHourId))
                .thenReturn(Arrays.asList(winSpin, loseSpin));

        // When
        List<SpinHistory> result = spinHistoryService.findByGoldenHourId(goldenHourId);

        // Then
        assertEquals(2, result.size());
        verify(spinHistoryRepository).findByGoldenHourId(goldenHourId);
    }

    @Test
    void findSpinsInTimeRange_ShouldReturnSpinsInRange() {
        // Given
        Long participantEventId = 1L;
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);
        
        when(spinHistoryRepository.findSpinsInTimeRange(participantEventId, start, end))
                .thenReturn(Arrays.asList(winSpin, loseSpin));

        // When
        List<SpinHistory> result = spinHistoryService.findSpinsInTimeRange(participantEventId, start, end);

        // Then
        assertEquals(2, result.size());
        verify(spinHistoryRepository).findSpinsInTimeRange(participantEventId, start, end);
    }

    @Test
    void calculateTotalWinnings_ShouldReturnTotalValue() {
        // Given
        Long participantEventId = 1L;
        BigDecimal totalWinnings = BigDecimal.valueOf(150);
        
        when(spinHistoryRepository.calculateTotalWinnings(participantEventId)).thenReturn(totalWinnings);

        // When
        BigDecimal result = spinHistoryService.calculateTotalWinnings(participantEventId);

        // Then
        assertEquals(totalWinnings, result);
        verify(spinHistoryRepository).calculateTotalWinnings(participantEventId);
    }

    @Test
    void countWinningSpinsAtLocation_ShouldReturnCount() {
        // Given
        Long locationId = 1L;
        LocalDateTime start = now.minusHours(1);
        LocalDateTime end = now.plusHours(1);
        long spinCount = 5;
        
        when(spinHistoryRepository.countWinningSpinsAtLocation(locationId, start, end)).thenReturn(spinCount);

        // When
        long result = spinHistoryService.countWinningSpinsAtLocation(locationId, start, end);

        // Then
        assertEquals(spinCount, result);
        verify(spinHistoryRepository).countWinningSpinsAtLocation(locationId, start, end);
    }

    @Test
    void findWinningSpinsForEvent_ShouldReturnWinningSpins() {
        // Given
        Long eventId = 1L;
        when(spinHistoryRepository.findWinningSpinsForEvent(eventId))
                .thenReturn(Collections.singletonList(winSpin));

        // When
        List<SpinHistory> result = spinHistoryService.findWinningSpinsForEvent(eventId);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isWin());
        verify(spinHistoryRepository).findWinningSpinsForEvent(eventId);
    }

    @Test
    void findByStatus_ShouldReturnFilteredSpins() {
        // Given
        when(spinHistoryRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Arrays.asList(winSpin, loseSpin));

        // When
        List<SpinHistory> result = spinHistoryService.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertEquals(2, result.size());
        verify(spinHistoryRepository).findByStatus(CommonStatus.ACTIVE);
    }
}
