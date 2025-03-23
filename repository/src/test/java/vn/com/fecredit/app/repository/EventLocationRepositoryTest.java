package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.repository.config.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest // Add this annotation
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional // Add this annotation to ensure all test methods run in a transaction
public class EventLocationRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(EventLocationRepositoryTest.class);

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private DatabaseCleanupUtil databaseCleanupUtil;

    @PersistenceContext
    private EntityManager entityManager;

    // Will be populated after persist
    private Long eventId;
    private Long region1Id;
    private Long region2Id;

    @BeforeEach
    public void setUp() {
        log.info("Setting up test data...");

        // Clear all existing data
        log.info("Cleaning up existing data...");
        databaseCleanupUtil.cleanAllTables();
        entityManager.flush();

        // Create test data directly using JPA entities
        log.info("Creating new test data...");

        // Create and persist Event
        Event event = new Event();
        event.setCode("EVENT001");
        event.setName("Test Event 1");
        event.setDescription("Test Description 1");
        event.setStartTime(LocalDateTime.now().minusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(23));
        event.setStatus(CommonStatus.ACTIVE);
        event.setVersion(0L);
        event.setCreatedAt(LocalDateTime.now());
        event.setCreatedBy("system");
        event.setUpdatedAt(LocalDateTime.now());
        event.setUpdatedBy("system");
        entityManager.persist(event);
        entityManager.flush();
        eventId = event.getId(); // Capture the generated ID
        log.info("\n=== Event Created ===");
        log.info("Event ID: {}", eventId);
        log.info("Event Code: {}", event.getCode());
        log.info("Event Status: {}", event.getStatus());

        // Create and persist Regions
        Region region1 = new Region();
        region1.setCode("REGION001");
        region1.setName("North Region");
        region1.setStatus(CommonStatus.ACTIVE);
        region1.setVersion(0L);
        region1.setCreatedAt(LocalDateTime.now());
        region1.setCreatedBy("system");
        region1.setUpdatedAt(LocalDateTime.now());
        region1.setUpdatedBy("system");
        entityManager.persist(region1);
        entityManager.flush();
        region1Id = region1.getId(); // Capture the generated ID

        Region region2 = new Region();
        region2.setCode("REGION002");
        region2.setName("South Region");
        region2.setStatus(CommonStatus.ACTIVE);
        region2.setVersion(0L);
        region2.setCreatedAt(LocalDateTime.now());
        region2.setCreatedBy("system");
        region2.setUpdatedAt(LocalDateTime.now());
        region2.setUpdatedBy("system");
        entityManager.persist(region2);
        entityManager.flush();
        region2Id = region2.getId(); // Capture the generated ID

        // Ensure changes are synchronized before counting
        entityManager.flush();
        entityManager.clear(); 
        
        // Method 1: Use JPA COUNT query
        Long count = entityManager.createQuery("SELECT COUNT(e) FROM EventLocation e", Long.class)
                .getSingleResult();
        log.info("Initial count using JPQL: {}", count);

        // Create and persist EventLocations
        EventLocation loc1 = new EventLocation();
        loc1.setEvent(event);
        loc1.setRegion(region1);
        loc1.setCode("LOC1");
        loc1.setName("Location 1");
        loc1.setMaxSpin(3);
        loc1.setStatus(CommonStatus.ACTIVE);
        loc1.setVersion(0L);
        loc1.setCreatedAt(LocalDateTime.now());
        loc1.setCreatedBy("system");
        loc1.setUpdatedAt(LocalDateTime.now());
        loc1.setUpdatedBy("system");
        entityManager.persist(loc1);
        entityManager.flush();  // Flush after persist
        entityManager.clear(); // Clear persistence context
        
        // Method 2: Use repository count method
        long repoCount = eventLocationRepository.count();
        log.info("Count after first insert using repository: {}", repoCount);

        EventLocation loc2 = new EventLocation();
        loc2.setEvent(event);
        loc2.setRegion(region1);
        loc2.setCode("LOC2");
        loc2.setName("Location 2");
        loc2.setMaxSpin(1); // Changed from 0 to 1 to pass validation
        loc2.setStatus(CommonStatus.INACTIVE);
        loc2.setVersion(0L);
        loc2.setCreatedAt(LocalDateTime.now());
        loc2.setCreatedBy("system");
        loc2.setUpdatedAt(LocalDateTime.now());
        loc2.setUpdatedBy("system");
        entityManager.persist(loc2);
        entityManager.flush();  // Flush after persist
        entityManager.clear(); // Clear persistence context
        
        // Method 3: Use repository findAll().size()
        int listCount = eventLocationRepository.findAll().size();
        log.info("Count after second insert using list size: {}", listCount);
        
        EventLocation loc3 = new EventLocation();
        loc3.setEvent(event);
        loc3.setRegion(region1);
        loc3.setCode("LOC3");
        loc3.setName("Location 3");
        loc3.setMaxSpin(1); // Changed from 0 to 1 to pass validation
        loc3.setStatus(CommonStatus.ACTIVE);
        loc3.setVersion(0L);
        loc3.setCreatedAt(LocalDateTime.now());
        loc3.setCreatedBy("system");
        loc3.setUpdatedAt(LocalDateTime.now());
        loc3.setUpdatedBy("system");
        entityManager.persist(loc3);
        entityManager.flush();  // Flush after persist
        entityManager.clear(); // Clear persistence context
        
        EventLocation loc4 = new EventLocation();
        loc4.setEvent(event);
        loc4.setRegion(region2);
        loc4.setCode("LOC4");
        loc4.setName("Location 4");
        loc4.setMaxSpin(5);
        loc4.setStatus(CommonStatus.ACTIVE);
        loc4.setVersion(0L);
        loc4.setCreatedAt(LocalDateTime.now());
        loc4.setCreatedBy("system");
        loc4.setUpdatedAt(LocalDateTime.now());
        loc4.setUpdatedBy("system");
        entityManager.persist(loc4);
        entityManager.flush();  // Flush after persist
        entityManager.clear(); // Clear persistence context

        // Final verification using all methods
        entityManager.flush();
        entityManager.clear();
        
        Long jpqlCount = entityManager.createQuery("SELECT COUNT(e) FROM EventLocation e", Long.class)
                .getSingleResult();
        long finalRepoCount = eventLocationRepository.count();
        int finalListCount = eventLocationRepository.findAll().size();
        
        log.info("Final counts - JPQL: {}, Repository: {}, List: {}", 
                 jpqlCount, finalRepoCount, finalListCount);
                
        // Add assertion to verify all counts match
        assertThat(jpqlCount).isEqualTo(finalRepoCount)
                            .isEqualTo(finalListCount)
                            .isEqualTo(4L);

        log.info("Test data setup complete");
    }

    /**
     * Diagnostic test to directly verify database access.
     * This will help identify if the issue is with the repository method or the
     * test data.
     */
    @Test
    public void directJpqlQueryShouldReturnEventLocations() {
        log.info("Beginning transaction test...");

        // Flush and clear to ensure fresh state
        entityManager.flush();
        entityManager.clear();

        // First verify the event exists using JPA
        Event event = entityManager.find(Event.class, eventId);
        assertThat(event).isNotNull();
        log.info("Found event with JPA: id={}, code={}", event.getId(), event.getCode());

        // Then verify event locations using JPQL
        List<EventLocation> jpqlLocations = entityManager.createQuery(
                "SELECT el FROM EventLocation el WHERE el.event.id = :eventId",
                EventLocation.class)
                .setParameter("eventId", eventId)
                .getResultList();

        log.info("Found {} locations with JPQL", jpqlLocations.size());
        jpqlLocations.forEach(loc -> log.info("Location: id={}, code={}, event={}",
                loc.getId(), loc.getCode(), loc.getEvent().getId()));

        // Verify using repository
        List<EventLocation> repoLocations = eventLocationRepository.findByEventId(eventId);
        log.info("Found {} locations with repository", repoLocations.size());

        assertThat(jpqlLocations).hasSize(4);
        assertThat(repoLocations).hasSize(4);
    }

    /**
     * Test to verify event location retrieval using JPQL queries
     */
    @Test
    public void directSqlQueryShouldReturnEventLocations() {
        log.info("Starting JPQL query test for eventId: {}", eventId);

        entityManager.flush();
        entityManager.clear();

        // Execute simple JPQL query to get all event locations for the test event
        List<EventLocation> locations = entityManager.createQuery(
                "SELECT el FROM EventLocation el WHERE el.event.id = :eventId", 
                EventLocation.class)
                .setParameter("eventId", eventId)
                .getResultList();

        log.info("Found {} locations for event {}", locations.size(), eventId);
        locations.forEach(el -> log.info("Location: id={}, code={}", el.getId(), el.getCode()));

        // Execute join query to verify relationships
        List<Object[]> joinResults = entityManager.createQuery(
                "SELECT el.code, el.event.code, el.region.code FROM EventLocation el " +
                "WHERE el.event.id = :eventId", Object[].class)
                .setParameter("eventId", eventId)
                .getResultList();

        log.info("\nVerifying relationships:");
        joinResults.forEach(r -> log.info("Location code={}, event={}, region={}", r[0], r[1], r[2]));

        assertThat(locations)
            .as("Event locations query results")
            .hasSize(4);

        assertThat(joinResults)
            .as("Join query results")
            .hasSize(4);
    }

    @Test
    public void findByEventId_ShouldReturnAllLocations() {
        // This test retrieves all event locations for event ID 1
        var eventLocations = eventLocationRepository.findByEventId(1L);

        log.info("Repository method returned {} locations", eventLocations.size());
        eventLocations.forEach(loc -> log.info("Location: {}, Event ID: {}", loc.getCode(), loc.getEvent().getId()));

        assertThat(eventLocations).isNotNull();
        assertThat(eventLocations).hasSize(4);

        // Verify the codes match our test data
        assertThat(eventLocations)
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

        // Get regions to test
        Region region1 = entityManager.find(Region.class, region1Id);
        Region region2 = entityManager.find(Region.class, region2Id);

        // Test each region individually to avoid iteration issues
        if (region1 != null) {
            log.info("Testing region1: {}", region1.getCode());
        }

        if (region2 != null) {
            log.info("Testing region2: {}", region2.getCode());
        }

        List<EventLocation> locations1 = eventLocationRepository.findByRegionId(region1Id);
        List<EventLocation> locations2 = eventLocationRepository.findByRegionId(region2Id);

        assertThat(locations1).hasSize(3);
        assertThat(locations2).hasSize(1);

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
