package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.EventLocationKey;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.ParticipantEventKey;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.RewardEvent;
import vn.com.fecredit.app.entity.RewardEventKey;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class ParticipantEventRepositoryTest extends AbstractRepositoryTest {

        @Autowired
        private ParticipantEventRepository participantEventRepository;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private EventLocationRepository eventLocationRepository;

        @PersistenceContext
        private EntityManager entityManager;

        private final LocalDateTime now = LocalDateTime.now();
        private Region region;
        private Province province;
        private Event event1, event2;
        private EventLocation location1, location2;
        private Participant participant1, participant2;
        private ParticipantEvent participantEvent1, participantEvent2, participantEvent3;

        @BeforeEach
        void setUp() {
                cleanDatabase();
                createTestData();
        }

        private void cleanDatabase() {
                entityManager.createNativeQuery("DELETE FROM spin_histories").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM rewards").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM participant_events").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM participants").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM events").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();

                entityManager.flush();
        }

        private void createTestData() {
                // Create region
                region = Region.builder()
                                .name("Test Region")
                                .code("TR1")
                                .status(CommonStatus.ACTIVE)
                                .provinces(new HashSet<>())
                                .eventLocations(new HashSet<>())
                                .build();
                region.setVersion(0L);
                region.setCreatedBy("test");
                region.setUpdatedBy("test");
                region.setCreatedAt(now);
                region.setUpdatedAt(now);
                region = entityManager.merge(region);

                // Create a second region for location2
                Region region2 = Region.builder()
                                .name("Test Region 2")
                                .code("TR2")
                                .status(CommonStatus.ACTIVE)
                                .provinces(new HashSet<>())
                                .eventLocations(new HashSet<>())
                                .build();
                region2.setVersion(0L);
                region2.setCreatedBy("test");
                region2.setUpdatedBy("test");
                region2.setCreatedAt(now);
                region2.setUpdatedAt(now);
                region2 = entityManager.merge(region2);

                // Create province
                province = Province.builder()
                                .name("Test Province")
                                .code("TP1")
                                .regions(new HashSet<>() {
                                    {
                                        add(region);
                                    }
                                })
                                .status(CommonStatus.ACTIVE)
                                .participants(new HashSet<>())
                                .build();
                province.setVersion(0L);
                province.setCreatedBy("test");
                province.setUpdatedBy("test");
                province.setCreatedAt(now);
                province.setUpdatedAt(now);
                province = entityManager.merge(province);
                region.getProvinces().add(province);
                entityManager.merge(region);
                // Create events
                event1 = Event.builder()
                                .name("Test Event 1")
                                .code("TE1")
                                .startTime(now.minusHours(1))
                                .endTime(now.plusHours(5))
                                .status(CommonStatus.ACTIVE)
                                .locations(new HashSet<>())
                                .build();
                event1.setVersion(0L);
                event1.setCreatedBy("test-user");
                event1.setUpdatedBy("test-user");
                event1.setCreatedAt(now);
                event1.setUpdatedAt(now);
                event1 = eventRepository.save(event1);

                event2 = Event.builder()
                                .name("Test Event 2")
                                .code("TE2")
                                .startTime(now.minusHours(2))
                                .endTime(now.plusHours(3))
                                .status(CommonStatus.ACTIVE)
                                .locations(new HashSet<>())
                                .build();
                event2.setVersion(0L);
                event2.setCreatedBy("test-user");
                event2.setUpdatedBy("test-user");
                event2.setCreatedAt(now);
                event2.setUpdatedAt(now);
                event2 = eventRepository.save(event2);

                // Flush to ensure IDs are generated
                entityManager.flush();

                // Create event location with properly initialized composite key
                EventLocationKey locationKey = EventLocationKey.of(event1.getId(), region.getId());
                location1 = EventLocation.builder()
                                .region(region)
                                .event(event1)
                                .maxSpin(10)
                                .status(CommonStatus.ACTIVE)
                                .participantEvents(new HashSet<>())
                                .rewardEvents(new HashSet<>())
                                .goldenHours(new HashSet<>())
                                .todaySpin(50)
                                .build();
                location1.setId(locationKey); // Set the composite key explicitly
                location1.setCreatedBy("test");
                location1.setUpdatedBy("test");
                location1.setCreatedAt(now);
                location1.setUpdatedAt(now);

                location1 = eventLocationRepository.save(location1);

                // Create a second location for event1 with a different region
                EventLocationKey locationKey2 = EventLocationKey.of(event1.getId(), region2.getId());
                location2 = EventLocation.builder()
                                .region(region2) // Use region2 instead of region
                                .event(event1)
                                .maxSpin(10)
                                .status(CommonStatus.INACTIVE) // Different status for variety
                                .participantEvents(new HashSet<>())
                                .rewardEvents(new HashSet<>())
                                .goldenHours(new HashSet<>())
                                .todaySpin(50)
                                .build();
                location2.setId(locationKey2);
                location2.setCreatedBy("test");
                location2.setUpdatedBy("test");
                location2.setCreatedAt(now);
                location2.setUpdatedAt(now);

                location2 = eventLocationRepository.save(location2);

                // Clear persistence context to avoid entity conflicts
                entityManager.flush();
                entityManager.clear();

                // Reload entities
                event1 = eventRepository.findById(event1.getId()).orElseThrow();
                region = entityManager.find(Region.class, region.getId());
                location1 = eventLocationRepository.findById(locationKey).orElseThrow();
                location2 = eventLocationRepository.findById(locationKey2).orElseThrow();

                // Create participants
                participant1 = Participant.builder()
                                .name("John Doe")
                                .code("P1")
                                .province(province)
                                .status(CommonStatus.ACTIVE)
                                .participantEvents(new HashSet<>())
                                .build();
                participant1.setVersion(0L);
                participant1.setCreatedBy("test");
                participant1.setUpdatedBy("test");
                participant1.setCreatedAt(now);
                participant1.setUpdatedAt(now);
                participant1 = entityManager.merge(participant1);

                participant2 = Participant.builder()
                                .name("Jane Smith")
                                .code("P2")
                                .province(province)
                                .status(CommonStatus.ACTIVE)
                                .participantEvents(new HashSet<>())
                                .build();
                participant2.setVersion(0L);
                participant2.setCreatedBy("test");
                participant2.setUpdatedBy("test");
                participant2.setCreatedAt(now);
                participant2.setUpdatedAt(now);
                participant2 = entityManager.merge(participant2);

                // Create participant events with properly initialized composite keys
                ParticipantEventKey participantEventKey1 = new ParticipantEventKey();
                participantEventKey1.setEventLocationKey(location1.getId());
                participantEventKey1.setParticipantId(participant1.getId());

                participantEvent1 = ParticipantEvent.builder()
                                .participant(participant1)
                                .eventLocation(location1)
                                .spinsRemaining(5)
                                .status(CommonStatus.ACTIVE)
                                .spinHistories(new ArrayList<>())
                                .build();
                participantEvent1.setId(participantEventKey1); // Set the composite key
                participantEvent1.setCreatedBy("test");
                participantEvent1.setUpdatedBy("test");
                participantEvent1.setCreatedAt(now);
                participantEvent1.setUpdatedAt(now);

                ParticipantEventKey participantEventKey2 = new ParticipantEventKey();
                participantEventKey2.setEventLocationKey(location1.getId());
                participantEventKey2.setParticipantId(participant2.getId());

                participantEvent2 = ParticipantEvent.builder()
                                .participant(participant2)
                                .eventLocation(location1)
                                .spinsRemaining(3)
                                .status(CommonStatus.ACTIVE)
                                .spinHistories(new ArrayList<>())
                                .build();
                participantEvent2.setId(participantEventKey2); // Set the composite key
                participantEvent2.setCreatedBy("test");
                participantEvent2.setUpdatedBy("test");
                participantEvent2.setCreatedAt(now);
                participantEvent2.setUpdatedAt(now);

                // Use location2 for participantEvent3
                ParticipantEventKey participantEventKey3 = new ParticipantEventKey();
                participantEventKey3.setEventLocationKey(location2.getId());
                participantEventKey3.setParticipantId(participant1.getId());

                participantEvent3 = ParticipantEvent.builder()
                                .participant(participant1)
                                .eventLocation(location2)
                                .spinsRemaining(0)
                                .status(CommonStatus.INACTIVE)
                                .spinHistories(new ArrayList<>())
                                .build();
                participantEvent3.setId(participantEventKey3); // Set the composite key
                participantEvent3.setCreatedBy("test");
                participantEvent3.setUpdatedBy("test");
                participantEvent3.setCreatedAt(now);
                participantEvent3.setUpdatedAt(now);

                // Save all participant events
                participantEvent1 = participantEventRepository.save(participantEvent1);
                participantEvent2 = participantEventRepository.save(participantEvent2);
                participantEvent3 = participantEventRepository.save(participantEvent3);

                // First create and persist the Reward entity
                Reward reward = Reward.builder()
                                .name("Test Reward")
                                .code("TR1")
                                .prizeValue(BigDecimal.valueOf(100.00))
                                .status(CommonStatus.ACTIVE)
                                .rewardEvents(new HashSet<>())
                                .build();
                reward.setVersion(0L);
                reward.setCreatedBy("test");
                reward.setUpdatedBy("test");
                reward.setCreatedAt(now);
                reward.setUpdatedAt(now);
                entityManager.persist(reward);
                entityManager.flush(); // Make sure the reward has an ID

                // Now create the RewardEvent with proper composite key
                RewardEventKey rewardEventKey = new RewardEventKey();
                rewardEventKey.setEventLocationKey(location1.getId());
                rewardEventKey.setRewardId(reward.getId());

                RewardEvent rewardEvent = RewardEvent.builder()
                                .eventLocation(location1)
                                .reward(reward)
                                .quantity(50)
                                .todayQuantity(10)
                                .status(CommonStatus.ACTIVE)
                                .build();
                rewardEvent.setId(rewardEventKey);
                rewardEvent.setCreatedBy("test");
                rewardEvent.setUpdatedBy("test");
                rewardEvent.setCreatedAt(now);
                rewardEvent.setUpdatedAt(now);

                // Set bidirectional relationship
                reward.getRewardEvents().add(rewardEvent);

                // Persist the RewardEvent
                entityManager.persist(rewardEvent);

                entityManager.flush();
                entityManager.clear();
        }

        @Test
        void findByParticipantId_ShouldReturnMatchingEvents() {
                // When
                var result = participantEventRepository.findByParticipantId(participant1.getId());

                // Then
                assertThat(result).hasSize(2);
                assertThat(result).extracting("participant")
                                .extracting("name")
                                .containsOnly("John Doe");
        }

        @Test
        void findByEventId_ShouldReturnMatchingEvents() {
                // When
                var result = participantEventRepository.findByEventLocationId(event1.getId());

                // Then
                // We expect 3 events because:
                // - participantEvent1 is associated with location1 (event1, region1)
                // - participantEvent2 is associated with location1 (event1, region1)
                // - participantEvent3 is associated with location2 (event1, region2)
                assertThat(result).hasSize(3);
                assertThat(result).extracting("eventLocation")
                                .extracting("event")
                                .extracting("code")
                                .containsOnly("TE1");
        }

        @Test
        void findActiveByParticipant_ShouldReturnOnlyActiveEvents() {
                // When
                var result = participantEventRepository.findByParticipantIdAndStatus(participant1.getId(),
                                CommonStatus.ACTIVE);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getStatus()).isEqualTo(CommonStatus.ACTIVE);
        }

        @Test
        void findWithSpinsRemaining_ShouldReturnEventsWithSpins() {
                // When
                var result = participantEventRepository
                                .findByParticipantIdAndSpinsRemainingGreaterThan(participant1.getId(), 0);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getSpinsRemaining()).isGreaterThan(0);
        }

        @Test
        void findPaginated_ShouldReturnPagedResults() {
                // When
                Page<ParticipantEvent> result = participantEventRepository.findAll(
                                Pageable.ofSize(2));

                // Then
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getTotalElements()).isEqualTo(3);
                assertThat(result.getTotalPages()).isEqualTo(2);
        }

        @Test
        void saveParticipantEvent_ShouldUpdateSpinCount() {
                // Given
                participantEvent1.setSpinsRemaining(10);

                // When
                ParticipantEvent saved = participantEventRepository.save(participantEvent1);
                entityManager.flush();

                // Then
                ParticipantEvent reloaded = participantEventRepository.findById(saved.getId()).orElseThrow();
                assertThat(reloaded.getSpinsRemaining()).isEqualTo(10);
        }
}
