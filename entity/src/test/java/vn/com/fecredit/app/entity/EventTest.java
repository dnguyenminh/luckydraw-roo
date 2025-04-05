package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Test class for the Event entity.
 * This class tests the functionality of the Event entity, including:
 * - Location management (adding/removing)
 * - Province overlap validation
 * - Time-based activation rules
 * - Default location selection
 * - Null handling in relationships
 */
class EventTest {

    private Event event;
    private Region region1;
    private Region region2;
    private Province province1;
    private Province province2;
    private EventLocation location1;
    private EventLocation location2;

    /**
     * Sets up the test environment before each test.
     * Creates and configures all necessary entities for testing Event:
     * - Event with time boundaries and default values
     * - Two regions for testing location associations
     * - Two provinces for testing overlap validation
     * - Two event locations for testing association management
     */
    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        region1 = Region.builder()
            .name("Region 1")
            .code("REG1")
            .status(CommonStatus.ACTIVE)
            .build();

        region2 = Region.builder()
            .name("Region 2")
            .code("REG2")
            .status(CommonStatus.ACTIVE)
            .build();

        province1 = Province.builder()
            .name("Province 1")
            .code("PROV1")
            .region(region1)
            .status(CommonStatus.ACTIVE)
            .build();

        province2 = Province.builder()
            .name("Province 2")
            .code("PROV2")
            .region(region2)
            .status(CommonStatus.ACTIVE)
            .build();

        location1 = EventLocation.builder()
            .name("Location 1")
            .code("LOC1")
            .region(region1)
            .status(CommonStatus.ACTIVE)
            .maxSpin(1000)
            .build();

        location2 = EventLocation.builder()
            .name("Location 2")
            .code("LOC2")
            .region(region2)
            .status(CommonStatus.ACTIVE)
            .maxSpin(1000)
            .build();

        event = Event.builder()
            .name("Test Event")
            .code("TEST_EVENT")
            .status(CommonStatus.ACTIVE)
            .startTime(now)
            .endTime(now.plusDays(7))
            .build();

        // Set up bidirectional relationships
        region1.addProvince(province1);
        region2.addProvince(province2);
        region1.addEventLocation(location1);
        region2.addEventLocation(location2);
    }

    /**
     * Tests the bidirectional relationship between Event and EventLocation.
     * Verifies that:
     * - Locations are properly added to the event's collection
     * - The event reference is properly set in the locations
     * - Removing a location updates both sides of the relationship
     */
    @Test
    void testEventLocationAssociation() {
        // Add locations to event
        event.addLocation(location1);
        event.addLocation(location2);

        assertEquals(2, event.getLocations().size());
        assertTrue(event.getLocations().contains(location1));
        assertTrue(event.getLocations().contains(location2));
        assertEquals(event, location1.getEvent());
        assertEquals(event, location2.getEvent());

        // Test removal
        event.removeLocation(location1);
        assertEquals(1, event.getLocations().size());
        assertFalse(event.getLocations().contains(location1));
        assertNull(location1.getEvent());
    }

    /**
     * Tests that an event cannot have locations with overlapping provinces.
     * Verifies that an exception is thrown when attempting to add a location
     * with a region that contains a province already covered by another location's region.
     */
    @Test
    void testOverlappingProvinces() {
        // Setup regions with overlapping province
        region1.addProvince(province1);
        region2.addProvince(province1); 

        event.addLocation(location1);
        
        // Should throw exception when adding location with overlapping province
        assertThrows(IllegalArgumentException.class, () -> {
            event.addLocation(location2);
        });
    }

    /**
     * Tests the time-based activation rules for Event.
     * Verifies that an event is only active when:
     * - The current time is between the start and end times
     * - Both start and end times are non-null
     * - The status is ACTIVE
     */
    @Test
    void testEventTimeBoundaryActivation() {
        LocalDateTime now = LocalDateTime.now();
        
        // Test current active event
        event.setStartTime(now.minusDays(1));
        event.setEndTime(now.plusDays(1));
        event.setStatus(CommonStatus.ACTIVE);
        assertTrue(event.isActive());

        // Test past event
        event.setStartTime(now.minusDays(2));
        event.setEndTime(now.minusDays(1));
        assertFalse(event.isActive());

        // Test future event
        event.setStartTime(now.plusDays(1));
        event.setEndTime(now.plusDays(2));
        assertFalse(event.isActive());

        // Test inactive status
        event.setStartTime(now.minusDays(1));
        event.setEndTime(now.plusDays(1));
        event.setStatus(CommonStatus.INACTIVE);
        assertFalse(event.isActive());

        // Test null dates
        event.setStatus(CommonStatus.ACTIVE);
        event.setStartTime(null);
        event.setEndTime(null);
        assertFalse(event.isActive());
    }

    /**
     * Tests the default location selection mechanism.
     * Verifies that:
     * - The default location is null when no locations are added
     * - The first location added becomes the default
     * - The default location changes when the first location is removed
     */
    @Test
    void testDefaultLocation() {
        assertNull(event.getDefaultLocation());

        event.addLocation(location1);
        assertEquals(location1, event.getDefaultLocation());

        event.addLocation(location2);
        // Default location should still be the first one added
        assertEquals(location1, event.getDefaultLocation());

        event.removeLocation(location1);
        assertEquals(location2, event.getDefaultLocation());
    }

    /**
     * Tests that the event properly handles null locations.
     * Verifies that adding or removing null locations does not cause exceptions.
     */
    @Test
    void testNullLocationHandling() {
        assertDoesNotThrow(() -> event.addLocation(null));
        assertDoesNotThrow(() -> event.removeLocation(null));
        assertTrue(event.getLocations().isEmpty());
    }

    /**
     * Tests that the event properly handles locations with null regions.
     * Verifies that province overlap validation is skipped when a region is null.
     */
    @Test
    void testOverlappingProvincesWithNull() {
        location1.setRegion(null);
        event.addLocation(location1);
        // Should not throw exception when region is null
        assertDoesNotThrow(() -> event.addLocation(location2));
    }
}
