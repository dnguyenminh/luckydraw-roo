package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for the GoldenHour entity.
 * This class tests the functionality of the GoldenHour entity, including:
 * - Time window checks
 * - Multiplier functionality
 * - Overlap detection
 * - Status management
 */
class GoldenHourTest {

    private GoldenHour goldenHour;
    private Event event;
    private EventLocation location;
    private LocalDateTime now;
    @SuppressWarnings("unused") // Field used for test setup
    private Province province;
    private Region region; // Added missing field declaration

    /**
     * Sets up the test environment.
     */
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        event = Event.builder()
                .name("Test Event")
                .code("TEST_EVENT")
                .status(CommonStatus.ACTIVE)
                .startTime(now.minusHours(2))
                .endTime(now.plusHours(2))
                .build();
                
        region = Region.builder()
                .code("TEST_REGION")
                .name("Test Region")
                .status(CommonStatus.ACTIVE)
                .build();

        province = Province.builder()
                .code("TEST_PROV")
                .name("Test Province")
                .status(CommonStatus.ACTIVE)
                .region(region)
                .build();
                
        location = EventLocation.builder()
                .name("Test Location")
                .code("TEST_LOC")
                .status(CommonStatus.ACTIVE)
                .region(region)
                .maxSpin(1000)
                .goldenHours(new HashSet<>())
                .build();

        // Set up bidirectional relationships
        region.addEventLocation(location);
        event.addLocation(location);
                
        goldenHour = GoldenHour.builder()
                .multiplier(BigDecimal.valueOf(2.0))
                .status(CommonStatus.ACTIVE)
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(1))
                .build();
                
        // Set up bidirectional relationship
        location.addGoldenHour(goldenHour);
    }

    /**
     * Tests active status checking based on given time
     */
    @Test
    void testIsActive() {
        assertTrue(goldenHour.isActive(now));
        
        // Before start time
        assertFalse(goldenHour.isActive(now.minusHours(2)));
        
        // After end time
        assertFalse(goldenHour.isActive(now.plusHours(2)));
        
        // Inactive status
        goldenHour.setStatus(CommonStatus.INACTIVE);
        assertFalse(goldenHour.isActive(now));
    }

    /**
     * Tests overlap detection between golden hours
     */
    @Test
    void testOverlaps() {
        // Same time window
        GoldenHour other = goldenHour.toBuilder()
            .startTime(goldenHour.getStartTime())
            .endTime(goldenHour.getEndTime())
            .build();
        assertTrue(goldenHour.overlaps(other));

        // Overlapping windows
        other.setStartTime(goldenHour.getStartTime().minusHours(1));
        other.setEndTime(goldenHour.getStartTime().plusMinutes(30));
        assertTrue(goldenHour.overlaps(other));

        // Non-overlapping windows
        other.setStartTime(goldenHour.getEndTime().plusHours(1));
        other.setEndTime(goldenHour.getEndTime().plusHours(2));
        assertFalse(goldenHour.overlaps(other));
    }

    /**
     * Tests state validation
     */
    @Test
    void testValidateState() {
        // Test end time before start time
        assertThrows(IllegalStateException.class, () -> {
            goldenHour.setEndTime(goldenHour.getStartTime().minusHours(1));
            goldenHour.validateState();
        });

        // Test multiplier range
        assertThrows(IllegalStateException.class, () -> {
            goldenHour.setMultiplier(BigDecimal.ZERO);
            goldenHour.validateState();
        });

        // Test time window within event
        assertThrows(IllegalStateException.class, () -> {
            goldenHour.setStartTime(event.getStartTime().minusHours(1));
            goldenHour.validateState();
        });

        assertThrows(IllegalStateException.class, () -> {
            goldenHour.setEndTime(event.getEndTime().plusHours(1));
            goldenHour.validateState();
        });
    }

    /**
     * Tests relationship with event location
     */
    @Test
    void testLocationRelationship() {
        assertTrue(location.getGoldenHours().contains(goldenHour));
        assertEquals(location, goldenHour.getEventLocation());

        // Create and set new location
        EventLocation newLocation = EventLocation.builder()
                .name("New Location")
                .code("NEW_LOC")
                .status(CommonStatus.ACTIVE)
                .region(region)
                .maxSpin(500)
                .goldenHours(new HashSet<>())
                .build();

        // Set up bidirectional relationships
        region.addEventLocation(newLocation);
        event.addLocation(newLocation);

        goldenHour.setEventLocation(newLocation);
        
        assertTrue(newLocation.getGoldenHours().contains(goldenHour));
        assertFalse(location.getGoldenHours().contains(goldenHour));
    }
}
