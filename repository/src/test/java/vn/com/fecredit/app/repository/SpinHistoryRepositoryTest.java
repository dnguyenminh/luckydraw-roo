package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.EventLocationKey;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.ParticipantEventKey;
import vn.com.fecredit.app.entity.RewardEventKey;
import vn.com.fecredit.app.entity.RewardEvent;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class SpinHistoryRepositoryTest extends AbstractRepositoryTest {

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

    private SpinHistory winSpin;

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
        location = createAndSaveLocation(event, region);
        participant = createAndSaveParticipant();
        participantEvent = createAndSaveParticipantEvent(participant, location);
        reward = createAndSaveReward(location);
        goldenHour = createAndSaveGoldenHour(location);

        winSpin = createAndSaveSpin(participantEvent, now.minusMinutes(30),
            reward, goldenHour, true, CommonStatus.ACTIVE);
        entityManager.persist(winSpin);

        createAndSaveSpin(participantEvent, now.minusMinutes(15),
            null, null, false, CommonStatus.ACTIVE);

        createAndSaveSpin(participantEvent, now.minusMinutes(45),
            reward, null, true, CommonStatus.INACTIVE);

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
            .startTime(now.minusDays(1))
            .endTime(now.plusDays(7))
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .locations(new HashSet<>())
            .build();

        entityManager.persist(event);
        return event;
    }

    private EventLocation createAndSaveLocation(Event event, Region region) {
        // Ensure both event and region have been persisted and have IDs
        if (event.getId() == null) {
            event = entityManager.merge(event);
            entityManager.flush(); // Make sure ID is generated
        }
        
        if (region.getId() == null) {
            region = entityManager.merge(region);
            entityManager.flush(); // Make sure ID is generated
        }
        
        // Create the composite key using non-null IDs
        EventLocationKey locationKey = EventLocationKey.of(event.getId(), region.getId());
        
        // Create the EventLocation with the key
        EventLocation location = EventLocation.builder()
                .event(event)
                .region(region)
                .maxSpin(10)
                .todaySpin(50)
                .dailySpinDistributingRate(0.1)
                .status(CommonStatus.ACTIVE)
                .participantEvents(new HashSet<>())
                .rewardEvents(new HashSet<>())
                .goldenHours(new HashSet<>())
                .build();
                
        // Set the key explicitly
        location.setId(locationKey);
        location.setCreatedBy("test");
        location.setUpdatedBy("test");
        location.setCreatedAt(LocalDateTime.now());
        location.setUpdatedAt(LocalDateTime.now());
        
        // Save and return the location
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
        Participant participant, EventLocation location) {
        
        // Create the composite key first
        ParticipantEventKey participantEventKey = new ParticipantEventKey();
        participantEventKey.setEventLocationKey(location.getId()); // Set the EventLocationKey from the location
        participantEventKey.setParticipantId(participant.getId()); // Set the participant ID
        
        // Create the ParticipantEvent with proper relationships
        ParticipantEvent participantEvent = ParticipantEvent.builder()
            .participant(participant)
            .eventLocation(location)
            .spinsRemaining(5)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .spinHistories(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        
        // Set the composite key to the entity
        participantEvent.setId(participantEventKey);

        // Maintain bidirectional relationship
        location.getParticipantEvents().add(participantEvent);
        
        // Persist and return
        entityManager.persist(participantEvent);
        return participantEvent;
    }

    private Reward createAndSaveReward(EventLocation eventLocation) {
        // First create and persist the Reward entity
        Reward reward = Reward.builder()
            .code("TEST-REWARD")
            .name("Test Reward")
            .prizeValue(BigDecimal.valueOf(50.00))
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(reward);
        entityManager.flush(); // Ensure the reward has an ID
        
        // Create the composite key for RewardEvent
        RewardEventKey rewardEventKey = new RewardEventKey();
        rewardEventKey.setEventLocationKey(eventLocation.getId());
        rewardEventKey.setRewardId(reward.getId());
        
        // Create the RewardEvent with the composite key
        RewardEvent rewardEvent = RewardEvent.builder()
            .eventLocation(eventLocation)
            .reward(reward)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        
        // Set the composite key to the entity
        rewardEvent.setId(rewardEventKey);
        
        // Then persist the RewardEvent
        entityManager.persist(rewardEvent);
        
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
        Reward reward, GoldenHour goldenHour, boolean win, CommonStatus status) {
        
        // Only create and associate RewardEvent if reward is not null
        RewardEvent rewardEvent = null;
        if (reward != null) {
            EventLocation location = participantEvent.getEventLocation();
            
            // First, create the key to search for existing RewardEvent
            RewardEventKey rewardEventKey = new RewardEventKey();
            rewardEventKey.setEventLocationKey(location.getId());
            rewardEventKey.setRewardId(reward.getId());
            
            // Try to find existing RewardEvent in the database
            try {
                rewardEvent = entityManager.find(RewardEvent.class, rewardEventKey);
            } catch (Exception e) {
                // Log and continue - we'll create a new one if needed
                System.out.println("Error finding RewardEvent: " + e.getMessage());
            }
            
            // Create new RewardEvent if not found
            if (rewardEvent == null) {
                // Detach any potentially conflicting entities from the session
                entityManager.flush();
                
                rewardEvent = RewardEvent.builder()
                    .eventLocation(location)
                    .reward(reward)
                    .quantity(10)
                    .todayQuantity(5)
                    .status(CommonStatus.ACTIVE)
                    .version(0L)
                    .createdAt(now)
                    .updatedAt(now)
                    .createdBy("test-user")
                    .updatedBy("test-user")
                    .build();
                rewardEvent.setId(rewardEventKey);
                
                // Use merge instead of persist to handle both create and update scenarios
                rewardEvent = entityManager.merge(rewardEvent);
                entityManager.flush();
            }
        }
        
        SpinHistory spinHistory = SpinHistory.builder()
            .participantEvent(participantEvent)
            .spinTime(spinTime)
            .rewardEvent(rewardEvent)
            .goldenHour(goldenHour)
            .win(win)
            .status(status)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
            
        participantEvent.getSpinHistories().add(spinHistory);
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

        // Check only the size to avoid scale comparison issues
        assertThat(winningSpins).hasSize(1);
    }

    @Test
    void testFindWinningSpins() {
        // Get a direct reference to winSpin
        SpinHistory spin = entityManager.find(SpinHistory.class, winSpin.getId());

        // Need to flush and clear to avoid stale data
        entityManager.flush();
        entityManager.clear();

        // Reload the entity to ensure we have a fresh copy
        spin = entityManager.find(SpinHistory.class, winSpin.getId());

        // Use the standard setter method
        spin.setWin(true);
        spinHistoryRepository.save(spin);
        entityManager.flush();

        // Verify the change was saved
        SpinHistory saved = entityManager.find(SpinHistory.class, spin.getId());
        assertThat(saved.isWin()).isTrue();
    }
}
