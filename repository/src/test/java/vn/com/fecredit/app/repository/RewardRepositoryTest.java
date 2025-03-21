package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.repository.config.TestConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RewardRepositoryTest {

    @Autowired
    private RewardRepository rewardRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Region region;
    private Event event;
    private EventLocation location;
    private Reward availableReward;
    private Reward depleteReward;
    private Reward inactiveReward;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM rewards").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        region = createAndSaveRegion();
        event = createAndSaveEvent();
        location = createAndSaveLocation(event, region);
        
        availableReward = createAndSaveReward(
            location, "REWARD1", "Available Reward", 
            BigDecimal.valueOf(100), 10, 0.5, CommonStatus.ACTIVE);
            
        depleteReward = createAndSaveReward(
            location, "REWARD2", "Depleted Reward",
            BigDecimal.valueOf(50), 0, 0.3, CommonStatus.ACTIVE);
            
        inactiveReward = createAndSaveReward(
            location, "REWARD3", "Inactive Reward",
            BigDecimal.valueOf(200), 5, 0.4, CommonStatus.INACTIVE);

        entityManager.flush();
        entityManager.clear();
    }

    private Region createAndSaveRegion() {
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST-REG")
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

    private EventLocation createAndSaveLocation(Event event, Region region) {
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

    private Reward createAndSaveReward(
            EventLocation location, String code, String name,
            BigDecimal value, Integer quantity, Double winProbability,
            CommonStatus status) {
        Reward reward = Reward.builder()
            .eventLocation(location)
            .code(code)
            .name(name)
            .value(value)
            .quantity(quantity)
            .winProbability(winProbability)
            .status(status)
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

    @Test
    void findByCode_ShouldReturnReward_WhenExists() {
        var result = rewardRepository.findByCode("REWARD1");
        
        assertThat(result)
            .isPresent()
            .hasValueSatisfying(reward -> {
                assertThat(reward.getName()).isEqualTo("Available Reward");
                assertThat(reward.getValue()).isEqualByComparingTo(BigDecimal.valueOf(100));
                assertThat(reward.getQuantity()).isEqualTo(10);
            });
    }

    @Test
    void findByEventLocationId_ShouldReturnAllRewards() {
        var rewards = rewardRepository.findByEventLocationId(location.getId());
        assertThat(rewards).hasSize(3);
    }

    @Test
    void findByEventLocationIdAndStatus_ShouldReturnFilteredRewards() {
        var activeRewards = rewardRepository.findByEventLocationIdAndStatus(
            location.getId(), CommonStatus.ACTIVE);
        assertThat(activeRewards).hasSize(2);
        
        var inactiveRewards = rewardRepository.findByEventLocationIdAndStatus(
            location.getId(), CommonStatus.INACTIVE);
        assertThat(inactiveRewards).hasSize(1);
    }

    @Test
    void findAvailableRewardsForLocation_ShouldReturnRewardsWithQuantity() {
        var availableRewards = rewardRepository.findAvailableRewardsForLocation(
            location.getId());
            
        assertThat(availableRewards)
            .hasSize(1)
            .extracting("code")
            .containsExactly("REWARD1");
    }

    @Test
    void isRewardAvailable_ShouldCheckQuantityAndStatus() {
        assertThat(rewardRepository.isRewardAvailable(availableReward.getId())).isTrue();
        assertThat(rewardRepository.isRewardAvailable(depleteReward.getId())).isFalse();
        assertThat(rewardRepository.isRewardAvailable(inactiveReward.getId())).isFalse();
    }

    @Test
    void getRemainingQuantity_ShouldReturnCorrectCount() {
        assertThat(rewardRepository.getRemainingQuantity(availableReward.getId())).isEqualTo(10);
        assertThat(rewardRepository.getRemainingQuantity(depleteReward.getId())).isEqualTo(0);
        assertThat(rewardRepository.getRemainingQuantity(inactiveReward.getId())).isEqualTo(5);
    }
}