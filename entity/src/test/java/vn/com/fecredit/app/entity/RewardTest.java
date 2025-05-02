package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;

class RewardTest {
    private Reward reward;
    private RewardEvent rewardEvent;
    private EventLocation location;
    private Region region;
    private Event event;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        event = Event.builder()
            .name("Test Event")
            .code("TEST_EVENT")
            .startTime(now.minusHours(1))
            .endTime(now.plusHours(1))
            .status(CommonStatus.ACTIVE)
            .build();

        region = Region.builder()
            .name("Test Region")
            .code("TEST_REG")
            .status(CommonStatus.ACTIVE)
            .build();

        location = EventLocation.builder()
            .maxSpin(1000)
            .event(event)
            .region(region)
            .status(CommonStatus.ACTIVE)
            .build();

        reward = Reward.builder()
            .name("Test Reward")
            .code("TEST_REWARD")
            .status(CommonStatus.ACTIVE)
            .description("Reward for testing")
            .build();
        rewardEvent = RewardEvent.builder()
            .reward(reward)
            .eventLocation(location)
            .build();
        // Setup bidirectional relationships
        reward.getRewardEvents().add(rewardEvent);
        event.addLocation(location);
        region.addEventLocation(location);
        location.addRewardEvent(rewardEvent);
    }

    @Test
    void testLocationRelationship() {
        assertTrue(reward.getRewardEvents().stream().filter(re -> re.getEventLocation() == location).count() > 0);
        assertTrue(location.getRewardEvents().contains(rewardEvent));

        EventLocation newLocation = EventLocation.builder()
            .maxSpin(500)
            .event(event)
            .region(region)
            .status(CommonStatus.ACTIVE)
            .build();
        RewardEvent newRewardEvent = RewardEvent.builder()
            .reward(reward)
            .eventLocation(newLocation)
            .build();
        newLocation.getRewardEvents().add(newRewardEvent);
        // Setup bidirectional relationships
        event.addLocation(newLocation);
        region.addEventLocation(newLocation);

        reward.getRewardEvents().add(newRewardEvent);

        assertEquals(1, reward.getRewardEvents().stream().map(RewardEvent::getEventLocation)
            .filter(location -> location == newLocation).count());
        assertTrue(newLocation.getRewardEvents().contains(newRewardEvent));
    }

    // @Test
    // void testIsAvailable() {
    // EventLocation location = reward.getEventLocation();
    // location.setStatus(CommonStatus.ACTIVE);
    // reward.setStatus(CommonStatus.ACTIVE);
    //
    // assertTrue(reward.isAvailable());
    //
    // // Test with quantity = 0
    // assertFalse(reward.isAvailable());
    //
    // // Set back to positive but inactive
    // location.setStatus(CommonStatus.INACTIVE);
    // assertFalse(reward.isAvailable());
    // }

    @Test
    void testValidation() {
        assertDoesNotThrow(() -> reward.validateState());

        // Test missing location
        assertDoesNotThrow(() -> {
            reward.setRewardEvents(null);
            reward.validateState();
        });
    }

    @Test
    void testRewardInitialization() {
        assertNotNull(reward);
        assertEquals("Test Reward", reward.getName());
        assertTrue(reward.getStatus().isActive());
    }

}
