package vn.com.fecredit.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;
import vn.com.fecredit.app.service.impl.EventLocationServiceImpl;

class EventLocationServiceTest {

    @Mock
    private EventLocationRepository eventLocationRepository;

    @InjectMocks
    private EventLocationServiceImpl eventLocationService;

    private EventLocation eventLocation;
    private Region region;
    private Event event;
    private Reward reward1;
    private Reward reward2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        region = Region.builder()
                .id(1L)
                .name("Test Region")
                .code("REG1")
                .status(CommonStatus.ACTIVE)
                .eventLocations(new HashSet<>())
                .build();

        event = Event.builder()
                .id(1L)
                .name("Test Event")
                .code("EVENT1")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .status(CommonStatus.ACTIVE)
                .locations(new HashSet<>())
                .build();

        eventLocation = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .code("LOC1")
                .quantity(10)
                .winProbability(0.5)
                .maxSpin(100)
                .region(region)
                .event(event)
                .status(CommonStatus.ACTIVE)
                .rewards(new HashSet<>())
                .participantEvents(new HashSet<>())
                .build();

        region.getEventLocations().add(eventLocation);
        event.getLocations().add(eventLocation);

        reward1 = Reward.builder()
                .id(1L)
                .name("Reward 1")
                .code("RWD1")
                .eventLocation(eventLocation)
                .status(CommonStatus.ACTIVE)
                .build();

        reward2 = Reward.builder()
                .id(2L)
                .name("Reward 2")
                .code("RWD2")
                .eventLocation(eventLocation)
                .status(CommonStatus.ACTIVE)
                .build();

        eventLocation.getRewards().add(reward1);
        eventLocation.getRewards().add(reward2);
    }

    @Test
    void testFindById() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(eventLocation));

        Optional<EventLocation> found = eventLocationService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("LOC1", found.get().getCode());

        verify(eventLocationRepository).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        when(eventLocationRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<EventLocation> found = eventLocationService.findById(99L);

        assertFalse(found.isPresent());

        verify(eventLocationRepository).findById(99L);
    }

    @Test
    void testFindByCode() {
        when(eventLocationRepository.findByCode("LOC1")).thenReturn(Optional.of(eventLocation));

        Optional<EventLocation> found = eventLocationService.findByCode("LOC1");

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());

        verify(eventLocationRepository).findByCode("LOC1");
    }

    @Test
    void testFindByEventId() {
        when(eventLocationRepository.findByEventId(1L)).thenReturn(List.of(eventLocation));

        List<EventLocation> locations = eventLocationService.findByEventId(1L);

        assertFalse(locations.isEmpty());
        assertEquals(1, locations.size());
        assertEquals("LOC1", locations.get(0).getCode());

        verify(eventLocationRepository).findByEventId(1L);
    }

    @Test
    void testFindByEventIdAndStatus() {
        when(eventLocationRepository.findByEventIdAndStatus(1L, CommonStatus.ACTIVE))
                .thenReturn(List.of(eventLocation));

        List<EventLocation> locations = eventLocationService.findByEventIdAndStatus(1L, CommonStatus.ACTIVE);

        assertFalse(locations.isEmpty());
        assertEquals(1, locations.size());
        assertEquals("LOC1", locations.get(0).getCode());

        verify(eventLocationRepository).findByEventIdAndStatus(1L, CommonStatus.ACTIVE);
    }

    @Test
    void testFindByRegionId() {
        when(eventLocationRepository.findByRegionId(1L)).thenReturn(List.of(eventLocation));

        List<EventLocation> locations = eventLocationService.findByRegionId(1L);

        assertFalse(locations.isEmpty());
        assertEquals(1, locations.size());
        assertEquals("LOC1", locations.get(0).getCode());

        verify(eventLocationRepository).findByRegionId(1L);
    }

    @Test
    void testFindByStatus() {
        when(eventLocationRepository.findByStatus(CommonStatus.ACTIVE)).thenReturn(List.of(eventLocation));

        List<EventLocation> locations = eventLocationService.findByStatus(CommonStatus.ACTIVE);

        assertFalse(locations.isEmpty());
        assertEquals(1, locations.size());
        assertEquals("LOC1", locations.get(0).getCode());

        verify(eventLocationRepository).findByStatus(CommonStatus.ACTIVE);
    }

    @Test
    void testUpdateQuantity() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(eventLocation));
        when(eventLocationRepository.save(any(EventLocation.class))).thenReturn(eventLocation);

        EventLocation updated = eventLocationService.updateQuantity(1L, 20);

        assertEquals(20, updated.getQuantity());

        verify(eventLocationRepository).findById(1L);
        verify(eventLocationRepository).save(eventLocation);
    }

    @Test
    void testUpdateQuantityWithInvalidValue() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(eventLocation));
        when(eventLocationRepository.save(any(EventLocation.class))).thenReturn(eventLocation);

        EventLocation updated = eventLocationService.updateQuantity(1L, -5);

        // The entity will allow the negative value to be set, but validation should
        // happen elsewhere
        assertEquals(-5, updated.getQuantity());

        verify(eventLocationRepository).findById(1L);
        verify(eventLocationRepository).save(eventLocation);

        // Note: In a real application, validation should prevent negative values
        // This test demonstrates the current behavior, not necessarily the desired one
    }

    @Test
    void testUpdateQuantityNotFound() {
        when(eventLocationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            eventLocationService.updateQuantity(99L, 20);
        });

        verify(eventLocationRepository).findById(99L);
        verify(eventLocationRepository, never()).save(any(EventLocation.class));
    }

    @Test
    void testUpdateWinProbability() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(eventLocation));
        when(eventLocationRepository.save(any(EventLocation.class))).thenReturn(eventLocation);

        EventLocation updated = eventLocationService.updateWinProbability(1L, 0.75);

        assertEquals(0.75, updated.getWinProbability());

        verify(eventLocationRepository).findById(1L);
        verify(eventLocationRepository).save(eventLocation);
    }

    @Test
    void testUpdateWinProbabilityWithInvalidValue() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(eventLocation));
        when(eventLocationRepository.save(any(EventLocation.class))).thenReturn(eventLocation);

        EventLocation updated = eventLocationService.updateWinProbability(1L, 1.5);

        // The entity will allow the invalid value to be set, but validation should
        // happen elsewhere
        assertEquals(1.5, updated.getWinProbability());

        verify(eventLocationRepository).findById(1L);
        verify(eventLocationRepository).save(eventLocation);

        // Note: In a real application, validation should prevent values outside 0-1
        // This test demonstrates the current behavior, not necessarily the desired one
    }

    @Test
    void testUpdateWinProbabilityNotFound() {
        when(eventLocationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            eventLocationService.updateWinProbability(99L, 0.75);
        });

        verify(eventLocationRepository).findById(99L);
        verify(eventLocationRepository, never()).save(any(EventLocation.class));
    }

    @Test
    void testHasAvailableCapacity() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(eventLocation));

        boolean hasCapacity = eventLocationService.hasAvailableCapacity(1L);

        assertTrue(hasCapacity);

        verify(eventLocationRepository).findById(1L);
    }

    @Test
    void testDeactivate() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(eventLocation));
        when(eventLocationRepository.save(any(EventLocation.class))).thenAnswer(i -> i.getArgument(0));

        EventLocation deactivated = eventLocationService.deactivate(1L);

        assertEquals(CommonStatus.INACTIVE, deactivated.getStatus());

        // Check that rewards are also deactivated
        for (Reward reward : deactivated.getRewards()) {
            assertEquals(CommonStatus.INACTIVE, reward.getStatus());
        }

        verify(eventLocationRepository).findById(1L);
        verify(eventLocationRepository).save(eventLocation);
    }

    @Test
    void testCreate() {
        EventLocation newLocation = EventLocation.builder()
                .name("New Location")
                .code("NEWLOC")
                .quantity(15)
                .winProbability(0.6)
                .maxSpin(50)
                .region(region)
                .status(CommonStatus.ACTIVE)
                .build();

        when(eventLocationRepository.save(any(EventLocation.class))).thenAnswer(i -> {
            EventLocation saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        EventLocation created = eventLocationService.create(newLocation);

        assertNotNull(created);
        assertEquals(2L, created.getId());
        assertEquals("NEWLOC", created.getCode());
        assertEquals(15, created.getQuantity());
        assertEquals(0.6, created.getWinProbability());

        verify(eventLocationRepository).save(newLocation);
    }

    @Test
    void testUpdate() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(eventLocation));
        when(eventLocationRepository.save(any(EventLocation.class))).thenReturn(eventLocation);

        EventLocation updateData = EventLocation.builder()
                .name("Updated Location")
                .code("LOC1") // keep same code
                .quantity(30)
                .winProbability(0.8)
                .maxSpin(200)
                .build();

        EventLocation updated = eventLocationService.update(1L, updateData);

        assertEquals("Updated Location", updated.getName());
        assertEquals(30, updated.getQuantity());
        assertEquals(0.8, updated.getWinProbability());
        assertEquals(200, updated.getMaxSpin());

        verify(eventLocationRepository).findById(1L);
        verify(eventLocationRepository).save(eventLocation);
    }

    @Test
    void testFindActiveSpinLocationsForEvent() {
        when(eventLocationRepository.findActiveSpinLocations(1L)).thenReturn(List.of(eventLocation));

        List<EventLocation> locations = eventLocationService.findActiveSpinLocations(1L);

        assertFalse(locations.isEmpty());
        assertEquals(1, locations.size());
        assertEquals("LOC1", locations.get(0).getCode());

        verify(eventLocationRepository).findActiveSpinLocations(1L);
    }

    @Test
    void testExistsActiveLocationInRegion() {
        when(eventLocationRepository.existsActiveLocationInRegion(1L, 1L)).thenReturn(true);
        when(eventLocationRepository.existsActiveLocationInRegion(1L, 2L)).thenReturn(false);

        assertTrue(eventLocationService.existsActiveLocationInRegion(1L, 1L));
        assertFalse(eventLocationService.existsActiveLocationInRegion(1L, 2L));

        verify(eventLocationRepository).existsActiveLocationInRegion(1L, 1L);
        verify(eventLocationRepository).existsActiveLocationInRegion(1L, 2L);
    }

    @Test
    void testFindByEventAndStatus() {
        when(eventLocationRepository.findByEventAndStatus(event, CommonStatus.ACTIVE))
                .thenReturn(List.of(eventLocation));
        
        List<EventLocation> locations = eventLocationService.findByEventAndStatus(event, CommonStatus.ACTIVE);
        
        assertFalse(locations.isEmpty());
        assertEquals(1, locations.size());
        assertEquals("LOC1", locations.get(0).getCode());
        
        verify(eventLocationRepository).findByEventAndStatus(event, CommonStatus.ACTIVE);
    }

    @Test
    void testFindByRegion() {
        when(eventLocationRepository.findByRegion(region)).thenReturn(List.of(eventLocation));
        
        List<EventLocation> locations = eventLocationService.findByRegion(region);
        
        assertFalse(locations.isEmpty());
        assertEquals(1, locations.size());
        assertEquals("LOC1", locations.get(0).getCode());
        
        verify(eventLocationRepository).findByRegion(region);
    }

    @Test
    void testFindByEvent() {
        when(eventLocationRepository.findByEvent(event)).thenReturn(List.of(eventLocation));
        
        List<EventLocation> locations = eventLocationService.findByEvent(event);
        
        assertFalse(locations.isEmpty());
        assertEquals(1, locations.size());
        assertEquals("LOC1", locations.get(0).getCode());
        
        verify(eventLocationRepository).findByEvent(event);
    }
}
