package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.repository.config.TestConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GoldenHourRepositoryTest {

    @Autowired
    private GoldenHourRepository goldenHourRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Event event;
    private Region region;
    private EventLocation location;
    private GoldenHour currentGoldenHour;
    private GoldenHour futureGoldenHour;
    private GoldenHour inactiveGoldenHour;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM golden_hours").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        region = createAndSaveRegion();
        event = createAndSaveEvent();
        location = createAndSaveLocation(event, region);
        
        currentGoldenHour = createAndSaveGoldenHour(
            location, now.minusMinutes(30), now.plusMinutes(30),
            BigDecimal.valueOf(2.0).setScale(2), CommonStatus.ACTIVE);
            
        futureGoldenHour = createAndSaveGoldenHour(
            location, now.plusHours(1), now.plusHours(2),
            BigDecimal.valueOf(1.5).setScale(2), CommonStatus.ACTIVE);
            
        inactiveGoldenHour = createAndSaveGoldenHour(
            location, now.minusMinutes(30), now.plusMinutes(30),
            BigDecimal.valueOf(3.0).setScale(2), CommonStatus.INACTIVE);

        entityManager.flush();
        entityManager.clear();
    }

    private Region createAndSaveRegion() {
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST-REG")
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
        Event event = Event.builder()
            .name("Test Event")
            .code("TEST-EVENT")
            .startTime(now.minusHours(1))
            .endTime(now.plusHours(3))
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .locations(new HashSet<>())
            .participantEvents(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(event);
        return event;
    }

    private EventLocation createAndSaveLocation(Event event, Region region) {
        EventLocation location = EventLocation.builder()
            .name("Test Location")
            .code("TEST-LOC")
            .maxSpin(100)
            .event(event)
            .region(region)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .participantEvents(new HashSet<>())
            .rewards(new HashSet<>())
            .goldenHours(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        event.getLocations().add(location);
        region.getEventLocations().add(location);
        entityManager.persist(location);
        return location;
    }

    private GoldenHour createAndSaveGoldenHour(
            EventLocation location, LocalDateTime startTime, LocalDateTime endTime,
            BigDecimal multiplier, CommonStatus status) {
        GoldenHour goldenHour = GoldenHour.builder()
            .eventLocation(location)
            .startTime(startTime)
            .endTime(endTime)
            .multiplier(multiplier)
            .status(status)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        location.getGoldenHours().add(goldenHour);
        entityManager.persist(goldenHour);
        return goldenHour;
    }

    @Test
    void findByEventLocationId_ShouldReturnAllGoldenHours() {
        var results = goldenHourRepository.findByEventLocationId(location.getId());
        assertThat(results).hasSize(3);
    }

    @Test
    void findByEventLocationIdAndStatus_ShouldReturnFilteredGoldenHours() {
        var activeGoldenHours = goldenHourRepository.findByEventLocationIdAndStatus(
            location.getId(), CommonStatus.ACTIVE);
        assertThat(activeGoldenHours).hasSize(2);
        
        var inactiveGoldenHours = goldenHourRepository.findByEventLocationIdAndStatus(
            location.getId(), CommonStatus.INACTIVE);
        assertThat(inactiveGoldenHours).hasSize(1);
    }

    @Test
    void findActiveGoldenHours_ShouldReturnCurrentActiveGoldenHours() {
        var activeHours = goldenHourRepository.findActiveGoldenHours(location.getId(), now);
        
        assertThat(activeHours)
            .hasSize(1)
            .extracting(GoldenHour::getMultiplier)
            .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
            .containsExactly(BigDecimal.valueOf(2.0).setScale(2));
    }

    @Test
    void findGoldenHoursInTimeRange_ShouldReturnHoursInRange() {
        var range = goldenHourRepository.findGoldenHoursInTimeRange(
            location.getId(), now, now.plusHours(3));
            
        assertThat(range)
            .hasSize(2)
            .extracting(GoldenHour::getMultiplier)
            .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
            .containsExactlyInAnyOrder(
                BigDecimal.valueOf(2.0).setScale(2),
                BigDecimal.valueOf(1.5).setScale(2));
    }

    @Test
    void hasOverlappingGoldenHours_ShouldDetectOverlaps() {
        // Test non-overlapping period - gap between golden hours
        assertThat(goldenHourRepository.hasOverlappingGoldenHours(
            location.getId(),
            currentGoldenHour.getId(),
            now.plusMinutes(45),  // Between current and future golden hour
            now.plusMinutes(50)
        )).isFalse();

        // Test overlapping period - overlaps with future golden hour
        assertThat(goldenHourRepository.hasOverlappingGoldenHours(
            location.getId(), 
            null,  // Test with no exclusion
            now.plusMinutes(55),
            now.plusHours(1).plusMinutes(30)
        )).isTrue();
    }
}