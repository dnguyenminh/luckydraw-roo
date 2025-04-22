package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;

class EventLocationTest {

    private EventLocation location1;
    private EventLocation location2;
    private Event event1;
    private Event event2;
    private Region region1;
    private Region region2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        region1 = Region.builder()
            .name("Region One")
            .code("REG_ONE")
            .status(CommonStatus.ACTIVE)
            .build();

        region2 = Region.builder()
            .name("Region Two")
            .code("REG_TWO")
            .status(CommonStatus.ACTIVE)
            .build();

        event1 = Event.builder()
            .name("Event One")
            .code("EVENT_ONE")
            .startTime(now)
            .endTime(now.plusHours(2))
            .status(CommonStatus.ACTIVE)
            .build();

        event2 = Event.builder()
            .name("Event Two")
            .code("EVENT_TWO")
            .startTime(now.plusHours(3))
            .endTime(now.plusHours(5))
            .status(CommonStatus.ACTIVE)
            .build();

        location1 = EventLocation.builder()
            .maxSpin(1000)
            .event(event1)
            .region(region1)
            .rewardEvents(new HashSet<>())
            .goldenHours(new HashSet<>())
            .participantEvents(new HashSet<>())
            .status(CommonStatus.ACTIVE)
            .build();

        location2 = EventLocation.builder()
            .event(event2)
            .region(region2)
            .rewardEvents(new HashSet<>())
            .goldenHours(new HashSet<>())
            .participantEvents(new HashSet<>())
            .status(CommonStatus.ACTIVE)
            .build();

        // Setup bidirectional relationships
        event1.addLocation(location1);
        event2.addLocation(location2);
        region1.addEventLocation(location1);
        region2.addEventLocation(location2);
    }

    @Test
    void testRegionAssociation() {
        assertEquals(region1, location1.getRegion());
        assertEquals(region2, location2.getRegion());
        assertTrue(region1.getEventLocations().contains(location1));
        assertTrue(region2.getEventLocations().contains(location2));

        Region newRegion = Region.builder()
            .name("New Region")
            .code("NEW_REG")
            .status(CommonStatus.ACTIVE)
            .build();

        location1.setRegion(newRegion);
        assertEquals(newRegion, location1.getRegion());
        assertFalse(region1.getEventLocations().contains(location1));
        assertTrue(newRegion.getEventLocations().contains(location1));

        location1.setRegion(null);
        assertNull(location1.getRegion());
        assertFalse(newRegion.getEventLocations().contains(location1));
    }

    @Test
    void testBidirectionalRegionAssociation() {
        EventLocation newLocation = EventLocation.builder()
            .maxSpin(500)
            .region(region1)
            .rewardEvents(new HashSet<>())
            .goldenHours(new HashSet<>())
            .participantEvents(new HashSet<>())
            .status(CommonStatus.ACTIVE)
            .build();

        region1.addEventLocation(newLocation);
        assertEquals(region1, newLocation.getRegion());
        assertTrue(region1.getEventLocations().contains(newLocation));

        region1.removeEventLocation(newLocation);
        assertNull(newLocation.getRegion());
        assertFalse(region1.getEventLocations().contains(newLocation));
    }

    @Test
    void testEventAssociation() {
        assertEquals(event1, location1.getEvent());
        assertEquals(event2, location2.getEvent());
        assertTrue(event1.getLocations().contains(location1));
        assertTrue(event2.getLocations().contains(location2));
    }

    @Test
    void testStatusManagement() {
        region1.setStatus(CommonStatus.INACTIVE);
        assertThrows(IllegalStateException.class, () -> location1.markAsActive(),
            "Should not allow activation when region is inactive");

        region1.markAsActive();
        assertDoesNotThrow(() -> location1.markAsActive(),
            "Should allow activation when region is active");
        assertTrue(location1.isActive());
    }

    @Test
    void testValidation() {
        assertThrows(IllegalStateException.class, () -> {
            EventLocation invalid = EventLocation.builder().build();
            invalid.validateState();
        });

        location1.setRegion(null);
        assertThrows(IllegalStateException.class, () -> location1.validateState());

        location1.setRegion(region1);
        region1.setStatus(CommonStatus.INACTIVE);
        assertThrows(IllegalStateException.class, () -> location1.setStatus(CommonStatus.ACTIVE)); // Fix: Test
        // setStatus()

        region1.markAsActive();
        location1.setMaxSpin(-1);
        assertThrows(IllegalStateException.class, () -> location1.validateState(),
            "Should not allow negative max spins");

        location1.setMaxSpin(100);
        assertDoesNotThrow(() -> location1.validateState(),
            "Should allow valid configuration");
    }

    @Test
    void testParticipantCapacity() {
        assertTrue(location1.hasAvailableCapacity());

        for (int i = 0; i < location1.getMaxSpin(); i++) {
            Participant participant = Participant.builder()
                .id((long) (i + 1))
                .name("Participant " + i)
                .code("PART_" + i)
                .status(CommonStatus.ACTIVE)
                .build();

            ParticipantEvent pe = ParticipantEvent.builder()
                .id(ParticipantEventKey.builder()
                    .eventLocationKey(location1.getId())
                    .participantId(participant.getId())
                    .build())
                .eventLocation(location1)
                .participant(participant)
                .spinsRemaining(3)
                .status(CommonStatus.ACTIVE)
                .build();

            // Set up bidirectional relationships
            location1.getParticipantEvents().add(pe);
            pe.setEventLocation(location1);
        }

        assertFalse(location1.hasAvailableCapacity());
    }


    // Helper method to create a valid event location for tests
    private EventLocation createValidEventLocation() {
        return EventLocation.builder()
            .maxSpin(100)
            .maxSpin(100)
            .maxSpin(10)
            .dailySpinDistributingRate(0)
            .maxSpin(10)
            .todaySpin(10)
            .status(CommonStatus.ACTIVE)
            .rewardEvents(new HashSet<>())
            .goldenHours(new HashSet<>())
            .participantEvents(new HashSet<>())
            .build();
    }
}
