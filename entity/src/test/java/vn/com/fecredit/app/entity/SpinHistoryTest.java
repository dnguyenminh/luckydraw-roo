package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpinHistoryTest {

    private SpinHistory spinHistory;
    private ParticipantEvent participantEvent;
    private Reward reward;
    private GoldenHour goldenHour;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        Event event = Event.builder()
                .name("Test Event")
                .code("TEST_EVENT")
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(1))
                .status(CommonStatus.ACTIVE)
                .build();

        Region region = Region.builder()
                .name("Test Region")
                .code("TEST_REG")
                .status(CommonStatus.ACTIVE)
                .build();

        EventLocation location = EventLocation.builder()
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

        goldenHour = GoldenHour.builder()
                .startTime(now.minusMinutes(30))
                .endTime(now.plusMinutes(30))
                .multiplier(BigDecimal.valueOf(2))
                .eventLocation(location)
                .status(CommonStatus.ACTIVE)
                .build();

        Participant participant = Participant.builder()
                .name("Test Participant")
                .code("TEST_PART")
                // .phone("1234567890")
                // .email("test@example.com")
                .status(CommonStatus.ACTIVE)
                .build();

        participantEvent = ParticipantEvent.builder()
                .event(event)
                .eventLocation(location)
                .participant(participant)
                .spinsRemaining(5)
                .status(CommonStatus.ACTIVE)
                .build();

        spinHistory = SpinHistory.builder()
                .participantEvent(participantEvent)
                .spinTime(now)
                .status(CommonStatus.ACTIVE)
                .build();
    }

    @Test
    void testCalculateEffectiveValue() {
        // No reward, should be zero
        assertThat(spinHistory.calculateEffectiveValue()).isEqualByComparingTo(BigDecimal.ZERO);
        
        // With reward but not a win, should be zero
        spinHistory.setReward(reward);
        spinHistory.setWin(false);
        assertThat(spinHistory.calculateEffectiveValue()).isEqualByComparingTo(BigDecimal.ZERO);
        
        // With reward and win, normal multiplier
        spinHistory.setWin(true);
        assertThat(spinHistory.calculateEffectiveValue()).isEqualByComparingTo(BigDecimal.valueOf(100));
        
        // With golden hour multiplier
        spinHistory.setMultiplier(BigDecimal.valueOf(2.5));
        assertThat(spinHistory.calculateEffectiveValue()).isEqualByComparingTo(BigDecimal.valueOf(250));
    }

    @Test
    void testDefaultMultiplier() {
        SpinHistory newHistory = SpinHistory.builder()
                .participantEvent(participantEvent)
                .spinTime(LocalDateTime.now())
                .status(CommonStatus.ACTIVE)
                .build();
        
        assertThat(newHistory.getMultiplier()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void testWinFlag() {
        assertFalse(spinHistory.isWin());
        
        spinHistory.setWin(true);
        assertTrue(spinHistory.isWin());
    }

    @Test
    void testValidation() {
        assertDoesNotThrow(() -> spinHistory.validateState());

        // Test missing spin time
        assertThrows(IllegalStateException.class, () -> {
            spinHistory.setSpinTime(null);
            spinHistory.validateState();
        });

        // Test missing participant event
        assertThrows(IllegalStateException.class, () -> {
            spinHistory.setParticipantEvent(null);
            spinHistory.validateState();
        });

        // Test win without reward
        assertThrows(IllegalStateException.class, () -> {
            spinHistory.setWin(true);
            spinHistory.setReward(null);
            spinHistory.validateState();
        });

        // Test invalid multiplier
        assertThrows(IllegalStateException.class, () -> {
            spinHistory.setMultiplier(BigDecimal.ZERO);
            spinHistory.validateState();
        });
    }
}
