package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.repository.config.TestConfig;
import vn.com.fecredit.app.entity.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // Add this annotation
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional // Add this annotation to ensure all test methods run in a transaction
public class ParticipantEventRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ParticipantEventRepository participantEventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Participant participant1;
    private Participant participant2;
    private Event event1;
    private Event event2;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM participant_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM participants").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        // Create region and province first
        Region region = createAndSaveRegion();
        Province province = createAndSaveProvince(region);
        
        participant1 = createAndSaveParticipant("P001", "John Doe", province);
        participant2 = createAndSaveParticipant("P002", "Jane Smith", province);

        System.out.println("\n=== Creating test events with guaranteed unique codes ===");
        event1 = createAndSaveEvent("Event 1", now.plusDays(1), now.plusDays(2));
        event2 = createAndSaveEvent("Event 2", now.plusDays(3), now.plusDays(4));
        System.out.println("=== Events created successfully ===\n");

        // Store participant events directly in the database without keeping references
        createAndSaveParticipantEvent(participant1, event1, CommonStatus.ACTIVE);
        createAndSaveParticipantEvent(participant1, event2, CommonStatus.ACTIVE);
        createAndSaveParticipantEvent(participant2, event1, CommonStatus.INACTIVE);

        entityManager.flush();
        entityManager.clear();
    }

    private Region createAndSaveRegion() {
        Region region = Region.builder()
                .name("Test Region")
                .code("TEST_REG" + System.currentTimeMillis())
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        entityManager.persist(region);
        return region;
    }

    private Province createAndSaveProvince(Region region) {
        Province province = Province.builder()
                .name("Test Province")
                .code("TEST_PROV" + System.currentTimeMillis())
                .region(region)
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .participants(new HashSet<>())
                .build();
        entityManager.persist(province);
        region.getProvinces().add(province);
        return province;
    }

    private Participant createAndSaveParticipant(String code, String name, Province province) {
        Participant participant = Participant.builder()
                .code(code)
                .name(name)
                .province(province) // Added province
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .participantEvents(new HashSet<>())
                .build();
        entityManager.persist(participant);
        province.getParticipants().add(participant); // Add to province's participants
        return participant;
    }

    private static long eventCounter = 0;
    
    private Event createAndSaveEvent(String name, LocalDateTime startTime, LocalDateTime endTime) {
        String eventCode = String.format("EVENT_%d_%d", System.currentTimeMillis(), ++eventCounter);
        System.out.println("Creating event '" + name + "' with code: " + eventCode);
        
        Event event = Event.builder()
                .name(name)
                .code(eventCode)
                .startTime(startTime)
                .endTime(endTime)
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .locations(new LinkedHashSet<>()) // Initialize collections
                .participantEvents(new HashSet<>())
                .build();
        entityManager.persist(event);
        return event;
    }

    private ParticipantEvent createAndSaveParticipantEvent(
            Participant participant, Event event, CommonStatus status) {
        // Need to create an event location for the participant event
        Region region = createAndSaveRegion();
        EventLocation eventLocation = EventLocation.builder()
                .name("Test Location")
                .code("TEST_LOC" + System.currentTimeMillis())
                .region(region)
                .event(event)
                .maxSpin(100)
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .participantEvents(new HashSet<>())
                .rewards(new HashSet<>())
                .goldenHours(new HashSet<>())
                .build();
        entityManager.persist(eventLocation);
        event.getLocations().add(eventLocation);
        region.getEventLocations().add(eventLocation);
        
        ParticipantEvent participantEvent = ParticipantEvent.builder()
                .participant(participant)
                .event(event)
                .eventLocation(eventLocation) // Add event location
                .spinsRemaining(5)
                .status(status)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .spinHistories(new ArrayList<>())
                .build();

        entityManager.persist(participantEvent);
        participant.getParticipantEvents().add(participantEvent);
        event.getParticipantEvents().add(participantEvent);
        eventLocation.getParticipantEvents().add(participantEvent);
        return participantEvent;
    }

    @Test
    void findByEventId_ShouldReturnParticipantEvents() {
        List<ParticipantEvent> participants = participantEventRepository.findByEventId(event1.getId());

        assertThat(participants)
                .hasSize(2)
                .extracting(pe -> pe.getParticipant().getCode())
                .containsExactlyInAnyOrder("P001", "P002");
    }

    @Test
    void findByParticipantAndEvent_ShouldReturnParticipantEvent() {
        Optional<ParticipantEvent> result = participantEventRepository.findByParticipantAndEvent(
                participant1, event1);

        assertThat(result).isPresent();

    }

    @Test
    void countByEvent_ShouldReturnCorrectCount() {
        long count1 = participantEventRepository.countByEvent(event1);
        assertThat(count1).isEqualTo(2);

        long count2 = participantEventRepository.countByEvent(event2);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    void countByEventAndStatus_ShouldReturnCorrectCount() {
        long activeCount = participantEventRepository.countByEventAndStatus(
                event1, CommonStatus.ACTIVE);
        assertThat(activeCount).isEqualTo(1);

        long inactiveCount = participantEventRepository.countByEventAndStatus(
                event1, CommonStatus.INACTIVE);
        assertThat(inactiveCount).isEqualTo(1);
    }
}
