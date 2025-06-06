package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class EventRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Event activeEvent;
    private Event inactiveEvent;
    private Event upcomingEvent;
    // private Region testRegion;

    @BeforeEach
    void setUp() {
        createTestData();
    }

    private void createTestData() {
        // Always create a new region with unique code for testing
        String uniqueCode = "TEST-REGION-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Region region1 = Region.builder()
                .name("Test Region 1 " + uniqueCode)
                .code(uniqueCode + "-1")
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        region1.setCreatedBy("test-user");
        region1.setUpdatedBy("test-user");
        region1.setCreatedAt(now);
        region1.setUpdatedAt(now);
        region1.setVersion(0L);
        region1 = regionRepository.save(region1);

        // Create a second region for testing with multiple regions
        Region region2 = Region.builder()
                .name("Test Region 2 " + uniqueCode)
                .code(uniqueCode + "-2")
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        region2.setCreatedBy("test-user");
        region2.setUpdatedBy("test-user");
        region2.setCreatedAt(now);
        region2.setUpdatedAt(now);
        region2.setVersion(0L);
        region2 = regionRepository.save(region2);

        // Create a third region for testing with multiple regions
        Region region3 = Region.builder()
                .name("Test Region 3 " + uniqueCode)
                .code(uniqueCode + "-3")
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        region3.setCreatedBy("test-user");
        region3.setUpdatedBy("test-user");
        region3.setCreatedAt(now);
        region3.setUpdatedAt(now);
        region3.setVersion(0L);
        region3 = regionRepository.save(region3);

        // Create active event with unique code
        activeEvent = Event.builder()
                .code("ACTIVE-EVENT-" + java.util.UUID.randomUUID().toString().substring(0, 8))
                .name("Active Event")
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(23))
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .locations(new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();
        activeEvent = eventRepository.save(activeEvent);

        // Create inactive event with unique code
        inactiveEvent = Event.builder()
                .code("INACTIVE-EVENT-" + java.util.UUID.randomUUID().toString().substring(0, 8))
                .name("Inactive Event")
                .startTime(now.minusHours(5))
                .endTime(now.plusHours(5))
                .status(CommonStatus.INACTIVE)
                .version(0L)
                .locations(new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();
        inactiveEvent = eventRepository.save(inactiveEvent);

        // Create upcoming event with unique code
        upcomingEvent = Event.builder()
                .code("UPCOMING-EVENT-" + java.util.UUID.randomUUID().toString().substring(0, 8))
                .name("Upcoming Event")
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(25))
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .locations(new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();
        upcomingEvent = eventRepository.save(upcomingEvent);

        // Add locations to events with different regions
        addEventLocationWithRegion(activeEvent, region1);
        addEventLocationWithRegion(inactiveEvent, region2);
        addEventLocationWithRegion(upcomingEvent, region3);

        entityManager.flush();
        entityManager.clear();
    }

    private void addEventLocationWithRegion(Event event, Region region) {
        // Create EventLocation
        LocalDateTime now = LocalDateTime.now();
        String userName = "test-user";
        EventLocation eventLocation = EventLocation.builder()
                .event(event)
                .region(region)
                .status(event.getStatus())
                .description("Test Location for " + event.getName())
                .maxSpin(100)
                .todaySpin(50)
                .dailySpinDistributingRate(0.5)
                .createdBy(userName)
                .createdAt(now)
                .updatedBy(userName)
                .updatedAt(now)
                .build();
        event.addLocation(eventLocation);
        region.addEventLocation(eventLocation);
        // Save the entity with its properly initialized key
        eventLocationRepository.save(eventLocation);
    }

    @Test
    void findByCode_ShouldReturnEventWhenExists() {
        // When
        Optional<Event> result = eventRepository.findByCode(activeEvent.getCode());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo(activeEvent.getCode());
        assertThat(result.get().getName()).isEqualTo("Active Event");
    }

    @Test
    void findByCodeAndStatus_ShouldFilterByStatus() {
        // When
        Optional<Event> result = eventRepository.findByCodeAndStatus(
                inactiveEvent.getCode(), CommonStatus.INACTIVE);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(CommonStatus.INACTIVE);

        // When searching with wrong status
        result = eventRepository.findByCodeAndStatus(
                inactiveEvent.getCode(), CommonStatus.ACTIVE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findCurrentEvents_ShouldReturnActiveEventsInProgress() {
        // When
        List<Event> result = eventRepository.findCurrentEvents(now);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("code").containsExactly(activeEvent.getCode());
    }

    @Test
    void findUpcomingEvents_ShouldReturnFutureEvents() {
        // When
        List<Event> result = eventRepository.findUpcomingEvents(now);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("code").containsExactly(upcomingEvent.getCode());
    }

    @Test
    void findAll_WithPagination_ShouldReturnPagedResults() {
        // When
        Page<Event> result = eventRepository.findAll(
                PageRequest.of(0, 2, Sort.by("startTime").ascending()));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findByNameContaining_ShouldReturnMatchingEvents() {
        // When
        // Use the complete event name so we get an exact match
        List<Event> result = eventRepository.findByNameContainingIgnoreCase("Active Event");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").contains("Active Event");
    }

    @Test
    void findAllActive_ShouldReturnOnlyActiveEvents() {
        // When
        List<Event> result = eventRepository.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("status").containsOnly(CommonStatus.ACTIVE);
    }

    @Test
    void createAndUpdateEvent_ShouldPersistChanges() {
        // Given
        Event newEvent = Event.builder()
                .code("NEW-EVENT")
                .name("New Test Event")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .status(CommonStatus.ACTIVE)
                // Add required audit fields to prevent not-null constraint violations
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();

        // When - create
        Event saved = eventRepository.save(newEvent);
        entityManager.flush();

        // Then - verify saved
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("NEW-EVENT");

        // When - update
        saved.setName("Updated Event Name");
        eventRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        // Then - verify update
        Event updated = eventRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Event Name");
    }
}
