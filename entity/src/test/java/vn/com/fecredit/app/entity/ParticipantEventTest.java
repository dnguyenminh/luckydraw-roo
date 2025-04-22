package vn.com.fecredit.app.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;

class ParticipantEventTest {

    private ParticipantEvent participantEvent;
    private TestEvent event;
    private EventLocation eventLocation;
    private Participant participant;
    private Region region;
    private Province province;
    private LocalDateTime currentTime;

    // Test Event subclass with controlled current time
    private static class TestEvent extends Event {
        private final LocalDateTime fixedTime;

        private TestEvent(LocalDateTime fixedTime) {
            this.fixedTime = fixedTime;
            setStatus(CommonStatus.ACTIVE);
        }

        @Override
        public LocalDateTime getCurrentServerTime() {
            return fixedTime;
        }
    }

    @BeforeEach
    void setUp() {
        currentTime = LocalDateTime.now().withNano(0); // Remove nanos for consistent comparison

        region = Region.builder()
                .name("Test Region")
                .code("TEST_REG")
                .status(CommonStatus.ACTIVE)
                .build();

        province = Province.builder()
                .name("Test Province")
                .code("TEST_PROV")
                .status(CommonStatus.ACTIVE)
                .region(region)
                .build();

        // Create test event with controlled time
        event = new TestEvent(currentTime);
        event.setName("Test Event");
        event.setCode("TEST_EVENT");
        event.setStartTime(currentTime.minusDays(1));
        event.setEndTime(currentTime.plusDays(1));

        eventLocation = EventLocation.builder()
                .status(CommonStatus.ACTIVE)
                .maxSpin(1000)
                .region(region)
                .event(event)
                .build();

        participant = Participant.builder()
                .name("Test Participant")
                .code("TEST_PART")
                .status(CommonStatus.ACTIVE)
                .province(province)
                .build();

        participantEvent = ParticipantEvent.builder()
                .eventLocation(eventLocation)
                .participant(participant)
                .spinsRemaining(10)
                .status(CommonStatus.ACTIVE)
                .build();

        // Set up bidirectional relationships
        region.addEventLocation(eventLocation);
        region.addProvince(province);
        event.addLocation(eventLocation);
        participantEvent.setEventLocation(eventLocation);
        eventLocation.getParticipantEvents().add(participantEvent);
    }

    @Test
    void testCanSpin() {
        assertTrue(participantEvent.canSpin());

        participantEvent.setSpinsRemaining(0);
        assertFalse(participantEvent.canSpin(), "Should not be able to spin with no spins remaining");

        participantEvent.setSpinsRemaining(10);
        event.setStatus(CommonStatus.INACTIVE);
        assertFalse(participantEvent.canSpin(), "Should not be able to spin when event is inactive");

        event.setStatus(CommonStatus.ACTIVE);
        eventLocation.setStatus(CommonStatus.INACTIVE);
        assertFalse(participantEvent.canSpin(), "Should not be able to spin when location is inactive");

        eventLocation.setStatus(CommonStatus.ACTIVE);
        participantEvent.setStatus(CommonStatus.INACTIVE);
        assertFalse(participantEvent.canSpin(), "Should not be able to spin when participation is inactive");
    }

    @Test
    void testMaxSpinLimit() {

        // Initial state
        assertThat(participantEvent.canSpin()).as("Initial can spin").isTrue();
        assertThat(participantEvent.getSpinHistories()).as("Initial histories").isEmpty();

        int remainSpinOfParticipant = participantEvent.getSpinsRemaining();
        eventLocation.setMaxSpin(remainSpinOfParticipant);

        // Perform spins up to the limit
        IntStream.range(0, remainSpinOfParticipant).forEach(i -> {
            assertThat(participantEvent.canSpin())
                    .as("Can spin before attempt %d", i)
                    .isTrue();

            SpinHistory spin = participantEvent.spin();

            assertThat(spin).as("Spin %d result", i).isNotNull();
            assertThat(spin.getParticipantEvent()).as("Spin %d relationship", i).isEqualTo(participantEvent);
            // assertThat(spin.getSpinTime()).as("Spin %d time", i).isEqualTo(currentTime);
            LocalDateTime now = LocalDateTime.now();
            assertThat(Math.abs(spin.getSpinTime().until(now, java.time.temporal.ChronoUnit.SECONDS)))
                    .as("Spin %d time should be within 10 seconds of current time", i)
                    .isLessThanOrEqualTo(10);
        });

        assertThat(participantEvent.canSpin())
                .as("Can spin after maximum spins")
                .isFalse();

        // Verify exception when trying to spin beyond limit
        assertThatThrownBy(() -> participantEvent.spin())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot spin");
    }

    @Test
    void testSpin() {
        SpinHistory spinHistory = participantEvent.spin();

        assertThat(participantEvent.getSpinsRemaining()).isEqualTo(9);
        assertThat(spinHistory.getParticipantEvent()).isEqualTo(participantEvent);
        assertThat(participantEvent.getSpinHistories()).contains(spinHistory);

        // Check that spinTime is within 10 seconds of current time

        LocalDateTime now = LocalDateTime.now();
        assertThat(Math.abs(spinHistory.getSpinTime().until(now, java.time.temporal.ChronoUnit.SECONDS)))
                .as("Spin time should be within 10 seconds of current time")
                .isLessThanOrEqualTo(10);
        participantEvent.setSpinsRemaining(0);
        assertThatThrownBy(() -> participantEvent.spin())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot spin");
    }

    @Test
    void testTotalWinnings() {
        Reward reward = Reward.builder()
                .name("Test Reward")
                .status(CommonStatus.ACTIVE)
                .build();

        RewardEvent rewardEvent = RewardEvent.builder()
                .eventLocation(eventLocation)
                .reward(reward)
                .quantity(10)
                .todayQuantity(10)
                .build();
        ;
        SpinHistory win = SpinHistory.builder()
                .participantEvent(participantEvent)
                .rewardEvent(rewardEvent)
                .win(true)
                .spinTime(currentTime)
                .status(CommonStatus.ACTIVE)
                .build();

        participantEvent.addSpinHistory(win);

        assertThat(participantEvent.getTotalWinnings())
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void testValidation() {
        ParticipantEvent invalid = ParticipantEvent.builder().build();
        assertThrows(IllegalStateException.class, () -> invalid.validateState());

        invalid.setEventLocation(eventLocation);
        assertThrows(IllegalStateException.class, () -> invalid.validateState());

        invalid.setParticipant(participant);
        invalid.setSpinsRemaining(-1);
        assertThrows(IllegalStateException.class, () -> invalid.validateState());

        invalid.setSpinsRemaining(10);
        assertDoesNotThrow(() -> invalid.validateState());
    }

    @Test
    void testSpinWithDailyReset() {
        // Create spins for yesterday (should not count towards today's maximum)
        LocalDateTime yesterday = currentTime.minusDays(1);
        for (int i = 0; i < eventLocation.getMaxSpin(); i++) {
            SpinHistory spin = SpinHistory.builder()
                    .participantEvent(participantEvent)
                    .spinTime(yesterday)
                    .status(CommonStatus.ACTIVE)
                    .build();
            participantEvent.addSpinHistory(spin);
        }

        assertThat(participantEvent.canSpin())
                .as("Can spin after yesterday's spins")
                .isTrue();

        // Should be able to spin today
        assertDoesNotThrow(() -> participantEvent.spin(),
                "Should allow spins after daily reset");
    }

    @Test
    void testParticipantEventCreation() {
        ParticipantEvent pe = ParticipantEvent.builder()
                .spinsRemaining(5)
                .build();

        assertEquals(5, pe.getSpinsRemaining());
    }

    @Test
    void testSpinWithWin() {
        // Create a spin history first with correct builder syntax
        SpinHistory spinHistory = SpinHistory.builder()
                .participantEvent(participantEvent)
                .spinTime(currentTime)
                .win(false) // Set initial value
                .status(CommonStatus.ACTIVE)
                .build();

        participantEvent.addSpinHistory(spinHistory);

        // Now call setWin method on the concrete instance
        spinHistory.setWin(true);

        // Verify the spin is marked as a win
        assertTrue(spinHistory.isWin());
    }
}
