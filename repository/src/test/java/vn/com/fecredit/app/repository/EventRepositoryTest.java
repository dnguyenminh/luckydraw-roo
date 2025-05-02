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
import vn.com.fecredit.app.entity.EventLocationKey;
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
    private Region testRegion;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST-REGION")
            .status(CommonStatus.ACTIVE)
            .provinces(new HashSet<>())
            .eventLocations(new HashSet<>())
            .build();
        region.setCreatedBy("test-user");
        region.setUpdatedBy("test-user");
        region.setCreatedAt(now);
        region.setUpdatedAt(now);
        entityManager.persist(region);

        // Create active event
        activeEvent = createAndSaveEvent(
            "ACTIVE-EVENT",
            "Active Event",
            now.minusHours(1),
            now.plusHours(23),
            CommonStatus.ACTIVE);

        // Create inactive event
        inactiveEvent = createAndSaveEvent(
            "INACTIVE-EVENT",
            "Inactive Event",
            now.minusHours(5),
            now.plusHours(5),
            CommonStatus.INACTIVE);

        // Create upcoming event
        upcomingEvent = createAndSaveEvent(
            "UPCOMING-EVENT",
            "Upcoming Event",
            now.plusHours(1),
            now.plusHours(25),
            CommonStatus.ACTIVE);

        // Add locations to events
        addEventLocation(activeEvent);
        addEventLocation(inactiveEvent);
        addEventLocation(upcomingEvent);

        entityManager.flush();
        entityManager.clear();
    }

    private Event createAndSaveEvent(String code, String name,
                                     LocalDateTime startTime, LocalDateTime endTime, CommonStatus status) {
        Event event = Event.builder()
            .code(code)
            .name(name)
            .startTime(startTime)
            .endTime(endTime)
            .status(status)
            .version(0L)
            .locations(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();

        return entityManager.merge(event);
    }

    private void addEventLocation(Event event) {
        // Create a region if needed
        if (testRegion == null) {
            testRegion = new Region();
            testRegion.setName("Test Region");
            testRegion.setCode("TEST_REGION");
            testRegion.setStatus(CommonStatus.ACTIVE);
            // Add required audit fields
            LocalDateTime now = LocalDateTime.now();
            testRegion.setCreatedBy("test-user");
            testRegion.setCreatedAt(now);
            testRegion.setUpdatedBy("test-user");
            testRegion.setUpdatedAt(now);
            testRegion = regionRepository.save(testRegion);
        }

        // Create EventLocation
        EventLocation eventLocation = new EventLocation();
        eventLocation.setEvent(event);
        eventLocation.setRegion(testRegion);

        // Important: Match location status with event status to avoid validation errors
        eventLocation.setStatus(event.getStatus());

        eventLocation.setDescription("Test Location");
        eventLocation.setMaxSpin(100);
        eventLocation.setTodaySpin(50);
        eventLocation.setDailySpinDistributingRate(0.5);

        // Add required audit fields
        LocalDateTime now = LocalDateTime.now();
        eventLocation.setCreatedBy("test-user");
        eventLocation.setCreatedAt(now);
        eventLocation.setUpdatedBy("test-user");
        eventLocation.setUpdatedAt(now);

        // IMPORTANT: Manually create and set the composite key with non-null values
        EventLocationKey key = new EventLocationKey();
        key.setEventId(event.getId());
        key.setRegionId(testRegion.getId());
        eventLocation.setId(key);

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
