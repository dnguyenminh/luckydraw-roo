package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.RewardEvent;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class SimpleSpinHistoryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ParticipantEventRepository participantEventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Region testRegion;
    private Province testProvince;
    private Event testEvent;
    private EventLocation testEventLocation;
    private Participant testParticipant;
    private ParticipantEvent testParticipantEvent;
    private SpinHistory activeSpinHistory1;
    private SpinHistory activeSpinHistory2;
    private SpinHistory inactiveSpinHistory;

    @BeforeEach
    void setUp() {
        // Create test entities with unique identifiers
        createTestEntities();

        // Flush to ensure all entities are persisted
        entityManager.flush();

        // Clear the persistence context to ensure we're getting fresh data
        entityManager.clear();
    }

    private void createTestEntities() {
        LocalDateTime now = LocalDateTime.now();
        String userName = "test-user-" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        // Create Region with unique code
        testRegion = Region.builder()
            .name("Test Region " + uniqueSuffix)
            .code("TEST_REGION_" + uniqueSuffix)
            .status(CommonStatus.ACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        regionRepository.save(testRegion);

        // Create Province with unique code
        testProvince = Province.builder()
            .name("Test Province " + uniqueSuffix)
            .code("TEST_PROV_" + uniqueSuffix)
            .status(CommonStatus.ACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        provinceRepository.save(testProvince);

        // Add province to region
        testRegion.addProvince(testProvince);
        testProvince.addRegion(testRegion);
        regionRepository.save(testRegion);

        // Create Event with unique code
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(5);

        testEvent = Event.builder()
            .name("Test Event " + uniqueSuffix)
            .code("TEST_EVENT_" + uniqueSuffix)
            .description("Test Description " + uniqueSuffix)
            .startTime(startTime)
            .endTime(endTime)
            .status(CommonStatus.ACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        eventRepository.save(testEvent);

        // Create EventLocation
        testEventLocation = EventLocation.builder()
            .event(testEvent)
            .region(testRegion)
            .status(CommonStatus.ACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();

        // Set up bidirectional relationships
        testEvent.addLocation(testEventLocation);
        testRegion.addEventLocation(testEventLocation);

        // Save EventLocation directly instead of through the parent entities
        eventLocationRepository.save(testEventLocation);

        // Create Participant with unique code
        testParticipant = Participant.builder()
            .name("Test Participant " + uniqueSuffix)
            .code("TEST_PART_" + uniqueSuffix)
            .phone("1234567890" + uniqueSuffix.substring(0, 2))
            .province(testProvince)
            .status(CommonStatus.ACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        participantRepository.save(testParticipant);

        // Create ParticipantEvent
        testParticipantEvent = ParticipantEvent.builder()
            .participant(testParticipant)
            .eventLocation(testEventLocation)
            .spinsRemaining(5)
            .status(CommonStatus.ACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        testParticipant.addParticipantEvent(testParticipantEvent);
        testEventLocation.addParticipantEvent(testParticipantEvent);
        participantEventRepository.save(testParticipantEvent);

        // Create active and inactive SpinHistory records with unique spin times
        LocalDateTime spinTime1 = LocalDateTime.now().minusHours(3);
        LocalDateTime spinTime2 = LocalDateTime.now().minusHours(2);
        LocalDateTime spinTime3 = LocalDateTime.now().minusHours(1);

        Reward reward = Reward.builder()
            .code("TEST_REWARD_" + uniqueSuffix)
            .name("Test Reward " + uniqueSuffix)
            .description("Test Reward Description " + uniqueSuffix)
            .status(CommonStatus.ACTIVE)
            .prizeValue(new BigDecimal(1000))
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        entityManager.persist(reward);
        RewardEvent rewardEvent = RewardEvent.builder()
            .eventLocation(testEventLocation)
            .reward(reward)
            .status(CommonStatus.ACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        testEventLocation.addRewardEvent(rewardEvent);
        reward.addRewardEvent(rewardEvent);
        entityManager.persist(rewardEvent);
        activeSpinHistory1 = SpinHistory.builder()
            .participantEvent(testParticipantEvent)
            .rewardEvent(rewardEvent)
            .spinTime(spinTime1)
            .win(true)
            .status(CommonStatus.ACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        entityManager.persist(activeSpinHistory1);

        activeSpinHistory2 = SpinHistory.builder()
            .participantEvent(testParticipantEvent)
            .spinTime(spinTime2)
            .win(false)
            .status(CommonStatus.ACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        entityManager.persist(activeSpinHistory2);

        inactiveSpinHistory = SpinHistory.builder()
            .participantEvent(testParticipantEvent)
            .rewardEvent(rewardEvent)
            .spinTime(spinTime3)
            .win(true)
            .status(CommonStatus.INACTIVE)
            .createdAt(now)
            .createdBy(userName)
            .updatedAt(now)
            .updatedBy(userName)
            .build();
        entityManager.persist(inactiveSpinHistory);
    }

    @Test
    void testFindByStatus() {
        // Test the findByStatus method
        List<SpinHistory> activeSpins = spinHistoryRepository.findByStatus(CommonStatus.ACTIVE);

        // Should find at least our 2 active spins
        assertThat(activeSpins.size()).isGreaterThanOrEqualTo(2);
        assertThat(activeSpins).allMatch(s -> s.getStatus() == CommonStatus.ACTIVE);
        assertThat(activeSpins).contains(activeSpinHistory1, activeSpinHistory2);

        // Test with INACTIVE status
        List<SpinHistory> inactiveSpins = spinHistoryRepository.findByStatus(CommonStatus.INACTIVE);

        // Should find at least our 1 inactive spin
        assertThat(inactiveSpins.size()).isGreaterThanOrEqualTo(1);
        assertThat(inactiveSpins).allMatch(s -> s.getStatus() == CommonStatus.INACTIVE);
        assertThat(inactiveSpins).contains(inactiveSpinHistory);
    }
}
