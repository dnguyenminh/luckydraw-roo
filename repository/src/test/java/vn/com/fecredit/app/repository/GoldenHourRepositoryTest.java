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
                cleanDatabase();
                createTestData();
        }

        private void cleanDatabase() {
                log.info("Cleaning database for golden hour tests");
                entityManager.createNativeQuery("DELETE FROM golden_hours").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
                entityManager.flush();
                log.info("Database cleaned");
        }

        private void createTestData() {
                log.info("Creating test data for golden hour tests");

                // Create and persist region and event first
                region = createAndSaveRegion();
                event = createAndSaveEvent();
                
                // Flush to ensure IDs are generated
                entityManager.flush();
                
                // Create EventLocation using direct repository save instead of merge
                EventLocationKey key = EventLocationKey.of(event.getId(), region.getId());
                location = new EventLocation();
                location.setEvent(event);
                location.setRegion(region);
                location.setId(key);
                location.setStatus(CommonStatus.ACTIVE);
                location.setDescription("Test Event Location");
                location.setMaxSpin(100);
                location.setTodaySpin(50);
                location.setDailySpinDistributingRate(0.5);
                
                // Add required audit fields
                location.setCreatedBy("test-user");
                location.setCreatedAt(now);
                location.setUpdatedBy("test-user");
                location.setUpdatedAt(now);
                
                // Use the repository directly
                location = eventLocationRepository.save(location);
                
                // Clear the session to detach all managed entities
                entityManager.flush();
                entityManager.clear();
                
                // Use repository to reload the EventLocation by its composite key
                location = eventLocationRepository.findById(key).orElseThrow(() -> 
                    new RuntimeException("Failed to reload EventLocation"));
                
                // Create golden hours now with the detached and reloaded location
                activeGoldenHour = GoldenHour.builder()
                                .eventLocation(location)
                                .startTime(now.minusHours(1))
                                .endTime(now.plusHours(1))
                                .multiplier(BigDecimal.valueOf(2.0))
                                .status(CommonStatus.ACTIVE)
                                .version(0L)
                                .createdAt(now)
                                .updatedAt(now)
                                .createdBy("test-user")
                                .updatedBy("test-user")
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
                                .createdBy("test-user")
                                .updatedBy("test-user")
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
                                .createdBy("test-user")
                                .updatedBy("test-user")
                                .build();

                entityManager.persist(inactiveGoldenHour);

                // Final flush to ensure everything is saved
                entityManager.flush();
                log.info("Test data created");
        }

        private Region createAndSaveRegion() {
                log.info("Creating test region");
                Region region = Region.builder()
                                .name("Test Region")
                                .code("TEST-REGION")
                                .status(CommonStatus.ACTIVE)
                                .version(0L)
                                .provinces(new HashSet<>())
                                .eventLocations(new HashSet<>())
                                .createdAt(now)
                                .updatedAt(now)
                                .createdBy("test-user")
                                .updatedBy("test-user")
                                .build();
                entityManager.persist(region);
                return region;
        }

        private Event createAndSaveEvent() {
                log.info("Creating test event");
                Event event = Event.builder()
                                .name("Test Event")
                                .code("TEST-EVENT")
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
