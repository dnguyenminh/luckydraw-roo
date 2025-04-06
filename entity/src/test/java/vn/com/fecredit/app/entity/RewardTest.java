package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;

class RewardTest {
    private Reward reward;
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
                .name("Test Location")
                .code("TEST_LOC")
                .maxSpin(1000)
                .event(event)
                .region(region)
                .status(CommonStatus.ACTIVE)
                .build();

        reward = Reward.builder()
                .name("Test Reward")
                .code("TEST_REWARD")
                .eventLocation(location)
                .status(CommonStatus.ACTIVE)
                .description("Reward for testing")
                .build();

        // Setup bidirectional relationships
        event.addLocation(location);
        region.addEventLocation(location);
        location.addReward(reward);
    }

    @Test
    void testLocationRelationship() {
        assertEquals(location, reward.getEventLocation());
        assertTrue(location.getRewards().contains(reward));

        EventLocation newLocation = EventLocation.builder()
                .name("New Location")
                .code("NEW_LOC")
                .maxSpin(500)
                .event(event)
                .region(region)
                .status(CommonStatus.ACTIVE)
                .build();

        // Setup bidirectional relationships
        event.addLocation(newLocation);
        region.addEventLocation(newLocation);

        reward.setEventLocation(newLocation);

        assertEquals(newLocation, reward.getEventLocation());
        assertTrue(newLocation.getRewards().contains(reward));
        assertFalse(location.getRewards().contains(reward));
    }

    @Test
    void testRemainingQuantity() {
        EventLocation location = reward.getEventLocation();
        location.setQuantity(10); // Changed from reward.setQuantity(10)

        assertEquals(10, location.getQuantity());

        // Add some winning spins using the correct builder syntax
        for (int i = 0; i < 3; i++) {
            SpinHistory spinHistory = SpinHistory.builder()
                    .reward(reward)
                    .spinTime(LocalDateTime.now())
                    .win(true) // Set directly in builder
                    .status(CommonStatus.ACTIVE)
                    .build();
            reward.getSpinHistories().add(spinHistory);
        }

        assertEquals(7, reward.getRemainingQuantity()); // Updated to use remaining quantity method
    }

    @Test
    void testIsAvailable() {
        EventLocation location = reward.getEventLocation();
        location.setQuantity(5);
        location.setStatus(CommonStatus.ACTIVE);
        reward.setStatus(CommonStatus.ACTIVE);

        assertTrue(reward.isAvailable());

        // Test with quantity = 0
        location.setQuantity(0);
        assertFalse(reward.isAvailable());

        // Set back to positive but inactive
        location.setQuantity(10);
        location.setStatus(CommonStatus.INACTIVE);
        assertFalse(reward.isAvailable());
    }

    @Test
    void testWinProbabilityValidation() {
        EventLocation location = reward.getEventLocation();
        location.setWinProbability(-0.1);
        assertThrows(IllegalStateException.class, () -> location.validateState());

        location.setWinProbability(1.1);
        assertThrows(IllegalStateException.class, () -> location.validateState());
    }

    @Test
    void testQuantityValidation() {
        EventLocation location = reward.getEventLocation();
        location.setQuantity(-5);
        assertThrows(IllegalStateException.class, () -> location.validateState());
    }

    @Test
    void testValidation() {
        assertDoesNotThrow(() -> reward.validateState());

        // Test missing location
        assertThrows(IllegalStateException.class, () -> {
            reward.setEventLocation(null);
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