package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.RewardEvent;
import vn.com.fecredit.app.entity.RewardEventKey;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class RewardRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RewardRepository rewardRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final LocalDateTime now = LocalDateTime.now();
    private Region region;
    private Event event;
    private EventLocation eventLocation;
    private Reward activeReward, inactiveReward;
    
    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }
    
    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM reward_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM rewards").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
    }
    
    private void createTestData() {
        // Create region
        region = Region.builder()
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
        
        // Create event
        event = Event.builder()
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
        
        // Create event location with composite key
        EventLocationKey locationKey = EventLocationKey.of(event.getId(), region.getId());
        eventLocation = EventLocation.builder()
            .event(event)
            .region(region)
            .maxSpin(10)
            .todaySpin(50)
            .dailySpinDistributingRate(0.1)
            .status(CommonStatus.ACTIVE)
            .build();
        eventLocation.setId(locationKey);
        eventLocation.setCreatedBy("test-user");
        eventLocation.setUpdatedBy("test-user");
        eventLocation.setCreatedAt(now);
        eventLocation.setUpdatedAt(now);
        entityManager.persist(eventLocation);
        
        // Create active reward
        activeReward = Reward.builder()
            .name("Active Reward")
            .code("ACTIVE-RWD")
            .prizeValue(BigDecimal.valueOf(100.00))
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(activeReward);
        
        // Create RewardEvent for active reward
        RewardEventKey activeRewardEventKey = new RewardEventKey();
        activeRewardEventKey.setEventLocationKey(eventLocation.getId());
        activeRewardEventKey.setRewardId(activeReward.getId());
        
        RewardEvent activeRewardEvent = RewardEvent.builder()
            .eventLocation(eventLocation)
            .reward(activeReward)
            .quantity(10)
            .todayQuantity(5)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        activeRewardEvent.setId(activeRewardEventKey);
        entityManager.persist(activeRewardEvent);
        
        // Create inactive reward
        inactiveReward = Reward.builder()
            .name("Inactive Reward")
            .code("INACTIVE-RWD")
            .prizeValue(BigDecimal.valueOf(50.00))
            .status(CommonStatus.INACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        entityManager.persist(inactiveReward);
        
        // Create RewardEvent for inactive reward
        RewardEventKey inactiveRewardEventKey = new RewardEventKey();
        inactiveRewardEventKey.setEventLocationKey(eventLocation.getId());
        inactiveRewardEventKey.setRewardId(inactiveReward.getId());
        
        RewardEvent inactiveRewardEvent = RewardEvent.builder()
            .eventLocation(eventLocation)
            .reward(inactiveReward)
            .quantity(5)
            .todayQuantity(2)
            .status(CommonStatus.INACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
        inactiveRewardEvent.setId(inactiveRewardEventKey);
        entityManager.persist(inactiveRewardEvent);
        
        entityManager.flush();
        entityManager.clear();
    }
    
    @Test
    void testFindByStatus() {
        List<Reward> activeRewards = rewardRepository.findByStatus(CommonStatus.ACTIVE);
        List<Reward> inactiveRewards = rewardRepository.findByStatus(CommonStatus.INACTIVE);
        
        assertThat(activeRewards).isNotEmpty();
        assertThat(activeRewards.size()).isEqualTo(1);
        assertThat(activeRewards.get(0).getCode()).isEqualTo("ACTIVE-RWD");
        
        assertThat(inactiveRewards).isNotEmpty();
        assertThat(inactiveRewards.size()).isEqualTo(1);
        assertThat(inactiveRewards.get(0).getCode()).isEqualTo("INACTIVE-RWD");
    }
    
    @Test
    void testFindByCode() {
        Reward reward = rewardRepository.findByCode("ACTIVE-RWD").orElse(null);
        assertThat(reward).isNotNull();
        assertThat(reward.getName()).isEqualTo("Active Reward");
        assertThat(reward.getStatus()).isEqualTo(CommonStatus.ACTIVE);
    }
    
    @Test
    void testExistsByCode() {
        boolean exists = rewardRepository.existsByCode("ACTIVE-RWD");
        boolean notExists = rewardRepository.existsByCode("NONEXISTENT");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
