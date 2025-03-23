package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest; // Add this import
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.repository.config.TestConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest
@SpringBootTest // Add this annotation
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional // Add this annotation to ensure all test methods run in a transaction
class SpinHistoryRepositoryTest {

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Region region;
    private Province province;
    private Event event;
    private EventLocation location;
    private Participant participant;
    private ParticipantEvent participantEvent;
    private Reward reward;
    private GoldenHour goldenHour;
    
    @SuppressWarnings("unused") // Field used for test data setup
    private SpinHistory winSpin;
    
    @SuppressWarnings("unused") // Field used for test data setup
    private SpinHistory loseSpin;
    
    @SuppressWarnings("unused") // Field used for test data setup
    private SpinHistory inactiveSpin;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM spin_histories").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM participant_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM rewards").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM golden_hours").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM participants").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        region = createAndSaveRegion();
        province = createAndSaveProvince();
        event = createAndSaveEvent();
        location = createAndSaveLocation(event);
        participant = createAndSaveParticipant();
        participantEvent = createAndSaveParticipantEvent(participant, event, location);
        reward = createAndSaveReward(location);
        goldenHour = createAndSaveGoldenHour(location);
        
        winSpin = createAndSaveSpin(participantEvent, now.minusMinutes(30),
            reward, goldenHour, true, BigDecimal.valueOf(2.0), CommonStatus.ACTIVE);
            
        loseSpin = createAndSaveSpin(participantEvent, now.minusMinutes(15),
            null, null, false, BigDecimal.ONE, CommonStatus.ACTIVE);
            
        inactiveSpin = createAndSaveSpin(participantEvent, now.minusMinutes(45),
            reward, null, true, BigDecimal.ONE, CommonStatus.INACTIVE);

        entityManager.flush();
        entityManager.clear();
    }

    private Region createAndSaveRegion() {
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST_REG")
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .provinces(new HashSet<>())
            .eventLocations(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(region);
        return region;
    }

    private Province createAndSaveProvince() {
        Province province = Province.builder()
            .name("Test Province")
            .code("TEST_PROV")
            .region(region)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .participants(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        region.getProvinces().add(province);
        entityManager.persist(province);
        return province;
    }

    private Event createAndSaveEvent() {
        Event event = Event.builder()
            .name("Test Event")
            .code("TEST-EVENT")
            .startTime(now.minusHours(1))
            .endTime(now.plusHours(3))
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .locations(new HashSet<>())
            .participantEvents(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(event);
        return event;
    }

    private EventLocation createAndSaveLocation(Event event) {
        EventLocation location = EventLocation.builder()
            .name("Test Location")
            .code("TEST-LOC")
            .maxSpin(100)
            .event(event)
            .region(region)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .participantEvents(new HashSet<>())
            .rewards(new HashSet<>())
            .goldenHours(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        event.getLocations().add(location);
        region.getEventLocations().add(location);
        entityManager.persist(location);
        return location;
    }

    private Participant createAndSaveParticipant() {
        Participant participant = Participant.builder()
            .name("Test Participant")
            .code("TEST-PART")
            .province(province)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .participantEvents(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        province.getParticipants().add(participant);
        entityManager.persist(participant);
        return participant;
    }

    private ParticipantEvent createAndSaveParticipantEvent(
            Participant participant, Event event, EventLocation location) {
        ParticipantEvent participantEvent = ParticipantEvent.builder()
            .participant(participant)
            .event(event)
            .eventLocation(location)
            .spinsRemaining(10)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .spinHistories(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        participant.getParticipantEvents().add(participantEvent);
        event.getParticipantEvents().add(participantEvent);
        location.getParticipantEvents().add(participantEvent);
        entityManager.persist(participantEvent);
        return participantEvent;
    }

    private Reward createAndSaveReward(EventLocation location) {
        Reward reward = Reward.builder()
            .eventLocation(location)
            .code("TEST-REWARD")
            .name("Test Reward")
            .value(BigDecimal.valueOf(100))
            .quantity(10)
            .winProbability(0.5)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .spinHistories(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        location.getRewards().add(reward);
        entityManager.persist(reward);
        return reward;
    }

    private GoldenHour createAndSaveGoldenHour(EventLocation location) {
        GoldenHour goldenHour = GoldenHour.builder()
            .eventLocation(location)
            .startTime(now.minusHours(1))
            .endTime(now.plusHours(1))
            .multiplier(BigDecimal.valueOf(2.0))
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        location.getGoldenHours().add(goldenHour);
        entityManager.persist(goldenHour);
        return goldenHour;
    }

    private SpinHistory createAndSaveSpin(
            ParticipantEvent participantEvent, LocalDateTime spinTime,
            Reward reward, GoldenHour goldenHour, boolean win,
            BigDecimal multiplier, CommonStatus status) {
        SpinHistory spinHistory = SpinHistory.builder()
            .participantEvent(participantEvent)
            .spinTime(spinTime)
            .reward(reward)
            .goldenHour(goldenHour)
            .win(win)
            .multiplier(multiplier)
            .status(status)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        participantEvent.getSpinHistories().add(spinHistory);
        if (reward != null) {
            reward.getSpinHistories().add(spinHistory);
        }
        entityManager.persist(spinHistory);
        return spinHistory;
    }

    @Test
    void findByParticipantEventId_ShouldReturnAllSpins() {
        var spins = spinHistoryRepository.findByParticipantEventId(participantEvent.getId());
        assertThat(spins).hasSize(3);
    }

    @Test
    void findSpinsInTimeRange_ShouldReturnFilteredSpins() {
        var spins = spinHistoryRepository.findSpinsInTimeRange(
            participantEvent.getId(),
            now.minusMinutes(40),
            now.minusMinutes(10));
            
        assertThat(spins)
            .hasSize(2)
            .extracting("win")
            .containsExactly(true, false);
    }

    @Test
    void calculateTotalWinnings_ShouldComputeCorrectAmount() {
        var totalWinnings = spinHistoryRepository.calculateTotalWinnings(participantEvent.getId());
        // 100 * 2.0 multiplier for winning spin
        assertThat(totalWinnings).isEqualByComparingTo(BigDecimal.valueOf(200.0));
    }

    @Test
    void countWinningSpinsAtLocation_ShouldCountCorrectly() {
        var winCount = spinHistoryRepository.countWinningSpinsAtLocation(
            location.getId(),
            now.minusHours(1),
            now);
        assertThat(winCount).isEqualTo(1L);
    }

    @Test
    void findWinningSpinsForEvent_ShouldReturnWinningSpins() {
        var winningSpins = spinHistoryRepository.findWinningSpinsForEvent(event.getId());
        
        assertThat(winningSpins)
            .hasSize(1)
            .extracting("multiplier")
            .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
            .containsExactly(BigDecimal.valueOf(2.0).setScale(2));
    }
}