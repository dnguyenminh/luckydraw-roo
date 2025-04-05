package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Slf4j
public class GoldenHourRepositoryTest extends AbstractRepositoryTest {

        // private static final Logger log =
        // LoggerFactory.getLogger(GoldenHourRepositoryTest.class);

        @Autowired
        private GoldenHourRepository goldenHourRepository;

        @Autowired
        private DatabaseCleanupUtil databaseCleanupUtil;

        @PersistenceContext
        private EntityManager entityManager;

        private Event event1;
        private EventLocation eventLocation1;

        @BeforeEach
        public void setUp() {
                log.info("Setting up test data for GoldenHourRepositoryTest");
                databaseCleanupUtil.cleanAllTables();
                createTestData();
        }

        private void createTestData() {
                log.info("Creating test data");

                // Create event with consistent time range for all tests
                LocalDateTime[] timeRange = getTestEventTimeRange();
                event1 = createAndSaveEvent(timeRange[0], timeRange[1]);
                entityManager.flush();
                log.info("Created event with ID: {} spanning {} to {}",
                                event1.getId(), event1.getStartTime(), event1.getEndTime());

                // Create and save EventLocation
                try {
                        eventLocation1 = createAndSaveEventLocation(event1);
                        log.info("Successfully created EventLocation with ID: {}", eventLocation1.getId());
                } catch (Exception e) {
                        log.error("Error creating EventLocation: {}", e.getMessage(), e);
                        throw e;
                }

                // Now create golden hours
                createAndSaveGoldenHour(eventLocation1);
                createAndSaveGoldenHour(eventLocation1);

                // Create an inactive golden hour for testing filtering
                GoldenHour inactiveGoldenHour = createAndSaveGoldenHour(eventLocation1);
                inactiveGoldenHour.setStatus(CommonStatus.INACTIVE);
                inactiveGoldenHour.setMultiplier(BigDecimal.valueOf(1.5));
                entityManager.merge(inactiveGoldenHour);

                entityManager.flush();
        }

        private Region createAndSaveRegion() {
                Region region = Region.builder()
                                .name("Test Region")
                                .code("REG-" + System.currentTimeMillis())
                                .status(CommonStatus.ACTIVE)
                                .version(0L)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy("system")
                                .updatedBy("system")
                                .provinces(new HashSet<>())
                                .eventLocations(new HashSet<>())
                                .build();
                entityManager.persist(region);
                entityManager.flush();
                return region;
        }

        private LocalDateTime[] getTestEventTimeRange() {
                // Ensure consistent time precision by truncating nanoseconds
                LocalDateTime now = LocalDateTime.now().withNano(0);
                return new LocalDateTime[] {
                                now.minusHours(8),
                                now.plusHours(8)
                };
        }

        private Event createAndSaveEvent(LocalDateTime startTime, LocalDateTime endTime) {
                log.info("\n=== Creating Event ===");
                log.info("Start Time: {}", startTime);
                log.info("End Time: {}", endTime);

                Event event = Event.builder()
                                .name("Test Event")
                                .code("EVENT-" + System.currentTimeMillis())
                                .description("Test event description")
                                .startTime(startTime)
                                .endTime(endTime)
                                .status(CommonStatus.ACTIVE)
                                .version(0L)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy("system")
                                .updatedBy("system")
                                .locations(new LinkedHashSet<>())
                                .participantEvents(new HashSet<>())
                                .build();
                entityManager.persist(event);
                entityManager.flush();
                return event;
        }

        private EventLocation createAndSaveEventLocation(Event event) {
                log.info("Creating and saving EventLocation for event with ID: {}", event.getId());

                try {
                        // Ensure the event is managed and committed first
                        event = entityManager.merge(event);
                        entityManager.flush();

                        // Create and save the region first
                        Region region = createAndSaveRegion();
                        entityManager.flush();
                        log.info("Created region with ID: {}", region.getId());

                        String uniqueCode = "LOC-" + System.currentTimeMillis();
                        log.info("Creating EventLocation with code: {}", uniqueCode);

                        // Create a completely new entity with all required fields set
                        EventLocation eventLocation = new EventLocation();
                        eventLocation.setName("Test Location");
                        eventLocation.setCode(uniqueCode);
                        eventLocation.setMaxSpin(100);
                        eventLocation.setRegion(region); // Set region first
                        eventLocation.setEvent(event); // Set event second
                        eventLocation.setStatus(CommonStatus.ACTIVE); // Set status after relationships
                        eventLocation.setVersion(0L);
                        eventLocation.setCreatedAt(LocalDateTime.now());
                        eventLocation.setUpdatedAt(LocalDateTime.now());
                        eventLocation.setCreatedBy("system");
                        eventLocation.setUpdatedBy("system");

                        // Initialize collections
                        eventLocation.setParticipantEvents(new HashSet<>());
                        eventLocation.setRewards(new HashSet<>());
                        eventLocation.setGoldenHours(new HashSet<>());

                        // Ensure we can persist it
                        log.info("Validating EventLocation entity before persistence");
                        eventLocation.validateState(); // Call validate explicitly if it exists

                        // Try to persist it in a clear transaction
                        log.info("Persisting EventLocation entity");
                        entityManager.persist(eventLocation);

                        // Force a flush to catch any immediate errors
                        entityManager.flush();

                        // Verify immediately with the entityManager
                        Long locationId = eventLocation.getId();

                        // Return the freshly loaded entity to ensure we have the latest state
                        EventLocation managedLocation = entityManager.find(EventLocation.class, locationId);
                        log.info("Successfully persisted and loaded EventLocation: {}", managedLocation);
                        return managedLocation;

                } catch (Exception e) {
                        log.error("Error in createAndSaveEventLocation: {}", e.getMessage(), e);
                        // Roll back the transaction if needed
                        throw new RuntimeException("Failed to create and save EventLocation", e);
                }
        }

        private GoldenHour createAndSaveGoldenHour(EventLocation eventLocation) {
                // Make sure we have the latest EventLocation from the database
                EventLocation managedLocation = entityManager.find(EventLocation.class, eventLocation.getId());
                if (managedLocation == null) {
                        throw new IllegalArgumentException(
                                        "EventLocation with ID " + eventLocation.getId() + " not found in database");
                }

                // Get current count of golden hours to determine which type to create
                long count = entityManager.createQuery("SELECT COUNT(g) FROM GoldenHour g", Long.class)
                                .getSingleResult();
                int goldenHourType = (int) (count % 3);

                Event event = managedLocation.getEvent();
                LocalDateTime eventStart = event.getStartTime();
                LocalDateTime eventEnd = event.getEndTime();

                // Define the three types of golden hours we'll create
                GoldenHourConfig config = getGoldenHourConfig(goldenHourType, eventStart, eventEnd);

                // Create the golden hour entity
                GoldenHour goldenHour = GoldenHour.builder()
                                .eventLocation(managedLocation)
                                .startTime(config.startTime)
                                .endTime(config.endTime)
                                .multiplier(config.multiplier)
                                .status(CommonStatus.ACTIVE)
                                .version(0L)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy("test-user")
                                .updatedBy("test-user")
                                .build();

                // Add to the location's golden hours (bidirectional relationship)
                managedLocation.addGoldenHour(goldenHour);

                // Persist and flush to ensure it's saved
                entityManager.persist(goldenHour);
                entityManager.flush();

                // Re-fetch to verify stored values
                GoldenHour storedGoldenHour = entityManager.find(GoldenHour.class, goldenHour.getId());
                log.info("\nVerified stored golden hour:");
                log.info("  ID: {}", storedGoldenHour.getId());
                log.info("  Start: {} ({})",
                                storedGoldenHour.getStartTime(),
                                storedGoldenHour.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC));
                log.info("  End: {} ({})",
                                storedGoldenHour.getEndTime(),
                                storedGoldenHour.getEndTime().toEpochSecond(java.time.ZoneOffset.UTC));
                log.info("  Status: {}", storedGoldenHour.getStatus());
                log.info("  Expected start >= {} : {}",
                                storedGoldenHour.getStartTime().plusSeconds(5),
                                storedGoldenHour.getStartTime()
                                                .compareTo(storedGoldenHour.getStartTime().plusSeconds(5)) >= 0);

                return storedGoldenHour;
        }

        /**
         * Helper class to organize golden hour configuration values
         */
        private static class GoldenHourConfig {
                final LocalDateTime startTime;
                final LocalDateTime endTime;
                final BigDecimal multiplier;

                GoldenHourConfig(LocalDateTime startTime, LocalDateTime endTime, BigDecimal multiplier) {
                        this.startTime = startTime;
                        this.endTime = endTime;
                        this.multiplier = multiplier;
                }
        }

        /**
         * Helper method to format time range debug info consistently
         */
        private String formatTimeRangeComparison(String label, LocalDateTime value1, LocalDateTime value2,
                        String comparison) {
                return String.format("%s: %s %s %s (epoch diff: %d)",
                                label,
                                value1,
                                comparison,
                                value2,
                                value2.toEpochSecond(java.time.ZoneOffset.UTC)
                                                - value1.toEpochSecond(java.time.ZoneOffset.UTC));
        }

        /**
         * Helper method to create consistent time ranges with buffer and normalized
         * precision
         */
        private LocalDateTime[] createTimeRange(LocalDateTime start, LocalDateTime end, int bufferSeconds) {
                // Ensure consistent time precision
                start = start.plusSeconds(bufferSeconds).withNano(0);
                end = end.minusSeconds(bufferSeconds).withNano(0);
                return new LocalDateTime[] { start, end };
        }

        /**
         * Get the configuration for a specific type of golden hour
         */
        private GoldenHourConfig getGoldenHourConfig(int type, LocalDateTime eventStart, LocalDateTime eventEnd) {
                // Calculate event duration
                long durationSeconds = eventEnd.toEpochSecond(java.time.ZoneOffset.UTC) -
                                eventStart.toEpochSecond(java.time.ZoneOffset.UTC);

                switch (type) {
                        case 0: // Morning golden hour (0% to 25% of event)
                                log.info("\n=== Creating Morning Golden Hour ===");
                                LocalDateTime[] morningRange = createTimeRange(
                                                eventStart,
                                                eventStart.plusSeconds(durationSeconds / 4),
                                                5 // 5-second buffer
                                );
                                log.info("Creating morning golden hour:");
                                log.info("  Event span: {}",
                                                formatTimeRangeComparison("Event", eventStart, eventEnd, "to"));
                                log.info("  Duration: {} seconds", durationSeconds);
                                log.info("  Target range: {}",
                                                formatTimeRangeComparison("", morningRange[0], morningRange[1], "to"));
                                return new GoldenHourConfig(
                                                morningRange[0],
                                                morningRange[1],
                                                BigDecimal.valueOf(2.0));

                        case 1: // Evening golden hour (50% to 75% of event)
                                LocalDateTime[] eveningRange = createTimeRange(
                                                eventStart.plusSeconds(durationSeconds / 2),
                                                eventStart.plusSeconds(durationSeconds * 3 / 4),
                                                5 // 5-second buffer
                                );
                                log.info("\nCreating evening golden hour:");
                                log.info("  Event span: {}",
                                                formatTimeRangeComparison("Event", eventStart, eventEnd, "to"));
                                log.info("  Duration: {} seconds", durationSeconds);
                                log.info("  Target range: {}",
                                                formatTimeRangeComparison("", eveningRange[0], eveningRange[1], "to"));
                                return new GoldenHourConfig(
                                                eveningRange[0],
                                                eveningRange[1],
                                                BigDecimal.valueOf(3.0));

                        case 2: // Noon golden hour (25% to 50% of event) - will be set inactive later
                                LocalDateTime[] noonRange = createTimeRange(
                                                eventStart.plusSeconds(durationSeconds / 4),
                                                eventStart.plusSeconds(durationSeconds / 2),
                                                5 // 5-second buffer
                                );
                                log.info("\nCreating noon golden hour:");
                                log.info("  Event span: {}",
                                                formatTimeRangeComparison("Event", eventStart, eventEnd, "to"));
                                log.info("  Duration: {} seconds", durationSeconds);
                                log.info("  Target range: {}",
                                                formatTimeRangeComparison("", noonRange[0], noonRange[1], "to"));
                                return new GoldenHourConfig(
                                                noonRange[0],
                                                noonRange[1],
                                                BigDecimal.valueOf(1.5));

                        default:
                                throw new IllegalArgumentException("Invalid golden hour type: " + type);
                }
        }

        @Test
        void findByEventId_ShouldReturnAllGoldenHours() {
                List<GoldenHour> goldenHours = goldenHourRepository.findByEventId(event1.getId());

                assertThat(goldenHours).hasSize(3);
        }

        @Test
        void findByEventIdAndStatus_ShouldReturnFilteredGoldenHours() {
                List<GoldenHour> activeGoldenHours = goldenHourRepository.findByEventIdAndStatus(event1.getId(),
                                CommonStatus.ACTIVE);

                assertThat(activeGoldenHours).hasSize(2);

                // Use a comparator that compares BigDecimal values by their numeric value
                // rather than exact representation
                assertThat(activeGoldenHours)
                                .extracting(GoldenHour::getMultiplier)
                                .usingElementComparator((bd1, bd2) -> bd1.compareTo(bd2))
                                .containsExactlyInAnyOrder(new BigDecimal("2.0"), new BigDecimal("3.0"));

                List<GoldenHour> inactiveGoldenHours = goldenHourRepository.findByEventIdAndStatus(event1.getId(),
                                CommonStatus.INACTIVE);

                assertThat(inactiveGoldenHours).hasSize(1);

                // Use same approach for inactive golden hours
                assertThat(inactiveGoldenHours)
                                .extracting(GoldenHour::getMultiplier)
                                .usingElementComparator((bd1, bd2) -> bd1.compareTo(bd2))
                                .containsExactly(new BigDecimal("1.5"));
        }

        @Test
        void findByTimeRangeAndStatus_ShouldReturnMatchingGoldenHours() {
                // The core purpose of this test is to verify that the repository method
                // correctly filters
                // golden hours based on time range and status

                // We've set up three golden hours in our test data:
                // 1. A morning golden hour (0-25% of event duration) with multiplier 2.0
                // 2. An evening golden hour (50-75% of event duration) with multiplier 3.0
                // 3. A noon golden hour (25-50% of event duration) with multiplier 1.5
                // (INACTIVE)

                // Get the event duration in seconds to calculate time ranges
                // Ensure consistent time precision for all times
                LocalDateTime eventStart = event1.getStartTime().withNano(0);
                LocalDateTime eventEnd = event1.getEndTime().withNano(0);
                // Update event times in database to ensure consistent precision
                event1.setStartTime(eventStart);
                event1.setEndTime(eventEnd);
                entityManager.merge(event1);
                entityManager.flush();

                long durationSeconds = eventEnd.toEpochSecond(java.time.ZoneOffset.UTC) -
                                eventStart.toEpochSecond(java.time.ZoneOffset.UTC);

                log.info("\n=== Starting Morning Time Range Test ===");
                log.info("Event details:");
                log.info("  - ID: {}", event1.getId());
                log.info("  - Start: {}", eventStart);
                log.info("  - End: {}", eventEnd);
                log.info("  - Duration: {} seconds", durationSeconds);

                // Case 1: Query the morning time range (should return the morning golden hour)
                LocalDateTime[] morningRange = createTimeRange(
                                eventStart,
                                eventStart.plusSeconds(durationSeconds / 4),
                                5 // Use same 5-second buffer as golden hour creation
                );
                LocalDateTime morningStart = morningRange[0];
                LocalDateTime morningEnd = morningRange[1];

                log.info("\n=== Time Range Details ===");
                log.info("Event span: {} to {}", eventStart, eventEnd);
                log.info("Duration in seconds: {}", durationSeconds);
                log.info("Morning query window:");
                log.info("  Start: {} (epoch seconds: {})", morningStart,
                                morningStart.toEpochSecond(java.time.ZoneOffset.UTC));
                log.info("  End: {} (epoch seconds: {})", morningEnd,
                                morningEnd.toEpochSecond(java.time.ZoneOffset.UTC));

                List<GoldenHour> allHours = goldenHourRepository.findAll();
                log.info("\nAll Golden Hours ({} total):", allHours.size());
                allHours.forEach(gh -> {
                        log.info("- Golden Hour ID: {}", gh.getId());
                        log.info("  Start Time: {} (epoch: {})",
                                        gh.getStartTime(),
                                        gh.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC));
                        log.info("  End Time: {} (epoch: {})",
                                        gh.getEndTime(),
                                        gh.getEndTime().toEpochSecond(java.time.ZoneOffset.UTC));
                        log.info("  Multiplier: {}", gh.getMultiplier());
                        log.info("  Status: {}", gh.getStatus());
                        log.info("  Event ID: {}", gh.getEventLocation().getEvent().getId());
                        boolean startMatch = gh.getStartTime().compareTo(morningStart) >= 0;
                        boolean endMatch = gh.getEndTime().compareTo(morningEnd) <= 0;
                        log.info("  Time Range Check:");
                        log.info("    {}", formatTimeRangeComparison(
                                        "Start Time", gh.getStartTime(), morningStart, ">="));
                        log.info("    {}", formatTimeRangeComparison(
                                        "End Time", gh.getEndTime(), morningEnd, "<="));
                        log.info("    Results: start match = {}, end match = {}, overall = {}",
                                        startMatch, endMatch, startMatch && endMatch);
                        log.info("  Status Match: {}", gh.getStatus() == CommonStatus.ACTIVE);
                        log.info("");
                });

                log.info("\n=== Executing Repository Query ===");
                log.info("Parameters:");
                log.info("  - Event ID: {}", event1.getId());
                log.info("  - Start Time: {}", morningStart);
                log.info("  - End Time: {}", morningEnd);
                log.info("  - Status: {}", CommonStatus.ACTIVE);

                // Print native SQL for debugging
                String sql = "SELECT gh.* FROM golden_hours gh " +
                                "JOIN event_locations el ON gh.event_location_id = el.id " +
                                "WHERE el.event_id = " + event1.getId() + " " +
                                "AND gh.start_time >= '" + morningStart + "' " +
                                "AND gh.end_time <= '" + morningEnd + "' " +
                                "AND gh.status = 'ACTIVE'";
                log.info("\nEquivalent SQL:\n{}", sql);

                List<GoldenHour> morningHours = goldenHourRepository.findByEventIdAndTimeRangeAndStatus(
                                event1.getId(), morningStart, morningEnd, CommonStatus.ACTIVE);

                // If no results found, verify time precision
                if (morningHours.isEmpty()) {
                        // Execute a direct JPQL query with debug info
                        log.info("\nVerifying time precision in database:");
                        List<Object[]> timeRanges = entityManager.createQuery(
                                        "SELECT gh.id, gh.startTime, gh.endTime, gh.status " +
                                                        "FROM GoldenHour gh " +
                                                        "WHERE gh.eventLocation.event.id = :eventId " +
                                                        "ORDER BY gh.startTime",
                                        Object[].class)
                                        .setParameter("eventId", event1.getId())
                                        .getResultList();

                        log.info("Database records for event {}:", event1.getId());
                        timeRanges.forEach(record -> {
                                LocalDateTime storedStart = (LocalDateTime) record[1];
                                LocalDateTime storedEnd = (LocalDateTime) record[2];
                                log.info("Record ID: {}", record[0]);
                                log.info("  Status: {}", record[3]);
                                log.info("  Stored Start:  {} ({})", storedStart,
                                                storedStart.toEpochSecond(java.time.ZoneOffset.UTC));
                                log.info("  Search Start:  {} ({})", morningStart,
                                                morningStart.toEpochSecond(java.time.ZoneOffset.UTC));
                                log.info("  Start Compare: {}", storedStart.compareTo(morningStart));
                                log.info("  Stored End:    {} ({})", storedEnd,
                                                storedEnd.toEpochSecond(java.time.ZoneOffset.UTC));
                                log.info("  Search End:    {} ({})", morningEnd,
                                                morningEnd.toEpochSecond(java.time.ZoneOffset.UTC));
                                log.info("  End Compare:   {}", storedEnd.compareTo(morningEnd));
                                log.info("");
                        });
                }

                log.info("\nQuery Results:");
                log.info("Number of results: {}", morningHours.size());
                morningHours.forEach(gh -> {
                        log.info("Morning hour found:");
                        log.info("  ID: {}", gh.getId());
                        log.info("  Start: {} ({})", gh.getStartTime(),
                                        gh.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC));
                        log.info("  End: {} ({})", gh.getEndTime(),
                                        gh.getEndTime().toEpochSecond(java.time.ZoneOffset.UTC));
                });

                // Verify exactly one active golden hour in morning range with multiplier 2.0
                assertThat(morningHours).hasSize(1);
                assertThat(morningHours.get(0).getMultiplier())
                                .isEqualByComparingTo(new BigDecimal("2.0"));

                // Case 2: Query the evening time range (should return the evening golden hour)
                LocalDateTime[] eveningRange = createTimeRange(
                                eventStart.plusSeconds(durationSeconds / 2),
                                eventStart.plusSeconds(durationSeconds * 3 / 4),
                                5 // Use same 5-second buffer
                );
                log.info("\n=== Evening Time Range ===");
                log.info("Start: {} ({})", eveningRange[0],
                                eveningRange[0].toEpochSecond(java.time.ZoneOffset.UTC));
                log.info("End: {} ({})", eveningRange[1],
                                eveningRange[1].toEpochSecond(java.time.ZoneOffset.UTC));

                List<GoldenHour> eveningHours = goldenHourRepository.findByEventIdAndTimeRangeAndStatus(
                                event1.getId(), eveningRange[0], eveningRange[1], CommonStatus.ACTIVE);

                // Verify exactly one active golden hour in evening range with multiplier 3.0
                assertThat(eveningHours).hasSize(1);
                assertThat(eveningHours.get(0).getMultiplier())
                                .isEqualByComparingTo(new BigDecimal("3.0"));

                // Case 3: Query the noon time range (should return no active golden hours)
                LocalDateTime[] noonRange = createTimeRange(
                                eventStart.plusSeconds(durationSeconds / 4),
                                eventStart.plusSeconds(durationSeconds / 2),
                                5 // Use same 5-second buffer
                );
                log.info("\n=== Noon Time Range ===");
                log.info("Start: {} ({})", noonRange[0],
                                noonRange[0].toEpochSecond(java.time.ZoneOffset.UTC));
                log.info("End: {} ({})", noonRange[1],
                                noonRange[1].toEpochSecond(java.time.ZoneOffset.UTC));

                List<GoldenHour> noonHours = goldenHourRepository.findByEventIdAndTimeRangeAndStatus(
                                event1.getId(), noonRange[0], noonRange[1], CommonStatus.ACTIVE);

                // Verify no active golden hours in noon range (it's set to INACTIVE)
                assertThat(noonHours).isEmpty();
        }
}