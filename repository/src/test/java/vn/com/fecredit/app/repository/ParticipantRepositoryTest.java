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

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ParticipantRepositoryTest {

    @Autowired
    private ParticipantRepository participantRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Region region;
    private Province province;
    private Event event;
    private EventLocation eventLocation; // Add field for event location

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createBaseEntities();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM participant_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM participants").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
    }

    private void createBaseEntities() {
        // Create Region
        region = Region.builder()
            .code("TEST_REGION")
            .name("Test Region")
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(region);

        // Create Province
        province = Province.builder()
            .code("TEST_PROV")
            .name("Test Province")
            .region(region)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(province);
        entityManager.flush();
    }

    @Test
    void findByCode_ShouldReturnEmpty_WhenNotExists() {
        var result = participantRepository.findByCode("NONEXISTENT");
        assertThat(result).isEmpty();
    }

    @Test
    void findByCode_ShouldReturnParticipant_WhenExists() {
        // Given
        createAndPersistParticipant("TEST1", "Test Participant", CommonStatus.ACTIVE);
        entityManager.flush();
        entityManager.clear();

        // When
        var result = participantRepository.findByCode("TEST1");

        // Then
        assertThat(result)
            .isPresent()
            .hasValueSatisfying(participant -> {
                assertThat(participant.getName()).isEqualTo("Test Participant");
                assertThat(participant.getStatus()).isEqualTo(CommonStatus.ACTIVE);
            });
    }

    @Test
    void findByStatus_ShouldReturnFilteredParticipants() {
        // Given
        createAndPersistParticipant("P1", "Active 1", CommonStatus.ACTIVE);
        createAndPersistParticipant("P2", "Inactive", CommonStatus.INACTIVE);
        createAndPersistParticipant("P3", "Active 2", CommonStatus.ACTIVE);
        entityManager.flush();
        entityManager.clear();

        // When
        var activeParticipants = participantRepository.findByStatus(CommonStatus.ACTIVE);
        var inactiveParticipants = participantRepository.findByStatus(CommonStatus.INACTIVE);

        // Then
        assertThat(activeParticipants)
            .hasSize(2)
            .extracting("code")
            .containsExactlyInAnyOrder("P1", "P3");

        assertThat(inactiveParticipants)
            .hasSize(1)
            .extracting("code")
            .containsExactly("P2");
    }

    @Test
    void existsByCode_ShouldReturnTrue_WhenExists() {
        // Given
        createAndPersistParticipant("TEST1", "Test Participant", CommonStatus.ACTIVE);
        entityManager.flush();

        // Then
        assertThat(participantRepository.existsByCode("TEST1")).isTrue();
        assertThat(participantRepository.existsByCode("NONEXISTENT")).isFalse();
    }

    @Test
    void findActiveParticipantsInEvent_ShouldReturnCorrectParticipants() {
        // Given
        var event = createAndPersistEvent("EVENT1", "Event 1");
        var participant = createAndPersistParticipant("P1", "Active In Event", CommonStatus.ACTIVE);
        var inactiveParticipant = createAndPersistParticipant("P2", "Inactive In Event", CommonStatus.ACTIVE);
        
        createAndPersistParticipantEvent(participant, event, CommonStatus.ACTIVE);
        createAndPersistParticipantEvent(inactiveParticipant, event, CommonStatus.INACTIVE);
        
        entityManager.flush();
        entityManager.clear();

        // When
        var participants = participantRepository.findActiveParticipantsInEvent(event.getId());

        // Then
        assertThat(participants)
            .hasSize(1)
            .extracting("code")
            .containsExactly("P1");
    }

    private Event createAndPersistEvent(String code, String name) {
        Event event = Event.builder()
            .code(code)
            .name(name)
            .startTime(now.minusHours(1))
            .endTime(now.plusHours(1))
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
        
        // Create and store default location for the event
        eventLocation = EventLocation.builder() // Store in field
            .name(name + " Location")
            .code(code + "_LOC")
            .status(CommonStatus.ACTIVE)
            .maxSpin(10)
            .event(event)
            .region(region)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        
        event.getLocations().add(eventLocation);
        entityManager.persist(eventLocation);
        
        return event;
    }

    private Participant createAndPersistParticipant(String code, String name, CommonStatus status) {
        // Generate unique valid phone number with proper format
        String formattedPhone = String.format("0987%06d", Math.abs(code.hashCode() % 1000000));
        
        Participant participant = Participant.builder()
            .code(code)
            .name(name)
            .status(status)
            .version(0L)
            .participantEvents(new HashSet<>())
            // .phone(formattedPhone)
            .province(province) // Add required province
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(participant);
        return participant;
    }

    private ParticipantEvent createAndPersistParticipantEvent(Participant participant, Event event, CommonStatus status) {
        ParticipantEvent participantEvent = ParticipantEvent.builder()
            .participant(participant)
            .event(event)
            .eventLocation(eventLocation) // Set the event location
            .status(status)
            .spinsRemaining(10) // Add default spins
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();

        entityManager.persist(participantEvent);
        participant.getParticipantEvents().add(participantEvent);
        event.getParticipantEvents().add(participantEvent);
        eventLocation.getParticipantEvents().add(participantEvent); // Add to location's events
        
        return participantEvent;
    }
}