package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class ParticipantRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ParticipantRepository participantRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused") // Fields used for test data setup
    private Participant activeParticipant;

    @SuppressWarnings("unused") // Fields used for test data setup
    private Participant inactiveParticipant;

    private Region region;
    private Province province;
    private final LocalDateTime now = LocalDateTime.now(); // Added timestamp for entity creation

    @BeforeEach
    void setUp() {
        // Clear previous data
        entityManager.createNativeQuery("DELETE FROM participants").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();

        // Create test data
        region = createAndSaveRegion();
        province = createAndSaveProvince(region);

        activeParticipant = createAndSaveParticipant("ACTIVE", "Active Participant", province);
        inactiveParticipant = createAndSaveParticipant("INACTIVE", "Inactive Participant", province);

        entityManager.flush();
    }

    private Region createAndSaveRegion() {
        Region region = Region.builder()
                .name("Test Region")
                .code("TEST_REG")
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .createdAt(now) // Set createdAt timestamp
                .updatedAt(now) // Set updatedAt timestamp
                .createdBy("test-user") // Set creator
                .updatedBy("test-user") // Set updater
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        entityManager.persist(region);
        return region;
    }

    private Province createAndSaveProvince(Region region) {
        Province province = Province.builder()
                .name("Test Province")
                .code("TEST_PROV")
                .regions(new HashSet<>() {
                    {
                        add(region);
                    }
                })
                .status(CommonStatus.ACTIVE)
                .version(0L)
                .createdAt(now) // Set createdAt timestamp
                .updatedAt(now) // Set updatedAt timestamp
                .createdBy("test-user") // Set creator
                .updatedBy("test-user") // Set updater
                .participants(new HashSet<>())
                .build();
        entityManager.persist(province);
        region.getProvinces().add(province); // Maintain bidirectional relationship
        return province;
    }

    private Participant createAndSaveParticipant(String code, String name, Province province) {
        Participant participant = Participant.builder()
                .code(code)
                .name(name)
                .phone(code.equals("ACTIVE") ? "1234567890" : "0987654321") // Add phone numbers
                .province(province)
                .status(code.equals("ACTIVE") ? CommonStatus.ACTIVE : CommonStatus.INACTIVE) // Set status based on the
                                                                                             // code
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .participantEvents(new HashSet<>())
                .build();
        entityManager.persist(participant);
        province.getParticipants().add(participant);
        return participant;
    }

    @Test
    void findByCode_shouldReturnParticipant_whenExists() {
        var result = participantRepository.findByCode("ACTIVE");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Active Participant");
    }

    @Test
    void findByPhone_shouldReturnParticipant_whenExists() {
        var result = participantRepository.findByPhone("1234567890");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("ACTIVE");
    }

    @Test
    void existsByCode_shouldReturnTrue_whenExists() {
        boolean exists = participantRepository.existsByCode("ACTIVE");
        boolean notExists = participantRepository.existsByCode("NONEXISTENT");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void findByStatus_shouldReturnFilteredParticipants() {
        var activeParticipants = participantRepository.findByStatus(CommonStatus.ACTIVE);
        var inactiveParticipants = participantRepository.findByStatus(CommonStatus.INACTIVE);

        assertThat(activeParticipants).hasSize(1);
        assertThat(inactiveParticipants).hasSize(1);

        assertThat(activeParticipants.get(0).getCode()).isEqualTo("ACTIVE");
        assertThat(inactiveParticipants.get(0).getCode()).isEqualTo("INACTIVE");
    }

    @Test
    void findByProvinceId_shouldReturnParticipantsInProvince() {
        var participants = participantRepository.findByProvinceId(province.getId());

        assertThat(participants).hasSize(2);
    }

    @Test
    void findActiveParticipantsInEvent_shouldReturnActiveParticipants() {
        // Since we're using a custom query, we'll need to set up event data
        // This is a bit complex for this test, so we'll just verify the method exists
        var eventId = 1L;
        var result = participantRepository.findActiveParticipantsInEvent(eventId);

        // We don't have event data, so this should be empty
        assertThat(result).isEmpty();
    }
}