package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.EventLocationKey;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Slf4j
class GoldenHourRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private GoldenHourRepository goldenHourRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Event event;
    private EventLocation location;
    private Region region;
    private GoldenHour activeGoldenHour;
    private GoldenHour futureGoldenHour;
    private GoldenHour inactiveGoldenHour;

    @BeforeEach
    void setUp() {
        createTestData();
    }

    private void createTestData() {
        log.info("Creating test data for golden hour tests");

        // Create and persist region and event first
        region = createAndSaveRegion();
        event = createAndSaveEvent();

        // Flush to ensure IDs are generated
        entityManager.flush();

        // First check if the EventLocation already exists
        EventLocationKey key = EventLocationKey.of(event.getId(), region.getId());

        // Try to find an existing EventLocation with this composite key
        location = eventLocationRepository.findById(key).orElse(null);

        if (location == null) {
            // Only create a new EventLocation if one doesn't exist
            location = EventLocation.builder()
                .event(event)
                .region(region)
                .id(key)
                .status(CommonStatus.ACTIVE)
                .description("Test Event Location")
                .maxSpin(100)
                .todaySpin(50)
                .dailySpinDistributingRate(0.5)
                .createdBy("test-user")
                .createdAt(now)
                .updatedBy("test-user")
                .updatedAt(now)
                .build();
            event.addLocation(location);
            region.addEventLocation(location);
            // Add required audit fields

            // Use the repository to save the new EventLocation
            location = eventLocationRepository.save(location);
        } else {
            log.info("Using existing EventLocation with key: {}", key);
            // Update the existing location if needed
            location.setStatus(CommonStatus.ACTIVE);
            location.setUpdatedAt(now);
            location = eventLocationRepository.save(location);
        }

        // Clear the session to detach all managed entities
        entityManager.flush();
        entityManager.clear();

        // Use repository to reload the EventLocation by its composite key
        location = eventLocationRepository.findById(key).orElseThrow(() ->
            new RuntimeException("Failed to reload EventLocation"));

        // Create golden hours with the detached and reloaded location
        activeGoldenHour = GoldenHour.builder()
            .eventLocation(location)
            .startTime(now.minusHours(1))
            .endTime(now.plusHours(1))
            .multiplier(BigDecimal.valueOf(2.0))
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("system")
            .updatedBy("system")
            .build();

        entityManager.persist(activeGoldenHour);

        // Create future golden hour
        futureGoldenHour = GoldenHour.builder()
            .eventLocation(location)
            .startTime(now.plusHours(2))
            .endTime(now.plusHours(4))
            .multiplier(BigDecimal.valueOf(3.0))
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("system")
            .updatedBy("system")
            .build();

        entityManager.persist(futureGoldenHour);

        // Create inactive golden hour
        inactiveGoldenHour = GoldenHour.builder()
            .eventLocation(location)
            .startTime(now.minusHours(3))
            .endTime(now.plusHours(3))
            .multiplier(BigDecimal.valueOf(1.5))
            .status(CommonStatus.INACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("system")
            .updatedBy("system")
            .build();

        entityManager.persist(inactiveGoldenHour);

        // Final flush to ensure everything is saved
        entityManager.flush();
        log.info("Test data created");
    }

    private Province createAndSaveProvince() {
        log.info("Creating test province");

        // First try to find any existing region from the database
        List<Province> existingProvinces = entityManager.createQuery(
                "SELECT p FROM Province p", Province.class)
            .setMaxResults(1)
            .getResultList();

        if (!existingProvinces.isEmpty()) {
            log.info("Using existing province from database with ID: {}", existingProvinces.get(0).getId());
            return existingProvinces.get(0);
        }

        // If no regions exist at all, create a new one with a unique code
        String uniqueCode = "TEST-PROVINCE-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Province province = Province.builder()
            .name("Test Region " + uniqueCode)
            .code(uniqueCode)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .regions(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(province);
        log.info("Created new province with code: {}", uniqueCode);
        return province;
    }

    private Region createAndSaveRegion() {
        log.info("Creating test region");

        // First try to find any existing region from the database
        List<Region> existingRegions = entityManager.createQuery(
                "SELECT r FROM Region r", Region.class)
            .setMaxResults(1)
            .getResultList();

        if (!existingRegions.isEmpty()) {
            log.info("Using existing region from database with ID: {}", existingRegions.get(0).getId());
            return existingRegions.get(0);
        }

        Province aProvince = createAndSaveProvince();

        // If no regions exist at all, create a new one with a unique code
        String uniqueCode = "TEST-REGION-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Region region = Region.builder()
            .name("Test Region " + uniqueCode)
            .code(uniqueCode)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .provinces(new HashSet<>(){{
                add(aProvince);
            }})
            .eventLocations(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        aProvince.addRegion(region);
        entityManager.persist(region);
        log.info("Created new region with code: {}", uniqueCode);
        return region;
    }

    private Event createAndSaveEvent() {
        log.info("Creating test event");

        // First try to find any existing event from the database
        List<Event> existingEvents = entityManager.createQuery(
                "SELECT e FROM Event e", Event.class)
            .setMaxResults(1)
            .getResultList();

        if (!existingEvents.isEmpty()) {
            log.info("Using existing event from database with ID: {}", existingEvents.get(0).getId());
            return existingEvents.get(0);
        }

        // If no events exist at all, create a new one with a unique code
        String uniqueCode = "TEST-EVENT-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Event event = Event.builder()
            .name("Test Event " + uniqueCode)
            .code(uniqueCode)
            .description("Test event description")
            .startTime(now.minusHours(2))
            .endTime(now.plusHours(24))
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .locations(new HashSet<>())
            .build();

        entityManager.persist(event);
        log.info("Created new event with code: {}", uniqueCode);
        return event;
    }

    @Test
    void findByEventLocationId_ShouldReturnAllGoldenHours() {
        // When
        List<GoldenHour> result = goldenHourRepository.findByEventLocationId(location.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("eventLocation")
            .extracting("id")
            .containsOnly(location.getId());
    }

    @Test
    void findActiveByEventLocationId_ShouldReturnOnlyActive() {
        // When
        List<GoldenHour> result = goldenHourRepository.findByEventLocationIdAndStatus(
            location.getId(), CommonStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("status")
            .containsOnly(CommonStatus.ACTIVE);
    }

    @Test
    void findCurrentGoldenHour_ShouldReturnCurrentlyActive() {
        // When
        GoldenHour result = goldenHourRepository.findActiveGoldenHours(
            location.getId(), now);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(activeGoldenHour.getId());
        assertThat(result.getMultiplier()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
    }

    @Test
    void findCurrentGoldenHour_ShouldReturnNullWhenNoActiveHour() {
        // When - check time outside any golden hour, use time that's definitely not in any active golden hour
        // Use now.plusHours(1).plusSeconds(1) to ensure we're just past the end of activeGoldenHour
        // but before the start of futureGoldenHour at now.plusHours(2)
        GoldenHour result = goldenHourRepository.findActiveGoldenHours(
            location.getId(), now.plusHours(1).plusMinutes(1));

        // Then
        assertThat(result).isNull();
    }

    @Test
    void findActiveGoldenHoursDuringPeriod_ShouldReturnCorrect() {
        // When - find all golden hours during a time range
        List<GoldenHour> result = goldenHourRepository.findActiveGoldenHoursInPeriod(
            location, now, now.plusHours(3));

        // Then - should include current and future golden hours
        assertThat(result).hasSize(2);
        assertThat(result).extracting("id")
            .containsExactlyInAnyOrder(
                activeGoldenHour.getId(),
                futureGoldenHour.getId());
    }

    @Test
    void countOverlappingGoldenHours_ShouldReturnOverlapCount() {
        // When - check if any hours overlap with a specific time range, use integer hours
        long count = goldenHourRepository.countOverlappingActiveHoursExcluding(
            location,
            now.plusHours(2),
            now.plusHours(3),
            futureGoldenHour.getId());

        // Then - should find no overlaps with the future golden hour
        assertThat(count).isEqualTo(0);

        // When - check with a range that overlaps active golden hour
        count = goldenHourRepository.countOverlappingActiveHoursExcluding(
            location,
            now.minusMinutes(30),
            now.plusMinutes(30),
            null); // Don't exclude any

        // Then - should find the active golden hour
        assertThat(count).isEqualTo(1);
    }

    @Test
    void saveGoldenHour_ShouldPersistNewGoldenHour() {
        // Given
        GoldenHour newGoldenHour = GoldenHour.builder()
            .eventLocation(location)
            .startTime(now.plusHours(5))
            .endTime(now.plusHours(6))
            .multiplier(BigDecimal.valueOf(4.0))
            .status(CommonStatus.ACTIVE)
            // Add required audit fields to prevent not-null constraint violations
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();

        // When
        GoldenHour saved = goldenHourRepository.save(newGoldenHour);
        entityManager.flush();
        entityManager.clear();

        // Then
        GoldenHour retrieved = goldenHourRepository.findById(saved.getId()).orElseThrow();
        // Use truncated timestamps to avoid nanosecond precision issues
        assertThat(retrieved.getStartTime().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            .isEqualTo(now.plusHours(5).truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        assertThat(retrieved.getMultiplier()).isEqualByComparingTo(BigDecimal.valueOf(4.0));
    }
}
