package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@DataJpaTest
// @ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ParticipantEventRepositoryTest {

        @Autowired
        private ParticipantEventRepository participantEventRepository;

        @Autowired
        private ParticipantRepository participantRepository;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private RegionRepository regionRepository;

        @Autowired
        private ProvinceRepository provinceRepository;

        @Autowired
        private EventLocationRepository eventLocationRepository;

        @PersistenceContext
        private EntityManager entityManager;

        private Event event1;
        private Event event2;
        private Participant participant1;
        private Participant participant2;
        private EventLocation location1;
        private ParticipantEvent participantEvent1;
        private ParticipantEvent participantEvent2;
        private ParticipantEvent participantEvent3;

        @BeforeEach
        void setUp() {
                // Clear all tables first to avoid issues with references
                participantEventRepository.deleteAllInBatch();
                eventLocationRepository.deleteAllInBatch();
                participantRepository.deleteAllInBatch();
                eventRepository.deleteAllInBatch();
                provinceRepository.deleteAllInBatch();
                regionRepository.deleteAllInBatch();
                entityManager.flush();
                // Create timestamp for consistent audit fields
                LocalDateTime now = LocalDateTime.now();
                // Create region
                Region region = Region.builder()
                                .name("Test Region")
                                .code("TEST-REGION")
                                .status(CommonStatus.ACTIVE)
                                .build();
                // Add these required audit fields
                region.setCreatedBy("test");
                region.setUpdatedBy("test");
                region.setCreatedAt(now);
                region.setUpdatedAt(now);
                region = regionRepository.save(region);

                // Create province
                Province province = Province.builder()
                                .name("Test Province")
                                .code("TEST-PROV")
                                .region(region)
                                .status(CommonStatus.ACTIVE)
                                .build();
                // Add these required audit fields
                province.setCreatedBy("test");
                province.setUpdatedBy("test");
                province.setCreatedAt(now);
                province.setUpdatedAt(now);
                province = provinceRepository.save(province);

                // Create events
                event1 = Event.builder()
                                .name("Test Event 1")
                                .code("EVENT1")
                                .description("Test Event Description 1")
                                .startTime(now.minusDays(1))
                                .endTime(now.plusDays(7))
                                .status(CommonStatus.ACTIVE)
                                .build();
                event1.setCreatedBy("test");
                event1.setUpdatedBy("test");
                event1.setCreatedAt(now);
                event1.setUpdatedAt(now);

                event2 = Event.builder()
                                .name("Test Event 2")
                                .code("EVENT2")
                                .description("Test Event Description 2")
                                .startTime(now.plusDays(10))
                                .endTime(now.plusDays(20))
                                .status(CommonStatus.ACTIVE)
                                .build();
                event2.setCreatedBy("test");
                event2.setUpdatedBy("test");
                event2.setCreatedAt(now);
                event2.setUpdatedAt(now);

                event1 = eventRepository.save(event1);
                event2 = eventRepository.save(event2);

                // Create event location
                location1 = EventLocation.builder()
                                .name("Test Location")
                                .code("LOC1")
                                .region(region)
                                .event(event1)
                                .maxSpin(10)
                                .status(CommonStatus.ACTIVE)
                                .build();
                location1.setCreatedBy("test");
                location1.setUpdatedBy("test");
                location1.setCreatedAt(now);
                location1.setUpdatedAt(now);
                location1 = eventLocationRepository.save(location1);

                // Create participants
                participant1 = Participant.builder()
                                .name("Test Participant 1")
                                .code("PART1")
                                .phone("123456789")
                                .province(province)
                                .status(CommonStatus.ACTIVE)
                                .build();
                participant1.setCreatedBy("test");
                participant1.setUpdatedBy("test");
                participant1.setCreatedAt(now);
                participant1.setUpdatedAt(now);

                participant2 = Participant.builder()
                                .name("Test Participant 2")
                                .code("PART2")
                                .phone("987654321")
                                .province(province)
                                .status(CommonStatus.ACTIVE)
                                .build();
                participant2.setCreatedBy("test");
                participant2.setUpdatedBy("test");
                participant2.setCreatedAt(now);
                participant2.setUpdatedAt(now);

                participant1 = participantRepository.save(participant1);
                participant2 = participantRepository.save(participant2);

                // Create participant events
                participantEvent1 = ParticipantEvent.builder()
                                .participant(participant1)
                                .event(event1)
                                .eventLocation(location1)
                                .spinsRemaining(5)
                                .status(CommonStatus.ACTIVE)
                                .build();
                participantEvent1.setCreatedBy("test");
                participantEvent1.setUpdatedBy("test");
                participantEvent1.setCreatedAt(now);
                participantEvent1.setUpdatedAt(now);

                participantEvent2 = ParticipantEvent.builder()
                                .participant(participant2)
                                .event(event1)
                                .eventLocation(location1)
                                .spinsRemaining(3)
                                .status(CommonStatus.ACTIVE)
                                .build();
                participantEvent2.setCreatedBy("test");
                participantEvent2.setUpdatedBy("test");
                participantEvent2.setCreatedAt(now);
                participantEvent2.setUpdatedAt(now);

                participantEvent3 = ParticipantEvent.builder()
                                .participant(participant1)
                                .event(event2)
                                .eventLocation(location1)
                                .spinsRemaining(0)
                                .status(CommonStatus.INACTIVE)
                                .build();
                participantEvent3.setCreatedBy("test");
                participantEvent3.setUpdatedBy("test");
                participantEvent3.setCreatedAt(now);
                participantEvent3.setUpdatedAt(now);

                participantEvent1 = participantEventRepository.save(participantEvent1);
                participantEvent2 = participantEventRepository.save(participantEvent2);
                participantEvent3 = participantEventRepository.save(participantEvent3);

                entityManager.flush();
                entityManager.clear();
        }

        @Test
        void findByParticipantAndEvent_ShouldReturnParticipantEvent() {
                // When
                var result = participantEventRepository.findByParticipantAndEvent(participant1, event1);

                // Then
                assertThat(result).isPresent();
                assertThat(result.get().getSpinsRemaining()).isEqualTo(5);
        }

        @Test
        void findByEventId_ShouldReturnParticipantEvents() {
                // When
                var result = participantEventRepository.findByEventId(event1.getId());

                // Then
                assertThat(result).hasSize(2);
                assertThat(result).extracting("participant.name")
                                .containsExactlyInAnyOrder("Test Participant 1", "Test Participant 2");
        }

        @Test
        void countByEvent_ShouldReturnCorrectCount() {
                // When
                long count = participantEventRepository.countByEvent(event1);

                // Then
                assertThat(count).isEqualTo(2);
        }

        @Test
        void countByEventAndStatus_ShouldReturnCorrectCount() {
                // When
                long activeCount = participantEventRepository.countByEventAndStatus(event1, CommonStatus.ACTIVE);
                long inactiveCount = participantEventRepository.countByEventAndStatus(event2, CommonStatus.INACTIVE);

                // Then
                assertThat(activeCount).isEqualTo(2);
                assertThat(inactiveCount).isEqualTo(1);
        }
}
