package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.EventLocationKey;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.RewardEvent;
import vn.com.fecredit.app.entity.RewardEventKey;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Transactional
//@Sql(scripts = {"classpath:/data-h2.sql"})
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
        createTestData();
    }

    @Test
    void testFindById() {
        // Ensure we have a clean persistence context for the test
//        entityManager.clear();

        // Test finding a spin history by ID
        Optional<SpinHistory> found = spinHistoryRepository.findById(winSpin.getId());

        // Assert that the spin history was found
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(winSpin.getId());
        assertThat(found.get().isWin()).isTrue();
    }

    @Test
    void testFindByParticipantEventId() {
        // Ensure we have a clean persistence context for the test
//        entityManager.clear();

        // Load the participantEvent from the database to ensure it's managed
        ParticipantEvent managedParticipantEvent = entityManager.find(ParticipantEvent.class, participantEvent.getId());

        // Find all spin histories for this participant event
        List<SpinHistory> histories = spinHistoryRepository.findByParticipantEventId(managedParticipantEvent);

        // Should find 3 spin histories (2 active, 1 inactive)
        assertThat(histories).hasSize(3);
        assertThat(histories.stream().filter(h -> h.getStatus() == CommonStatus.ACTIVE).count()).isEqualTo(2);
    }

    @Test
    void testFindByStatus() {
        // Create test data
        LocalDateTime now = LocalDateTime.now();

//        Province managedProvince = entityManager.contains(province) ? province : entityManager.merge(province);
//        Region managedRegion = entityManager.contains(region) ? region : entityManager.merge(region);
//        Event managedEvent = entityManager.contains(event) ? event : entityManager.merge(event);
//        EventLocation managedLocation = entityManager.contains(location) ? location : entityManager.merge(location);
//        ParticipantEvent managedParticipantEvent = entityManager.contains(participantEvent) ? participantEvent : entityManager.merge(participantEvent);

        // Add spin history for testing
        SpinHistory activeSpin = SpinHistory.builder()
            .participantEvent(participantEvent)
            .spinTime(now)
            .status(CommonStatus.ACTIVE)
//            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(activeSpin);

        SpinHistory inactiveSpin = SpinHistory.builder()
            .participantEvent(participantEvent)
            .spinTime(now)
            .status(CommonStatus.INACTIVE)
//            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        participantEvent.addSpinHistory(inactiveSpin);
        entityManager.persist(inactiveSpin);

        entityManager.flush();
        entityManager.clear();

        // Test the repository method
        List<SpinHistory> activeSpins = spinHistoryRepository.findByStatus(CommonStatus.ACTIVE);

        // Assertions
        assertThat(activeSpins).hasSize(3);
        assertThat(activeSpins.get(0).getStatus()).isEqualTo(CommonStatus.ACTIVE);
    }

    @Test
    void testFindSpinsInTimeRange() {
        // Ensure we have a clean persistence context for the test
//        entityManager.clear();

        // Load the participantEvent from the database
        ParticipantEvent managedParticipantEvent = entityManager.find(ParticipantEvent.class, participantEvent.getId());

        // Find spins in a time range for this participant event
        List<SpinHistory> recentSpins = spinHistoryRepository.findSpinsInTimeRange(
            managedParticipantEvent,
            now.minusMinutes(40),
            now.minusMinutes(10));

        // Should find 2 spins in this time range (1 winning at -30, 1 losing at -15)
        assertThat(recentSpins).hasSize(2);
    }

    @Test
    void testCountWinningSpinsAtLocation() {
        // Ensure we have a clean persistence context for the test
//        entityManager.clear();

        // Count winning spins at this location
        Long winCount = spinHistoryRepository.countWinningSpinsAtLocation(
            location.getId(),
            now.minusDays(1),
            now,
            CommonStatus.ACTIVE);

        // Should find 1 active winning spin
        assertThat(winCount).isEqualTo(1);
    }

    @Test
    void testFindWinningSpinsForEvent() {
        // Ensure we have a clean persistence context for the test
//        entityManager.clear();

        // Find winning spins for this event
        List<SpinHistory> winningSpins = spinHistoryRepository.findWinningSpinsForEvent(
            event.getId(),
            CommonStatus.ACTIVE);

        // Should find 1 active winning spin
        assertThat(winningSpins).hasSize(1);
        assertThat(winningSpins.get(0).getId()).isEqualTo(winSpin.getId());
    }

    private void createTestData() {
        region = createAndSaveRegion();
        entityManager.flush();

        province = createAndSaveProvince();
        region.addProvince(province);
        province.addRegion(region);
        entityManager.persist(province);
        entityManager.flush();

        event = createAndSaveEvent();
        entityManager.flush();

        location = createAndSaveLocation(event, region);
        entityManager.flush();

        participant = createAndSaveParticipant();
        entityManager.flush();

        participantEvent = createAndSaveParticipantEvent(participant, location);
        entityManager.flush();

        reward = createAndSaveReward(location);
        entityManager.flush();

        goldenHour = createAndSaveGoldenHour(location);
        entityManager.flush();

        // Clear persistence context to avoid detached entities
        entityManager.clear();

        // Re-fetch managed entities
        participantEvent = entityManager.find(ParticipantEvent.class, participantEvent.getId());
        reward = entityManager.find(Reward.class, reward.getId());
        goldenHour = entityManager.find(GoldenHour.class, goldenHour.getId());

        winSpin = createAndSaveSpin(participantEvent, now.minusMinutes(30), reward, goldenHour, true, CommonStatus.ACTIVE);
        createAndSaveSpin(participantEvent, now.minusMinutes(15), null, null, false, CommonStatus.ACTIVE);
        createAndSaveSpin(participantEvent, now.minusMinutes(45), reward, null, true, CommonStatus.INACTIVE);
    }

    private Region createAndSaveRegion() {
        // Generate a unique code and name using timestamp AND UUID to guarantee uniqueness
        String uniqueValue = System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Region region = Region.builder()
            .id(null)
            .name("Test Region " + uniqueValue)
            .code("TEST_REG_" + uniqueValue)
            .status(CommonStatus.ACTIVE)
//            .version(0L)
            .provinces(new HashSet<>())
            .eventLocations(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("system")
            .updatedBy("system")
            .build();

        entityManager.persist(region);
        return region;
    }

    private Province createAndSaveProvince() {
        // Generate a unique code using current timestamp to avoid unique constraint
        // violations
        String uniqueCode = "TEST_PROV_" + System.currentTimeMillis();

        Province province = Province.builder()
            .name("Test Province")
            .code(uniqueCode)
//                .regions(new HashSet<>() {
//                    {
//                        add(region);
//                    }
//                })
            .status(CommonStatus.ACTIVE)
//            .version(0L)
            .participants(new HashSet<>())
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
//        region.getProvinces().add(province);
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
//            .version(0L)
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
            // If the event has an ID but might be detached, re-attach it
            event = entityManager.find(Event.class, event.getId());
        }

        if (region.getId() == null) {
            region = entityManager.merge(region);
        } else {
            // If the region has an ID but might be detached, re-attach it
            region = entityManager.find(Region.class, region.getId());
        }

        // Create the composite key using non-null IDs
        EventLocationKey locationKey = EventLocationKey.of(event.getId(), region.getId());

        // Check if the entity already exists
        EventLocation existingLocation = entityManager.find(EventLocation.class, locationKey);
        if (existingLocation != null) {
            return existingLocation;
        }

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
//            .version(0L)
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

    private ParticipantEvent createAndSaveParticipantEvent(Participant participant, EventLocation location) {
        // Ensure participant and location are managed entities
        Participant managedParticipant = entityManager.contains(participant)
            ? participant
            : entityManager.merge(participant);

        EventLocation managedLocation = entityManager.contains(location)
            ? location
            : entityManager.merge(location);

        // Create a new ParticipantEvent instance
        ParticipantEvent participantEvent = ParticipantEvent.builder()
            .participant(managedParticipant)
            .eventLocation(managedLocation)
            .spinsRemaining(5)
            .status(CommonStatus.ACTIVE)
//            .version(0L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .createdBy("test-user")
            .updatedBy("test-user")
            .spinHistories(new ArrayList<>())
            .build();

        // Set up bidirectional relationships
        managedParticipant.addParticipantEvent(participantEvent);
        managedLocation.addParticipantEvent(participantEvent);

        // Persist the new entity
        entityManager.persist(participantEvent);
        entityManager.flush();

        return participantEvent;
    }

    private Reward createAndSaveReward(EventLocation eventLocation) {
        // First create and persist the Reward entity
        Reward reward = Reward.builder()
            .code("TEST-REWARD")
            .name("Test Reward")
            .prizeValue(BigDecimal.valueOf(50.00))
            .status(CommonStatus.ACTIVE)
//            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(reward);

        // Create the RewardEvent using JPA instead of native SQL
        RewardEventKey rewardEventKey = new RewardEventKey();
        rewardEventKey.setEventLocationKey(eventLocation.getId());
        rewardEventKey.setRewardId(reward.getId());

        RewardEvent rewardEvent = new RewardEvent();
        rewardEvent.setId(rewardEventKey);
        rewardEvent.setEventLocation(eventLocation);
        rewardEvent.setReward(reward);
        rewardEvent.setQuantity(10);
        rewardEvent.setTodayQuantity(5);
        rewardEvent.setStatus(CommonStatus.ACTIVE);
//        rewardEvent.setVersion(0L);
        rewardEvent.setCreatedAt(now);
        rewardEvent.setUpdatedAt(now);
        rewardEvent.setCreatedBy("test-user");
        rewardEvent.setUpdatedBy("test-user");

        // Persist the reward event
        entityManager.persist(rewardEvent);
//        entityManager.clear();

        // Reload the reward to ensure we have a fresh copy
        reward = entityManager.find(Reward.class, reward.getId());
        return reward;
    }

    private GoldenHour createAndSaveGoldenHour(EventLocation location) {
        GoldenHour goldenHour = GoldenHour.builder()
            .eventLocation(location)
            .startTime(now.minusHours(1))
            .endTime(now.plusHours(1))
            .multiplier(BigDecimal.valueOf(2.0))
            .status(CommonStatus.ACTIVE)
//            .version(0L)
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
        // Ensure participantEvent is managed
        ParticipantEvent managedParticipantEvent = entityManager.contains(participantEvent)
            ? participantEvent
            : entityManager.merge(participantEvent);

        // Handle reward and reward event for winning spins
        RewardEvent managedRewardEvent = null;
        if (win && reward != null) {
            Reward managedReward = entityManager.contains(reward)
                ? reward
                : entityManager.merge(reward);
            RewardEventKey rewardEventKey = new RewardEventKey();
            rewardEventKey.setEventLocationKey(managedParticipantEvent.getEventLocation().getId());
            rewardEventKey.setRewardId(managedReward.getId());
            managedRewardEvent = entityManager.find(RewardEvent.class, rewardEventKey);
        }

        // Handle golden hour
        GoldenHour managedGoldenHour = goldenHour != null && entityManager.contains(goldenHour)
            ? goldenHour
            : (goldenHour != null ? entityManager.merge(goldenHour) : null);

        // Create and persist spin history
        SpinHistory spinHistory = SpinHistory.builder()
            .participantEvent(managedParticipantEvent)
            .spinTime(spinTime)
            .rewardEvent(managedRewardEvent)
            .goldenHour(managedGoldenHour)
            .win(win)
            .status(status)
//            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();

        entityManager.persist(spinHistory);
        managedParticipantEvent.getSpinHistories().add(spinHistory);
        entityManager.flush();

        return spinHistory;
    }
}
