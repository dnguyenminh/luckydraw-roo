package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                .value(BigDecimal.valueOf(100))
                .quantity(10)
                .winProbability(0.5)
                .eventLocation(location)
                .status(CommonStatus.ACTIVE)
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
        assertEquals(10, reward.getRemainingQuantity());

        // Add some winning spins
        for (int i = 0; i < 3; i++) {
            SpinHistory spinHistory = SpinHistory.builder()
                    .reward(reward)
                    .spinTime(LocalDateTime.now())
                    .win(true)
                    .status(CommonStatus.ACTIVE)
                    .build();
            reward.getSpinHistories().add(spinHistory);
        }

        assertEquals(7, reward.getRemainingQuantity());
    }

    @Test
    void testAvailability() {
        assertTrue(reward.isAvailable());

        // Set quantity to 0
        reward.setQuantity(0);
        assertFalse(reward.isAvailable());

        // Set back to positive but inactive
        reward.setQuantity(10);
        reward.setStatus(CommonStatus.INACTIVE);
        assertFalse(reward.isAvailable());
    }

    @Test
    void testValidation() {
        assertDoesNotThrow(() -> reward.validateState());

        // Test invalid win probability
        assertThrows(IllegalStateException.class, () -> {
            reward.setWinProbability(-0.1);
            reward.validateState();
        });

        assertThrows(IllegalStateException.class, () -> {
            reward.setWinProbability(1.1);
            reward.validateState();
        });

        // Test invalid quantity
        assertThrows(IllegalStateException.class, () -> {
            reward.setQuantity(-1);
            reward.validateState();
        });

        // Test missing location
        assertThrows(IllegalStateException.class, () -> {
            reward.setEventLocation(null);
            reward.validateState();
        });
    }
}