package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.repository.ParticipantEventRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantEventServiceImplTest {

    @Mock
    private ParticipantEventRepository participantEventRepository;

    @InjectMocks
    private ParticipantEventServiceImpl participantEventService;

    private ParticipantEvent activeParticipantEvent;
    private ParticipantEvent inactiveParticipantEvent;
    private Event event;
    private EventLocation eventLocation;
    private Participant participant;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        event = Event.builder()
                .id(1L)
                .name("Test Event")
                .code("EVENT001")
                .startTime(now.minusDays(1))
                .endTime(now.plusDays(1))
                .status(CommonStatus.ACTIVE)
                .build();

        eventLocation = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .code("LOC001")
                .event(event)
                .maxSpin(10)
                .status(CommonStatus.ACTIVE)
                .build();

        participant = Participant.builder()
                .id(1L)
                .name("Test Participant")
                .code("PART001")
                .status(CommonStatus.ACTIVE)
                .build();

        activeParticipantEvent = ParticipantEvent.builder()
                .id(1L)
                .event(event)
                .eventLocation(eventLocation)
                .participant(participant)
                .spinsRemaining(5)
                .status(CommonStatus.ACTIVE)
                .build();

        inactiveParticipantEvent = ParticipantEvent.builder()
                .id(2L)
                .event(event)
                .eventLocation(eventLocation)
                .participant(participant)
                .spinsRemaining(0)
                .status(CommonStatus.INACTIVE)
                .build();
    }

    @Test
    void findByEventId_ShouldReturnParticipantEvents() {
        // Given
        Long eventId = 1L;
        when(participantEventRepository.findByEventId(eventId))
                .thenReturn(Arrays.asList(activeParticipantEvent, inactiveParticipantEvent));

        // When
        List<ParticipantEvent> result = participantEventService.findByEventId(eventId);

        // Then
        assertEquals(2, result.size());
        verify(participantEventRepository).findByEventId(eventId);
    }

    @Test
    void findByEventLocationId_ShouldReturnParticipantEvents() {
        // Given
        Long locationId = 1L;
        when(participantEventRepository.findByEventLocationId(locationId))
                .thenReturn(Arrays.asList(activeParticipantEvent, inactiveParticipantEvent));

        // When
        List<ParticipantEvent> result = participantEventService.findByEventLocationId(locationId);

        // Then
        assertEquals(2, result.size());
        verify(participantEventRepository).findByEventLocationId(locationId);
    }

    @Test
    void findByParticipantId_ShouldReturnParticipantEvents() {
        // Given
        Long participantId = 1L;
        when(participantEventRepository.findByParticipantId(participantId))
                .thenReturn(Arrays.asList(activeParticipantEvent, inactiveParticipantEvent));

        // When
        List<ParticipantEvent> result = participantEventService.findByParticipantId(participantId);

        // Then
        assertEquals(2, result.size());
        verify(participantEventRepository).findByParticipantId(participantId);
    }

    @Test
    void findByEventIdAndStatus_ShouldReturnFilteredParticipantEvents() {
        // Given
        Long eventId = 1L;
        when(participantEventRepository.findByEventIdAndStatus(eventId, CommonStatus.ACTIVE))
                .thenReturn(Collections.singletonList(activeParticipantEvent));

        // When
        List<ParticipantEvent> result = participantEventService.findByEventIdAndStatus(eventId, CommonStatus.ACTIVE);

        // Then
        assertEquals(1, result.size());
        assertEquals(CommonStatus.ACTIVE, result.get(0).getStatus());
        verify(participantEventRepository).findByEventIdAndStatus(eventId, CommonStatus.ACTIVE);
    }

    @Test
    void findByEventIdAndParticipantId_ShouldReturnParticipantEvent() {
        // Given
        Long eventId = 1L;
        Long participantId = 1L;
        when(participantEventRepository.findByEventIdAndParticipantId(eventId, participantId))
                .thenReturn(Optional.of(activeParticipantEvent));

        // When
        Optional<ParticipantEvent> result = participantEventService.findByEventIdAndParticipantId(eventId, participantId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(participantEventRepository).findByEventIdAndParticipantId(eventId, participantId);
    }

    @Test
    void existsActiveParticipation_ShouldReturnTrue_WhenActiveParticipationExists() {
        // Given
        Long eventId = 1L;
        Long participantId = 1L;
        when(participantEventRepository.existsActiveParticipation(eventId, participantId))
                .thenReturn(true);

        // When
        boolean result = participantEventService.existsActiveParticipation(eventId, participantId);

        // Then
        assertTrue(result);
        verify(participantEventRepository).existsActiveParticipation(eventId, participantId);
    }

    @Test
    void existsActiveParticipation_ShouldReturnFalse_WhenNoActiveParticipation() {
        // Given
        Long eventId = 2L;
        Long participantId = 2L;
        when(participantEventRepository.existsActiveParticipation(eventId, participantId))
                .thenReturn(false);

        // When
        boolean result = participantEventService.existsActiveParticipation(eventId, participantId);

        // Then
        assertFalse(result);
        verify(participantEventRepository).existsActiveParticipation(eventId, participantId);
    }

    @Test
    void findByStatus_ShouldReturnFilteredParticipantEvents() {
        // Given
        when(participantEventRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Collections.singletonList(activeParticipantEvent));

        // When
        List<ParticipantEvent> result = participantEventService.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertEquals(1, result.size());
        assertEquals(CommonStatus.ACTIVE, result.get(0).getStatus());
        verify(participantEventRepository).findByStatus(CommonStatus.ACTIVE);
    }
}
