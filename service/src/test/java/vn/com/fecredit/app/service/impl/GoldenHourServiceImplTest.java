package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.repository.GoldenHourRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoldenHourServiceImplTest {

    @Mock
    private GoldenHourRepository goldenHourRepository;

    @InjectMocks
    private GoldenHourServiceImpl goldenHourService;

    private GoldenHour activeGoldenHour;
    private GoldenHour upcomingGoldenHour;
    private GoldenHour pastGoldenHour;
    private GoldenHour inactiveGoldenHour;
    private EventLocation eventLocation;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        eventLocation = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .code("LOC001")
                .status(CommonStatus.ACTIVE)
                .build();

        activeGoldenHour = GoldenHour.builder()
                .id(1L)
                .eventLocation(eventLocation)
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(1))
                .multiplier(BigDecimal.valueOf(2.0))
                .status(CommonStatus.ACTIVE)
                .build();

        upcomingGoldenHour = GoldenHour.builder()
                .id(2L)
                .eventLocation(eventLocation)
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(2))
                .multiplier(BigDecimal.valueOf(3.0))
                .status(CommonStatus.ACTIVE)
                .build();

        pastGoldenHour = GoldenHour.builder()
                .id(3L)
                .eventLocation(eventLocation)
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .multiplier(BigDecimal.valueOf(1.5))
                .status(CommonStatus.ACTIVE)
                .build();

        inactiveGoldenHour = GoldenHour.builder()
                .id(4L)
                .eventLocation(eventLocation)
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(1))
                .multiplier(BigDecimal.valueOf(2.5))
                .status(CommonStatus.INACTIVE)
                .build();
    }

    @Test
    void findByEventLocationId_ShouldReturnGoldenHours() {
        // Given
        Long locationId = 1L;
        when(goldenHourRepository.findByEventLocationId(locationId))
                .thenReturn(Arrays.asList(activeGoldenHour, upcomingGoldenHour, pastGoldenHour));

        // When
        List<GoldenHour> result = goldenHourService.findByEventLocationId(locationId);

        // Then
        assertEquals(3, result.size());
        verify(goldenHourRepository).findByEventLocationId(locationId);
    }

    @Test
    void findActiveGoldenHours_ShouldReturnActiveGoldenHours() {
        // Given
        when(goldenHourRepository.findActiveGoldenHours(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(activeGoldenHour));

        // When
        List<GoldenHour> result = goldenHourService.findActiveGoldenHours(now);

        // Then
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(2.0), result.get(0).getMultiplier());
        verify(goldenHourRepository).findActiveGoldenHours(now);
    }

    @Test
    void findActiveGoldenHoursByLocation_ShouldReturnActiveGoldenHours() {
        // Given
        Long locationId = 1L;
        when(goldenHourRepository.findActiveGoldenHoursByLocation(eq(locationId), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(activeGoldenHour));

        // When
        List<GoldenHour> result = goldenHourService.findActiveGoldenHoursByLocation(locationId, now);

        // Then
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(2.0), result.get(0).getMultiplier());
        verify(goldenHourRepository).findActiveGoldenHoursByLocation(locationId, now);
    }

    @Test
    void findUpcomingGoldenHours_ShouldReturnUpcomingGoldenHours() {
        // Given
        when(goldenHourRepository.findUpcomingGoldenHours(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(upcomingGoldenHour));

        // When
        List<GoldenHour> result = goldenHourService.findUpcomingGoldenHours(now);

        // Then
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(3.0), result.get(0).getMultiplier());
        verify(goldenHourRepository).findUpcomingGoldenHours(now);
    }

    @Test
    void findPastGoldenHours_ShouldReturnPastGoldenHours() {
        // Given
        when(goldenHourRepository.findPastGoldenHours(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(pastGoldenHour));

        // When
        List<GoldenHour> result = goldenHourService.findPastGoldenHours(now);

        // Then
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(1.5), result.get(0).getMultiplier());
        verify(goldenHourRepository).findPastGoldenHours(now);
    }

    @Test
    void isGoldenHourActive_ShouldReturnTrue_WhenGoldenHourIsActive() {
        // Given
        when(goldenHourRepository.findById(1L)).thenReturn(Optional.of(activeGoldenHour));

        // When
        boolean result = goldenHourService.isGoldenHourActive(1L, now);

        // Then
        assertTrue(result);
        verify(goldenHourRepository).findById(1L);
    }

    @Test
    void isGoldenHourActive_ShouldReturnFalse_WhenGoldenHourIsUpcoming() {
        // Given
        when(goldenHourRepository.findById(2L)).thenReturn(Optional.of(upcomingGoldenHour));

        // When
        boolean result = goldenHourService.isGoldenHourActive(2L, now);

        // Then
        assertFalse(result);
        verify(goldenHourRepository).findById(2L);
    }

    @Test
    void isGoldenHourActive_ShouldReturnFalse_WhenGoldenHourIsPast() {
        // Given
        when(goldenHourRepository.findById(3L)).thenReturn(Optional.of(pastGoldenHour));

        // When
        boolean result = goldenHourService.isGoldenHourActive(3L, now);

        // Then
        assertFalse(result);
        verify(goldenHourRepository).findById(3L);
    }

    @Test
    void isGoldenHourActive_ShouldReturnFalse_WhenGoldenHourIsInactive() {
        // Given
        when(goldenHourRepository.findById(4L)).thenReturn(Optional.of(inactiveGoldenHour));

        // When
        boolean result = goldenHourService.isGoldenHourActive(4L, now);

        // Then
        assertFalse(result);
        verify(goldenHourRepository).findById(4L);
    }

    @Test
    void isGoldenHourActive_ShouldReturnFalse_WhenGoldenHourDoesNotExist() {
        // Given
        when(goldenHourRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        boolean result = goldenHourService.isGoldenHourActive(99L, now);

        // Then
        assertFalse(result);
        verify(goldenHourRepository).findById(99L);
    }
}
