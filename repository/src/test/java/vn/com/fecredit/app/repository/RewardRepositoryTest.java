package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class RewardRepositoryTest extends AbstractRepositoryTest {

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
    private Reward reward;

    @BeforeEach
    void setUp() {
        System.out.println("Executing test setup: Checking if SQL scripts are loaded correctly.");
        cleanDatabase();
        createTestData();
        reward = Reward.builder()
                .name("Repo Test Reward")
                .code("REPO_TEST_REWARD") // Add required code property

                .quantity(5) // Add required quantity
                .winProbability(0.3) // Add required probability
                .eventLocation(location) // Add required event location
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .spinHistories(new HashSet<>())
                .build();
        rewardRepository.save(reward);
    }

    private void cleanDatabase() {
        try {
            // Try to execute the delete statements, but don't fail if tables don't exist
            // yet
            try {
                entityManager.createNativeQuery("DELETE FROM spin_history").executeUpdate();
            } catch (Exception e) {
                // Table might not exist yet, that's ok
            }
            // First try to delete rewards since they depend on other tables
            try {
                entityManager.createNativeQuery("DELETE FROM rewards").executeUpdate();
            } catch (Exception e) {
                System.out.println("Warning: Could not delete from rewards table: " + e.getMessage());
            }

            // Try event_locations or event_location (singular form)
            try {
                entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
            } catch (Exception e) {
                System.out.println("Warning: Could not delete from event_locations table: " + e.getMessage());

                try {
                    entityManager.createNativeQuery("DELETE FROM event_location").executeUpdate();
                } catch (Exception e2) {
                    System.out.println("Warning: Could not delete from event_location table: " + e2.getMessage());
                }
            }

            // Try to delete events
            try {
                entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
            } catch (Exception e) {
                System.out.println("Warning: Could not delete from events table: " + e.getMessage());

                try {
                    entityManager.createNativeQuery("DELETE FROM event").executeUpdate();
                } catch (Exception e2) {
                    System.out.println("Warning: Could not delete from event table: " + e2.getMessage());
                }
            }

            // Try to delete regions
            try {
                entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
            } catch (Exception e) {
                System.out.println("Warning: Could not delete from regions table: " + e.getMessage());

                try {
                    entityManager.createNativeQuery("DELETE FROM region").executeUpdate();
                } catch (Exception e2) {
                    System.out.println("Warning: Could not delete from region table: " + e2.getMessage());
                }
            }

            entityManager.flush();
        } catch (Exception e) {
            System.out.println("Warning: Could not clean database: " + e.getMessage());
        }
    }

    private void createTestData() {
        region = createAndSaveRegion();
        event = createAndSaveEvent();
        location = createAndSaveLocation(event, region);

        availableReward = createAndSaveReward(
                location, "REWARD1", "Available Reward",
                (int) 10, (double) 0.5, CommonStatus.ACTIVE);

        depleteReward = createAndSaveReward(
                location, "REWARD2", "Depleted Reward",
                (int) 0, (double) 0.3, CommonStatus.ACTIVE);

        inactiveReward = createAndSaveReward(
                location, "REWARD3", "Inactive Reward",
                (int) 5, (double) 0.4, CommonStatus.INACTIVE);

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
            int quantity, double winProbability,
            CommonStatus status) {
        Reward reward = Reward.builder()
                .eventLocation(location)
                .code(code)
                .name(name)

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
                    assertThat(reward.getQuantity()).isEqualTo(10);
                });
    }

    @Test
    void findByEventLocationId_ShouldReturnAllRewards() {
        var rewards = rewardRepository.findByEventLocationId(location.getId());
        assertThat(rewards).hasSize(4); // Updated from 3 to 4 to include REPO_TEST_REWARD and the new reward
    }

    @Test
    void findByEventLocationIdAndStatus_ShouldReturnFilteredRewards() {
        var activeRewards = rewardRepository.findByEventLocationIdAndStatus(
                location.getId(), CommonStatus.ACTIVE);
        assertThat(activeRewards).hasSize(3); // Changed from 2 to 3 to include the REPO_TEST_REWARD

        var inactiveRewards = rewardRepository.findByEventLocationIdAndStatus(
                location.getId(), CommonStatus.INACTIVE);
        assertThat(inactiveRewards).hasSize(1);
    }

    @Test
    void findAvailableRewardsForLocation_ShouldReturnRewardsWithQuantity() {
        var availableRewards = rewardRepository.findAvailableRewardsForLocation(
                location.getId());

        assertThat(availableRewards)
                .hasSize(2) // Changed from 1 to 2 since we have two active rewards with quantity > 0
                .extracting("code")
                .containsExactlyInAnyOrder("REWARD1", "REPO_TEST_REWARD"); // Updated to check for both reward codes
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

    @Test
    void testFindById() {
        var found = rewardRepository.findById(reward.getId());
        assertTrue(found.isPresent());
        assertEquals("Repo Test Reward", found.get().getName());
    }

    @Test
    void testSaveAndFetch() {
        var newReward = Reward.builder()
                .name("Another Reward")
                .code("ANOTHER_REWARD") // Add required code property
                .quantity(300) // Adjusted to set quantity instead of value
                .quantity(5) // Add required quantity
                .winProbability(0.3) // Add required probability
                .eventLocation(location) // Add required event location
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .spinHistories(new HashSet<>())
                .build();
        var saved = rewardRepository.save(newReward);
        assertNotNull(saved.getId());
        assertEquals("Another Reward", saved.getName());
    }
}