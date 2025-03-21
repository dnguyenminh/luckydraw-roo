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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ParticipantEventRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    private Event activeEvent;
    private Participant participant;
    private EventLocation eventLocation;
    private ParticipantEvent activeParticipantEvent;
    private Province province;
    
    @BeforeEach
    void setUp() {
        // Clean up data using native queries
        entityManager.createNativeQuery("DELETE FROM participant_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM participants").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
        
        LocalDateTime now = LocalDateTime.now();

        // Create Region with all required fields
        Set<Province> provinces = new HashSet<>();
        Set<EventLocation> locations = new HashSet<>();
        
        Region region = Region.builder()
            .name("Test Region")
            .code("R1")
            .provinces(provinces)
            .eventLocations(locations)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(region);
        entityManager.flush();
        
        // Create Province with required fields
        province = Province.builder()
            .name("Test Province")
            .code("P1")
            .region(region)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        
        provinces.add(province);
        entityManager.persist(province);
        entityManager.flush();

        // Create Event with required fields
        activeEvent = Event.builder()
            .name("Test Event")
            .code("TEST")
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
        entityManager.persist(activeEvent);
        entityManager.flush();

        // Create Participant with required fields
        participant = Participant.builder()
            .name("Test Participant")
            .code("PART001")
            // .phone("0123456789")
            .province(province)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .participantEvents(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(participant);
        entityManager.flush();

        // Create EventLocation with required fields
        eventLocation = EventLocation.builder()
            .name("Test Location")
            .code("LOC1")
            .maxSpin(3)
            .region(region)
            .event(activeEvent)
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
        
        activeEvent.getLocations().add(eventLocation);
        region.getEventLocations().add(eventLocation);
        entityManager.persist(eventLocation);
        entityManager.flush();

        // Create ParticipantEvent with required fields
        activeParticipantEvent = ParticipantEvent.builder()
            .event(activeEvent)
            .eventLocation(eventLocation)
            .participant(participant)
            .spinsRemaining(3)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .spinHistories(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        
        activeEvent.getParticipantEvents().add(activeParticipantEvent);
        eventLocation.getParticipantEvents().add(activeParticipantEvent);
        participant.getParticipantEvents().add(activeParticipantEvent);
        
        entityManager.persist(activeParticipantEvent);
        entityManager.flush();
        entityManager.clear();
    }
    
    @Test
    void findByParticipantAndEventAndStatus_ShouldReturnCorrectRecord() {
        var result = entityManager.createQuery(
            "SELECT pe FROM ParticipantEvent pe " +
            "WHERE pe.participant = :participant " +
            "AND pe.event = :event " +
            "AND pe.status = :status",
            ParticipantEvent.class)
            .setParameter("participant", participant)
            .setParameter("event", activeEvent)
            .setParameter("status", CommonStatus.ACTIVE)
            .getResultList();
            
        assertThat(result)
            .hasSize(1)
            .element(0)
            .satisfies(pe -> {
                assertThat(pe.getParticipant().getCode()).isEqualTo("PART001");
                assertThat(pe.getEvent().getCode()).isEqualTo("TEST");
                assertThat(pe.getStatus()).isEqualTo(CommonStatus.ACTIVE);
            });
    }

    @Test
    void findByParticipantAndStatus_ShouldReturnAllActiveParticipations() {
        var results = entityManager.createQuery(
            "SELECT pe FROM ParticipantEvent pe " +
            "WHERE pe.participant = :participant " +
            "AND pe.status = :status",
            ParticipantEvent.class)
            .setParameter("participant", participant)
            .setParameter("status", CommonStatus.ACTIVE)
            .getResultList();
            
        assertThat(results)
            .hasSize(1)
            .element(0)
            .satisfies(pe -> {
                assertThat(pe.getParticipant().getCode()).isEqualTo("PART001");
                assertThat(pe.getStatus()).isEqualTo(CommonStatus.ACTIVE);
            });
    }

    @Test
    void findByEventAndStatus_ShouldReturnAllParticipantsForEvent() {
        var results = entityManager.createQuery(
            "SELECT pe FROM ParticipantEvent pe " +
            "WHERE pe.event = :event " +
            "AND pe.status = :status",
            ParticipantEvent.class)
            .setParameter("event", activeEvent)
            .setParameter("status", CommonStatus.ACTIVE)
            .getResultList();
            
        assertThat(results)
            .hasSize(1)
            .element(0)
            .satisfies(pe -> {
                assertThat(pe.getEvent().getCode()).isEqualTo("TEST");
                assertThat(pe.getStatus()).isEqualTo(CommonStatus.ACTIVE);
            });
    }

    @Test
    void existsByParticipantAndEvent_ShouldReturnTrue_WhenExists() {
        Long count = entityManager.createQuery(
            "SELECT COUNT(pe) FROM ParticipantEvent pe " +
            "WHERE pe.participant = :participant " +
            "AND pe.event = :event",
            Long.class)
            .setParameter("participant", participant)
            .setParameter("event", activeEvent)
            .getSingleResult();
            
        assertThat(count).isEqualTo(1L);

        // Create and persist a new participant for testing non-existence
        LocalDateTime now = LocalDateTime.now();
        Participant newParticipant = Participant.builder()
            .name("New Participant")
            .code("PART002")
            // .phone("0987654321")
            .province(province)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .participantEvents(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(newParticipant);
        entityManager.flush();
            
        count = entityManager.createQuery(
            "SELECT COUNT(pe) FROM ParticipantEvent pe " +
            "WHERE pe.participant = :participant " +
            "AND pe.event = :event",
            Long.class)
            .setParameter("participant", newParticipant)
            .setParameter("event", activeEvent)
            .getSingleResult();
            
        assertThat(count).isEqualTo(0L);
    }
}
