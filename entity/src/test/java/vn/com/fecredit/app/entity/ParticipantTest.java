package vn.com.fecredit.app.entity;

// import vn.com.fecredit.app.entity.util.TestUtil.generateUniqueCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Test class for the Participant entity.
 */
class ParticipantTest {

    private Participant participant;
    private Event event;
    private EventLocation eventLocation;
    private Region region;
    private Province province;

    @BeforeEach
    void setUp() {
        region = Region.builder()
            .name("Test Region")
            .code("TEST_REG")
            .status(CommonStatus.ACTIVE)
            .build();

        province = Province.builder()
            .name("Test Province")
            .code("TEST_PROV")
            .region(region)
            .status(CommonStatus.ACTIVE)
            .build();

        event = Event.builder()
            .name("Test Event")
            .code("TEST_EVENT")
            .status(CommonStatus.ACTIVE)
            .build();

        eventLocation = EventLocation.builder()
            .region(region)
            .maxSpin(1000)
            .status(CommonStatus.ACTIVE)
            .build();

        participant = Participant.builder()
            .name("Test Participant")
            .code("TEST_PART")
            // .email("test@example.com")
            // .phone("1234567890")
            .province(province)
            .status(CommonStatus.ACTIVE)
            .build();

        // Set up bidirectional relationships
        region.addEventLocation(eventLocation);
    }

    @Test
    void testEventParticipation() {
        assertTrue(participant.getParticipantEvents().isEmpty());

        ParticipantEvent pe = ParticipantEvent.builder()
            .eventLocation(eventLocation)
            .participant(participant)
            .spinsRemaining(10) // Default spins
            .status(CommonStatus.ACTIVE)
            .build();

        participant.getParticipantEvents().add(pe);
        assertEquals(1, participant.getParticipantEvents().size());
        assertEquals(participant, pe.getParticipant());
    }

    @Test
    void testProvinceAssociation() {
        assertEquals(province, participant.getProvince());

        Province newProvince = Province.builder()
            .name("New Province")
            .code("NEW_PROV")
            .region(region)
            .status(CommonStatus.ACTIVE)
            .build();

        participant.setProvince(newProvince);
        assertEquals(newProvince, participant.getProvince());
        assertTrue(newProvince.getParticipants().contains(participant));
        assertFalse(province.getParticipants().contains(participant));
    }

    @Test
    void testValidation() {
        Participant invalid = Participant.builder().build();
        assertThrows(IllegalStateException.class, () -> invalid.validateState());

        invalid.setName("Test");
        invalid.setCode("");
        assertThrows(IllegalStateException.class, () -> invalid.validateState());

        invalid.setCode("TEST");
        invalid.setProvince(null);
        assertThrows(IllegalStateException.class, () -> invalid.validateState());

        invalid.setProvince(province);
        assertDoesNotThrow(() -> invalid.validateState());
    }

    // @Test
    // void testContactInformation() {
    //     // participant.setPhone("0987654321");
    //     // participant.setEmail("newemail@example.com");
    //     // participant.setContact("Alternative Contact");

    //     // assertEquals("0987654321", participant.getPhone());
    //     // assertEquals("newemail@example.com", participant.getEmail());
    //     // assertEquals("Alternative Contact", participant.getContact());
    // }
}
