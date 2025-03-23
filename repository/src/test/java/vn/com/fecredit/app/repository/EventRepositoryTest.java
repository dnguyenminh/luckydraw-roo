package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest; // Add this import
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.repository.config.TestConfig;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest
@SpringBootTest // Add this annotation
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional // Add this annotation to ensure all test methods run in a transaction
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime baseTime = LocalDateTime.now()
        .withMinute(0)
        .withSecond(0)
        .withNano(0);

    private Event currentEvent;
    private Event futureEvent;
    @SuppressWarnings("unused") // Field used for test data setup
    private Event pastEvent;
    private Event inactiveEvent;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM participant_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        currentEvent = createAndSaveEvent(
            "CURRENT", "Current Event", 
            baseTime.minusHours(1), baseTime.plusHours(1), 
            CommonStatus.ACTIVE);

        futureEvent = createAndSaveEvent(
            "FUTURE", "Future Event",
            baseTime.plusHours(2), baseTime.plusHours(4),
            CommonStatus.ACTIVE);

        pastEvent = createAndSaveEvent(
            "PAST", "Past Event",
            baseTime.minusHours(4), baseTime.minusHours(2),
            CommonStatus.ACTIVE);

        inactiveEvent = createAndSaveEvent(
            "INACTIVE", "Inactive Event",
            baseTime.minusHours(1), baseTime.plusHours(1),
            CommonStatus.INACTIVE);

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
            .participantEvents(new HashSet<>())
            .createdAt(baseTime)
            .updatedAt(baseTime)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        return entityManager.merge(event);
    }

    @Test
    void findByCode_ShouldReturnEvent_WhenExists() {
        var result = eventRepository.findByCode("CURRENT");
        
        assertThat(result)
            .isPresent()
            .hasValueSatisfying(event -> {
                assertThat(event.getName()).isEqualTo("Current Event");
                assertThat(event.getStatus()).isEqualTo(CommonStatus.ACTIVE);
            });
    }

    @Test
    void findByCode_ShouldReturnEmpty_WhenNotExists() {
        var result = eventRepository.findByCode("NONEXISTENT");
        assertThat(result).isEmpty();
    }

    @Test
    void findByStartTimeBetween_ShouldReturnEventsInRange() {
        // Search range: inclusive start, exclusive end
        var searchStart = baseTime.minusMinutes(90); // Before current event
        var searchEnd = baseTime.plusHours(1);      // Start of future event
        
        List<Event> results = eventRepository.findByStartTimeBetween(searchStart, searchEnd);

        // Should only include CURRENT and INACTIVE as their startTime falls within range
        assertThat(results)
            .extracting("code")
            .containsExactlyInAnyOrder("CURRENT", "INACTIVE");

        // Verify time ranges
        for (Event event : results) {
            assertThat(event.getStartTime())
                .isAfterOrEqualTo(searchStart)
                .isBefore(searchEnd);
        }
    }

    @Test
    void findActiveEvents_ShouldReturnCurrentActiveEvents() {
        var results = eventRepository.findActiveEvents(baseTime);
        
        assertThat(results)
            .hasSize(1)
            .extracting("code")
            .containsExactly("CURRENT");
    }

    @Test
    void existsByCode_ShouldReturnTrue_WhenExists() {
        assertThat(eventRepository.existsByCode("CURRENT")).isTrue();
        assertThat(eventRepository.existsByCode("NONEXISTENT")).isFalse();
    }

    @Test
    void hasOverlappingEvents_ShouldIdentifyOverlaps() {
        // Test overlap with current event
        assertThat(eventRepository.hasOverlappingEvents(
            futureEvent.getId(),
            currentEvent.getStartTime(),
            currentEvent.getEndTime()
        )).isTrue();

        // Test non-overlapping time range
        assertThat(eventRepository.hasOverlappingEvents(
            currentEvent.getId(),
            baseTime.plusHours(5),
            baseTime.plusHours(6)
        )).isFalse();
    }

    @Test
    void hasOverlappingEvents_ShouldNotConsiderInactiveEvents() {
        // Check overlap with active event
        assertThat(eventRepository.hasOverlappingEvents(
            futureEvent.getId(),
            currentEvent.getStartTime(),
            currentEvent.getEndTime()
        )).isTrue();

        // Check overlap with inactive event only
        assertThat(eventRepository.hasOverlappingEvents(
            currentEvent.getId(),
            inactiveEvent.getStartTime(),
            inactiveEvent.getEndTime()
        )).isFalse();
    }
}
