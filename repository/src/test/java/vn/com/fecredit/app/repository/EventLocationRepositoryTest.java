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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventLocationRepositoryTest {

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Long eventId;
    private Long region1Id;
    private Long region2Id;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        insertTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM participant_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
    }

    private void insertTestData() {
        LocalDateTime now = LocalDateTime.now();
        
        // Insert regions using native SQL
        region1Id = 1L;
        region2Id = 2L;
        eventId = 1L;

        entityManager.createNativeQuery("""
            INSERT INTO regions (id, name, code, status, version, created_at, updated_at, created_by, updated_by)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9)
            """)
            .setParameter(1, region1Id)
            .setParameter(2, "Region 1")
            .setParameter(3, "R1")
            .setParameter(4, "ACTIVE")
            .setParameter(5, 0L)
            .setParameter(6, now)
            .setParameter(7, now)
            .setParameter(8, "test-user")
            .setParameter(9, "test-user")
            .executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO regions (id, name, code, status, version, created_at, updated_at, created_by, updated_by)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9)
            """)
            .setParameter(1, region2Id)
            .setParameter(2, "Region 2")
            .setParameter(3, "R2")
            .setParameter(4, "ACTIVE")
            .setParameter(5, 0L)
            .setParameter(6, now)
            .setParameter(7, now)
            .setParameter(8, "test-user")
            .setParameter(9, "test-user")
            .executeUpdate();

        // Insert event using native SQL
        entityManager.createNativeQuery("""
            INSERT INTO events (id, name, code, start_time, end_time, status, version, created_at, updated_at, created_by, updated_by)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)
            """)
            .setParameter(1, eventId)
            .setParameter(2, "Test Event")
            .setParameter(3, "TEST")
            .setParameter(4, now.minusHours(1))
            .setParameter(5, now.plusHours(1))
            .setParameter(6, "ACTIVE")
            .setParameter(7, 0L)
            .setParameter(8, now)
            .setParameter(9, now)
            .setParameter(10, "test-user")
            .setParameter(11, "test-user")
            .executeUpdate();

        // Insert event locations using native SQL
        Map<String, Object[]> locationData = Map.of(
            "LOC1", new Object[]{1L, "Active Location", 3, "ACTIVE", region1Id},
            "LOC2", new Object[]{2L, "Inactive Location", 3, "INACTIVE", region1Id},
            "LOC3", new Object[]{3L, "No Spins Location", 0, "ACTIVE", region1Id},
            "LOC4", new Object[]{4L, "Other Region Location", 3, "ACTIVE", region2Id}
        );

        locationData.forEach((code, data) -> {
            entityManager.createNativeQuery("""
                INSERT INTO event_locations (id, name, code, max_spin, status, version, event_id, region_id, created_at, updated_at, created_by, updated_by)
                VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12)
                """)
                .setParameter(1, data[0])
                .setParameter(2, data[1])
                .setParameter(3, code)
                .setParameter(4, data[2])
                .setParameter(5, data[3])
                .setParameter(6, 0L)
                .setParameter(7, eventId)
                .setParameter(8, data[4])
                .setParameter(9, now)
                .setParameter(10, now)
                .setParameter(11, "test-user")
                .setParameter(12, "test-user")
                .executeUpdate();
        });

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByEventId_ShouldReturnAllLocations() {
        var results = eventLocationRepository.findByEventId(eventId);
        
        assertThat(results)
            .hasSize(4)
            .extracting("code")
            .containsExactlyInAnyOrder("LOC1", "LOC2", "LOC3", "LOC4");
    }

    @Test
    void findByEventIdAndStatus_ShouldReturnFilteredLocations() {
        var activeResults = eventLocationRepository.findByEventIdAndStatus(
            eventId, CommonStatus.ACTIVE);
        
        assertThat(activeResults)
            .hasSize(3)
            .extracting("code")
            .containsExactlyInAnyOrder("LOC1", "LOC3", "LOC4");

        var inactiveResults = eventLocationRepository.findByEventIdAndStatus(
            eventId, CommonStatus.INACTIVE);
        
        assertThat(inactiveResults)
            .hasSize(1)
            .extracting("code")
            .containsExactly("LOC2");
    }

    @Test
    void findActiveSpinLocations_ShouldReturnActiveLocationsWithSpins() {
        var results = eventLocationRepository.findActiveSpinLocations(eventId);
        
        assertThat(results)
            .hasSize(2)
            .extracting("code")
            .containsExactlyInAnyOrder("LOC1", "LOC4");

        // Verify eager loading
        var firstResult = results.get(0);
        assertThat(entityManager.getEntityManagerFactory()
            .getPersistenceUnitUtil()
            .isLoaded(firstResult, "event")).isTrue();
        assertThat(entityManager.getEntityManagerFactory()
            .getPersistenceUnitUtil()
            .isLoaded(firstResult, "region")).isTrue();
    }

    @Test
    void existsActiveLocationInRegion_ShouldReturnCorrectResult() {
        assertThat(eventLocationRepository.existsActiveLocationInRegion(
            eventId, region1Id)).isTrue();
        assertThat(eventLocationRepository.existsActiveLocationInRegion(
            eventId, region2Id)).isTrue();
        assertThat(eventLocationRepository.existsActiveLocationInRegion(
            eventId + 1, region1Id)).isFalse();
    }

    @Test
    void findByEvent_ShouldReturnAllLocationsForEvent() {
        Event event = entityManager.find(Event.class, eventId);
        var results = eventLocationRepository.findByEvent(event);
        
        assertThat(results)
            .hasSize(4)
            .extracting("code")
            .containsExactlyInAnyOrder("LOC1", "LOC2", "LOC3", "LOC4");
    }

    @Test
    void findByRegion_ShouldReturnAllLocationsInRegion() {
        Region region1 = entityManager.find(Region.class, region1Id);
        Region region2 = entityManager.find(Region.class, region2Id);

        var region1Results = eventLocationRepository.findByRegion(region1);
        assertThat(region1Results)
            .hasSize(3)
            .extracting("code")
            .containsExactlyInAnyOrder("LOC1", "LOC2", "LOC3");

        var region2Results = eventLocationRepository.findByRegion(region2);
        assertThat(region2Results)
            .hasSize(1)
            .extracting("code")
            .containsExactly("LOC4");
    }

    @Test
    void findByEventAndStatus_ShouldReturnFilteredLocationsWithFetchedAssociations() {
        Event event = entityManager.find(Event.class, eventId);
        var results = eventLocationRepository.findByEventAndStatus(
            event, CommonStatus.ACTIVE);
        
        assertThat(results)
            .hasSize(3)
            .extracting("code")
            .containsExactlyInAnyOrder("LOC1", "LOC3", "LOC4");

        // Verify eager loading
        var firstResult = results.get(0);
        assertThat(entityManager.getEntityManagerFactory()
            .getPersistenceUnitUtil()
            .isLoaded(firstResult, "event")).isTrue();
        assertThat(entityManager.getEntityManagerFactory()
            .getPersistenceUnitUtil()
            .isLoaded(firstResult, "region")).isTrue();
    }
}