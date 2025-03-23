package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event activeEvent;
    private Event inactiveEvent;
    private Event upcomingEvent;
    private Event pastEvent;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        activeEvent = Event.builder()
                .id(1L)
                .name("Active Event")
                .code("ACTIVE001")
                .startTime(now.minusDays(1))
                .endTime(now.plusDays(1))
                .status(CommonStatus.ACTIVE)
                .build();
                
        inactiveEvent = Event.builder()
                .id(2L)
                .name("Inactive Event")
                .code("INACTIVE001")
                .startTime(now.minusDays(1))
                .endTime(now.plusDays(1))
                .status(CommonStatus.INACTIVE)
                .build();
                
        upcomingEvent = Event.builder()
                .id(3L)
                .name("Upcoming Event")
                .code("UPCOMING001")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .status(CommonStatus.ACTIVE)
                .build();
                
        pastEvent = Event.builder()
                .id(4L)
                .name("Past Event")
                .code("PAST001")
                .startTime(now.minusDays(2))
                .endTime(now.minusDays(1))
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void findByCode_ShouldReturnEvent_WhenEventExists() {
        // Given
        when(eventRepository.findByCode("ACTIVE001")).thenReturn(Optional.of(activeEvent));

        // When
        Optional<Event> result = eventService.findByCode("ACTIVE001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Active Event", result.get().getName());
        verify(eventRepository).findByCode("ACTIVE001");
    }

    @Test
    void findByCode_ShouldReturnEmpty_WhenEventDoesNotExist() {
        // Given
        when(eventRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When
        Optional<Event> result = eventService.findByCode("NONEXISTENT");

        // Then
        assertFalse(result.isPresent());
        verify(eventRepository).findByCode("NONEXISTENT");
    }

    @Test
    void findCurrentEvents_ShouldReturnCurrentEvents() {
        // Given
        when(eventRepository.findCurrentEvents(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(activeEvent));

        // When
        List<Event> result = eventService.findCurrentEvents(now);

        // Then
        assertEquals(1, result.size());
        assertEquals("Active Event", result.get(0).getName());
        verify(eventRepository).findCurrentEvents(now);
    }

    @Test
    void findUpcomingEvents_ShouldReturnUpcomingEvents() {
        // Given
        when(eventRepository.findUpcomingEvents(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(upcomingEvent));

        // When
        List<Event> result = eventService.findUpcomingEvents(now);

        // Then
        assertEquals(1, result.size());
        assertEquals("Upcoming Event", result.get(0).getName());
        verify(eventRepository).findUpcomingEvents(now);
    }

    @Test
    void findPastEvents_ShouldReturnPastEvents() {
        // Given
        when(eventRepository.findPastEvents(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(pastEvent));

        // When
        List<Event> result = eventService.findPastEvents(now);

        // Then
        assertEquals(1, result.size());
        assertEquals("Past Event", result.get(0).getName());
        verify(eventRepository).findPastEvents(now);
    }

    @Test
    void isEventActive_ShouldReturnTrue_WhenEventIsActive() {
        // Given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(activeEvent));

        // When
        boolean result = eventService.isEventActive(1L);

        // Then
        assertTrue(result);
        verify(eventRepository).findById(1L);
    }

    @Test
    void isEventActive_ShouldReturnFalse_WhenEventIsInactive() {
        // Given
        when(eventRepository.findById(2L)).thenReturn(Optional.of(inactiveEvent));

        // When
        boolean result = eventService.isEventActive(2L);

        // Then
        assertFalse(result);
        verify(eventRepository).findById(2L);
    }

    @Test
    void isEventActive_ShouldReturnFalse_WhenEventIsUpcoming() {
        // Given
        when(eventRepository.findById(3L)).thenReturn(Optional.of(upcomingEvent));

        // When
        boolean result = eventService.isEventActive(3L);

        // Then
        assertFalse(result);
        verify(eventRepository).findById(3L);
    }

    @Test
    void isEventActive_ShouldReturnFalse_WhenEventIsPast() {
        // Given
        when(eventRepository.findById(4L)).thenReturn(Optional.of(pastEvent));

        // When
        boolean result = eventService.isEventActive(4L);

        // Then
        assertFalse(result);
        verify(eventRepository).findById(4L);
    }

    @Test
    void isEventActive_ShouldReturnFalse_WhenEventDoesNotExist() {
        // Given
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        boolean result = eventService.isEventActive(99L);

        // Then
        assertFalse(result);
        verify(eventRepository).findById(99L);
    }
}
