package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventLocationServiceImplTest {

    @Mock
    private EventLocationRepository eventLocationRepository;

    @InjectMocks
    private EventLocationServiceImpl eventLocationService;

    private EventLocation activeLocation;
    private EventLocation inactiveLocation;
    private Event event;
    private Region region;
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

        region = Region.builder()
                .id(1L)
                .name("Test Region")
                .code("REG001")
                .status(CommonStatus.ACTIVE)
                .build();

        activeLocation = EventLocation.builder()
                .id(1L)
                .name("Active Location")
                .code("LOC001")
                .event(event)
                .region(region)
                .maxSpin(10)
                .status(CommonStatus.ACTIVE)
                .build();

        inactiveLocation = EventLocation.builder()
                .id(2L)
                .name("Inactive Location")
                .code("LOC002")
                .event(event)
                .region(region)
                .maxSpin(5)
                .status(CommonStatus.INACTIVE)
                .build();
    }

    @Test
    void findByCode_ShouldReturnLocation_WhenLocationExists() {
        // Given
        when(eventLocationRepository.findByCode("LOC001")).thenReturn(Optional.of(activeLocation));

        // When
        Optional<EventLocation> result = eventLocationService.findByCode("LOC001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Active Location", result.get().getName());
        verify(eventLocationRepository).findByCode("LOC001");
    }

    @Test
    void findByEventId_ShouldReturnLocations() {
        // Given
        Long eventId = 1L;
        when(eventLocationRepository.findByEventId(eventId))
                .thenReturn(Arrays.asList(activeLocation, inactiveLocation));

        // When
        List<EventLocation> result = eventLocationService.findByEventId(eventId);

        // Then
        assertEquals(2, result.size());
        verify(eventLocationRepository).findByEventId(eventId);
    }

    @Test
    void findByRegionId_ShouldReturnLocations() {
        // Given
        Long regionId = 1L;
        when(eventLocationRepository.findByRegionId(regionId))
                .thenReturn(Arrays.asList(activeLocation, inactiveLocation));

        // When
        List<EventLocation> result = eventLocationService.findByRegionId(regionId);

        // Then
        assertEquals(2, result.size());
        verify(eventLocationRepository).findByRegionId(regionId);
    }

    @Test
    void hasAvailableCapacity_ShouldReturnTrue_WhenLocationHasCapacity() {
        // Given
        Long locationId = 1L;
        EventLocation spyLocation = spy(activeLocation);
        when(eventLocationRepository.findById(locationId)).thenReturn(Optional.of(spyLocation));
        
        // Use doReturn with the spy object
        doReturn(true).when(spyLocation).hasAvailableCapacity();
        
        // When
        boolean result = eventLocationService.hasAvailableCapacity(locationId);

        // Then
        assertTrue(result);
        verify(eventLocationRepository).findById(locationId);
    }

    @Test
    void hasAvailableCapacity_ShouldReturnFalse_WhenLocationAtCapacity() {
        // Given
        Long locationId = 1L;
        EventLocation spyLocation = spy(activeLocation);
        when(eventLocationRepository.findById(locationId)).thenReturn(Optional.of(spyLocation));
        
        // Use doReturn with the spy object
        doReturn(false).when(spyLocation).hasAvailableCapacity();
        
        // When
        boolean result = eventLocationService.hasAvailableCapacity(locationId);

        // Then
        assertFalse(result);
        verify(eventLocationRepository).findById(locationId);
    }

    @Test
    void hasAvailableCapacity_ShouldThrowException_WhenLocationNotFound() {
        // Given
        Long locationId = 99L;
        when(eventLocationRepository.findById(locationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> eventLocationService.hasAvailableCapacity(locationId));
        verify(eventLocationRepository).findById(locationId);
    }

    @Test
    void deactivate_ShouldDeactivateLocation() {
        // Given
        Long locationId = 1L;
        when(eventLocationRepository.findById(locationId)).thenReturn(Optional.of(activeLocation));
        when(eventLocationRepository.save(any(EventLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EventLocation result = eventLocationService.deactivate(locationId);

        // Then
        assertEquals(CommonStatus.INACTIVE, result.getStatus());
        verify(eventLocationRepository).findById(locationId);
        verify(eventLocationRepository).save(activeLocation);
    }

    @Test
    void findByStatus_ShouldReturnFilteredLocations() {
        // Given
        when(eventLocationRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Collections.singletonList(activeLocation));

        // When
        List<EventLocation> result = eventLocationService.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertEquals(1, result.size());
        assertEquals("Active Location", result.get(0).getName());
        verify(eventLocationRepository).findByStatus(CommonStatus.ACTIVE);
    }
}
