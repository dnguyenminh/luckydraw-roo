package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;

class RegionTest {

    private Region region;
    private Province province;
    private EventLocation location;
    private Event event;

    @BeforeEach
    void setUp() {
        region = Region.builder()
            .name("Test Region")
            .code("TEST_REG")
            .status(CommonStatus.ACTIVE)
            .build();

        province = Province.builder()
            .name("Test Province")
            .code("TEST_PROV")
            .status(CommonStatus.ACTIVE)
            .build();

        event = Event.builder()
            .name("Test Event")
            .code("TEST_EVENT")
            .status(CommonStatus.ACTIVE)
            .build();

        location = EventLocation.builder()
            .name("Test Location")
            .code("TEST_LOC")
            .status(CommonStatus.ACTIVE)
            .maxSpin(1000)
            .event(event)
            .region(region)
            .rewards(new HashSet<>())
            .goldenHours(new HashSet<>())
            .participantEvents(new HashSet<>())
            .build();
    }

    @Test
    void testProvinceAssociation() {
        assertTrue(region.getProvinces().isEmpty());
        region.addProvince(province);
        
        assertEquals(1, region.getProvinces().size());
        assertTrue(region.getProvinces().contains(province));
        assertEquals(region, province.getRegion());

        region.removeProvince(province);
        assertTrue(region.getProvinces().isEmpty());
        assertNull(province.getRegion());
    }

    @Test
    void testEventLocationAssociation() {
        assertTrue(region.getEventLocations().isEmpty());
        region.addEventLocation(location);
        
        assertEquals(1, region.getEventLocations().size());
        assertTrue(region.getEventLocations().contains(location));
        assertEquals(region, location.getRegion());

        region.removeEventLocation(location);
        assertTrue(region.getEventLocations().isEmpty());
        assertNull(location.getRegion());
    }

    @Test
    void testActiveCounters() {
        region.addProvince(province);
        region.addEventLocation(location);
        
        assertEquals(1, region.getActiveProvinceCount());
        assertEquals(1, region.getActiveEventLocationCount());

        province.setStatus(CommonStatus.INACTIVE);
        location.setStatus(CommonStatus.INACTIVE);
        
        assertEquals(0, region.getActiveProvinceCount());
        assertEquals(0, region.getActiveEventLocationCount());
    }

    @Test
    void testStatusManagement() {
        region.addProvince(province);
        region.addEventLocation(location);
        
        // Test region deactivation - should cascade to locations
        assertDoesNotThrow(() -> region.setStatus(CommonStatus.INACTIVE),
            "Should allow deactivation regardless of children status");
        assertFalse(region.isActive());
        assertFalse(location.getStatus().isActive(),
            "EventLocation should be deactivated when region is deactivated");

        // Test region stays inactive after province activation
        province.setStatus(CommonStatus.ACTIVE);
        assertFalse(region.isActive(),
            "Region should stay inactive when manually deactivated");

        // Test region deactivation based on province status
        province.setStatus(CommonStatus.INACTIVE);
        assertFalse(region.isActive(),
            "Region should be deactivated when last province becomes inactive");

        // Test manual activation
        region.setStatus(CommonStatus.ACTIVE);
        assertTrue(region.isActive(),
            "Should allow manual activation");
    }

    @Test
    void testValidation() {
        Region invalid = Region.builder().build();
        assertThrows(IllegalStateException.class, () -> invalid.validateState());

        region.setCode("lowercase_code");
        region.validateState();
        assertEquals("LOWERCASE_CODE", region.getCode());
    }

    // @Test
    // void testProvinceStatusSync() {
    //     Province province2 = Province.builder()
    //         .name("Test Province 2")
    //         .code("TEST_PROV2")
    //         .status(CommonStatus.ACTIVE)
    //         .build();

    //     region.addProvince(province);
    //     region.addProvince(province2);

    //     // Test inactive region stays inactive when provinces change
    //     region.setStatus(CommonStatus.INACTIVE);
    //     province.setStatus(CommonStatus.ACTIVE);
    //     assertFalse(region.isActive(),
    //         "Region should stay inactive when manually deactivated");

    //     // Test active region stays active with one active province
    //     region.setStatus(CommonStatus.ACTIVE);
    //     province.setStatus(CommonStatus.INACTIVE);
    //     assertTrue(region.isActive(),
    //         "Region should remain active when at least one province is active");

    //     // Test active region becomes inactive when all provinces inactive
    //     province2.setStatus(CommonStatus.INACTIVE);
    //     assertFalse(region.isActive(),
    //         "Active region should become inactive when all provinces are inactive");

    //     // Test manual activation is allowed regardless of province status
    //     region.setStatus(CommonStatus.ACTIVE);
    //     assertTrue(region.isActive(),
    //         "Should allow manual activation regardless of province status");
    // }

    @Test
    void testEventLocationStatus() {
        region.addEventLocation(location);
        location.setStatus(CommonStatus.ACTIVE);
        assertTrue(location.getStatus().isActive());

        // When region becomes inactive, location should become inactive
        region.setStatus(CommonStatus.INACTIVE);
        assertFalse(location.getStatus().isActive(),
            "Location should be inactive when region is deactivated");

        // Location should stay inactive when region is reactivated
        region.setStatus(CommonStatus.ACTIVE);
        assertFalse(location.getStatus().isActive(),
            "Location should remain inactive after region reactivation");

        // Location can be activated when region is active
        location.setStatus(CommonStatus.ACTIVE);
        assertTrue(location.getStatus().isActive(),
            "Location can be activated when region is active");
    }

    @Test
    void testProvinceStatusSync() {
        // Create a region with multiple provinces
        Region region = Region.builder()
                .name("Test Region")
                .code("TEST_REG")
                .status(CommonStatus.ACTIVE)
                .build();
        
        Province province1 = Province.builder()
                .name("Province 1")
                .code("PROV_1")
                .status(CommonStatus.ACTIVE)
                .build();
        
        Province province2 = Province.builder()
                .name("Province 2")
                .code("PROV_2")
                .status(CommonStatus.ACTIVE)
                .build();
        
        // Manually set up bidirectional relationships
        region.addProvince(province1);
        region.addProvince(province2);
        
        // Region and both provinces start as active
        assertTrue(region.isActive(), "Region should be active initially");
        assertTrue(province1.isActive(), "Province 1 should be active initially");
        assertTrue(province2.isActive(), "Province 2 should be active initially");
        
        // Deactivate one province - region should remain active
        province1.setStatus(CommonStatus.INACTIVE);
        assertFalse(province1.isActive(), "Province 1 should be inactive after deactivation");
        assertTrue(province2.isActive(), "Province 2 should remain active");
        assertTrue(region.isActive(), "Region should remain active when at least one province is active");
        
        // Deactivate the other province - now region should be inactive
        province2.setStatus(CommonStatus.INACTIVE);
        assertFalse(province2.isActive(), "Province 2 should be inactive after deactivation");
        assertFalse(region.isActive(), "Region should be inactive when all provinces are inactive");
    }
}
