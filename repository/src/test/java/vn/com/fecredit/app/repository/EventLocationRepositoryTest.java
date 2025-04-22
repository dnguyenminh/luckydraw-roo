package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.EventLocationKey;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@DataJpaTest
@ActiveProfiles("test")
class EventLocationRepositoryTest {

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegionRepository regionRepository;

    private Long testEventId;
    private Long testRegionId;
    private EventLocationKey testLocationId;

    @BeforeEach
    void setUp() {
        eventLocationRepository.deleteAll();
        eventRepository.deleteAll();
        regionRepository.deleteAll();

        createTestData();
    }

    private void createTestData() {
        // First, ensure we have a region
        Region region = new Region();
        region.setName("Test Region");
        region.setCode("TEST_REGION");
        region.setStatus(CommonStatus.ACTIVE);
        // Add required audit fields
        LocalDateTime now = LocalDateTime.now();
        region.setCreatedBy("test-user");
        region.setCreatedAt(now);
        region.setUpdatedBy("test-user");
        region.setUpdatedAt(now);
        region = regionRepository.save(region);
        
        // Create a second region for the second EventLocation
        Region region2 = new Region();
        region2.setName("Test Region 2");
        region2.setCode("TEST_REGION_2");
        region2.setStatus(CommonStatus.ACTIVE);
        // Add required audit fields
        region2.setCreatedBy("test-user");
        region2.setCreatedAt(now);
        region2.setUpdatedBy("test-user");
        region2.setUpdatedAt(now);
        region2 = regionRepository.save(region2);
        
        // Store test region ID for later use
        testRegionId = region.getId();
        
        // Create ONE event that will be used by both locations
        Event event = new Event();
        event.setName("Test Event");
        event.setCode("TEST_EVENT");
        event.setStatus(CommonStatus.ACTIVE);
        event.setStartTime(now);
        event.setEndTime(now.plusDays(7));
        // Add required audit fields
        event.setCreatedBy("test-user");
        event.setCreatedAt(now);
        event.setUpdatedBy("test-user");
        event.setUpdatedAt(now);
        event = eventRepository.save(event);
        
        // Store test event ID for later use
        testEventId = event.getId();
        
        // Create an event location with the first region
        EventLocation eventLocation1 = new EventLocation();
        
        // IMPORTANT: Create a properly initialized key
        EventLocationKey key1 = new EventLocationKey();
        key1.setEventId(event.getId());
        key1.setRegionId(region.getId());
        eventLocation1.setId(key1);
        
        eventLocation1.setEvent(event);
        eventLocation1.setRegion(region);
        eventLocation1.setStatus(CommonStatus.ACTIVE);
        eventLocation1.setMaxSpin(100);
        eventLocation1.setTodaySpin(50);
        eventLocation1.setDescription("Active location for testing");
        // Add required audit fields
        eventLocation1.setCreatedBy("test-user");
        eventLocation1.setCreatedAt(now);
        eventLocation1.setUpdatedBy("test-user");
        eventLocation1.setUpdatedAt(now);
        
        // Save the entity
        EventLocation savedLocation1 = eventLocationRepository.save(eventLocation1);
        testLocationId = savedLocation1.getId();
        
        // Clear the persistence context to avoid conflicts
        eventLocationRepository.flush();
        
        // Create a second event location with SAME EVENT but different region
        // This maintains a unique composite key while allowing us to test findByEventId
        EventLocation eventLocation2 = new EventLocation();
        
        // IMPORTANT: Create a new key with same event ID but different region ID
        EventLocationKey key2 = new EventLocationKey();
        key2.setEventId(event.getId()); // USING THE SAME EVENT ID
        key2.setRegionId(region2.getId());
        eventLocation2.setId(key2);
        
        eventLocation2.setEvent(event);  // SAME event as the first location
        eventLocation2.setRegion(region2); // Different region
        eventLocation2.setStatus(CommonStatus.INACTIVE);
        eventLocation2.setMaxSpin(50);
        eventLocation2.setTodaySpin(25);
        eventLocation2.setDescription("Inactive location for testing");
        // Add required audit fields
        eventLocation2.setCreatedBy("test-user");
        eventLocation2.setCreatedAt(now);
        eventLocation2.setUpdatedBy("test-user");
        eventLocation2.setUpdatedAt(now);
        
        // Save the second entity
        eventLocationRepository.save(eventLocation2);
    }

    @Test
    void findById_shouldReturnEventLocation() {
        EventLocation location = eventLocationRepository.findById(testLocationId).orElse(null);
        assertThat(location).isNotNull();
        assertThat(location.getEvent().getId()).isEqualTo(testEventId);
        assertThat(location.getRegion().getId()).isEqualTo(testRegionId);
    }

    @Test
    void findAll_shouldReturnAllEventLocations() {
        List<EventLocation> locations = eventLocationRepository.findAll();
        assertThat(locations).hasSize(2);
    }

    @Test
    void findByEventId_shouldReturnEventLocationsForEvent() {
        List<EventLocation> locations = eventLocationRepository.findByEventId(testEventId);
        assertThat(locations).hasSize(2);
        // Don't check for specific codes that don't exist in our test data
        // Instead verify other attributes we know should be different
        assertThat(locations).extracting("description")
            .containsOnly("Active location for testing", "Inactive location for testing");
    }

    @Test
    void findByRegionId_shouldReturnEventLocationsForRegion() {
        List<EventLocation> locations = eventLocationRepository.findByRegionId(testRegionId);
        assertThat(locations).hasSize(1); // Only one location has this region ID
        assertThat(locations.get(0).getDescription()).isEqualTo("Active location for testing");
    }

    @Test
    void save_shouldCreateEventLocation() {
        // Create brand new test entities to avoid session conflicts
        
        // Create a new region just for this test
        Region newRegion = new Region();
        newRegion.setName("Test Region for Save");
        newRegion.setCode("TEST_SAVE_REGION");
        newRegion.setStatus(CommonStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        newRegion.setCreatedBy("test-save");
        newRegion.setCreatedAt(now);
        newRegion.setUpdatedBy("test-save");
        newRegion.setUpdatedAt(now);
        newRegion = regionRepository.saveAndFlush(newRegion);
        
        // Create a new event just for this test
        Event newEvent = new Event();
        newEvent.setName("Test Event for Save");
        newEvent.setCode("TEST_SAVE_EVENT");
        newEvent.setStatus(CommonStatus.ACTIVE);
        newEvent.setStartTime(now);
        newEvent.setEndTime(now.plusDays(7));
        newEvent.setCreatedBy("test-save");
        newEvent.setCreatedAt(now);
        newEvent.setUpdatedBy("test-save");
        newEvent.setUpdatedAt(now);
        newEvent = eventRepository.saveAndFlush(newEvent);
        
        // Clear the persistence context to ensure fresh state
        eventRepository.flush();
        regionRepository.flush();
        
        // Create new location with proper composite key FIRST
        EventLocationKey key = EventLocationKey.of(newEvent.getId(), newRegion.getId());
        
        // Create the event location and set its key and other properties
        EventLocation newLocation = new EventLocation();
        newLocation.setId(key); // Set the ID first
        newLocation.setEvent(newEvent);
        newLocation.setRegion(newRegion);
        
        // Set required fields
        newLocation.setDescription("New location for testing save operation");
        newLocation.setStatus(CommonStatus.ACTIVE);
        newLocation.setMaxSpin(10);
        newLocation.setTodaySpin(5);
        newLocation.setDailySpinDistributingRate(0.25);
        newLocation.setCreatedBy("test-save");
        newLocation.setCreatedAt(now);
        newLocation.setUpdatedBy("test-save");
        newLocation.setUpdatedAt(now);
        
        // Save and verify (using saveAndFlush to ensure immediate persistence)
        EventLocation savedLocation = eventLocationRepository.saveAndFlush(newLocation);
        
        // Assertions
        assertThat(savedLocation).isNotNull();
        assertThat(savedLocation.getId()).isNotNull();
        assertThat(savedLocation.getId().getEventId()).isEqualTo(newEvent.getId());
        assertThat(savedLocation.getId().getRegionId()).isEqualTo(newRegion.getId());
        
        // Verify can be found
        EventLocation found = eventLocationRepository.findById(savedLocation.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getDescription()).isEqualTo("New location for testing save operation");
    }
}
