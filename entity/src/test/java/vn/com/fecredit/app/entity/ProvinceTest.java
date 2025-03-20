package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProvinceTest {

    private Province province;
    private Region region;
    private Participant participant;

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

        participant = Participant.builder()
            .name("Test Participant")
            .code("TEST_PART")
            .status(CommonStatus.ACTIVE)
            .build();
    }

    @Test
    void testParticipantAssociation() {
        assertTrue(province.getParticipants().isEmpty());
        province.addParticipant(participant);
        
        assertTrue(province.getParticipants().contains(participant));
        assertEquals(province, participant.getProvince());

        province.removeParticipant(participant);
        assertFalse(province.getParticipants().contains(participant));
        assertNull(participant.getProvince());
    }

    @Test
    void testStatusManagement() {
        // Test province deactivation affects active region
        region.setStatus(CommonStatus.ACTIVE);
        province.setStatus(CommonStatus.INACTIVE);
        assertFalse(province.isActive());
        assertFalse(region.isActive(), "Region should is inactive after province deactivation");

        // Test province activation with active region
        province.setStatus(CommonStatus.ACTIVE);
        assertTrue(province.isActive());
        assertFalse(region.isActive(), "Region should remain inactive");

        // Test province status with inactive region
        region.setStatus(CommonStatus.INACTIVE);
        assertTrue(province.isActive(), "Province should remain active when region is deactivated");
    }

    @Test
    void testRegionStatusSync() {
        Province province2 = Province.builder()
            .name("Second Province")
            .code("PROV2")
            .region(region)
            .status(CommonStatus.ACTIVE)
            .build();
        region.addProvince(province2);

        // Test inactive region not affected by province changes
        region.setStatus(CommonStatus.INACTIVE);
        province.setStatus(CommonStatus.ACTIVE);
        province2.setStatus(CommonStatus.ACTIVE);
        assertFalse(region.isActive(), "Inactive region should stay inactive");

        // Test active region with multiple provinces
        region.setStatus(CommonStatus.ACTIVE);
        province.setStatus(CommonStatus.INACTIVE);
        assertTrue(region.isActive(), "Region should stay active with one active province");

        // Test region deactivation when all provinces inactive
        province2.setStatus(CommonStatus.INACTIVE);
        assertFalse(region.isActive(), "Region should become inactive when all provinces are inactive");
    }

    @Test
    void testValidation() {
        // Test code normalization
        province.setCode("lowercase_code");
        province.validateState();
        assertEquals("LOWERCASE_CODE", province.getCode());

        // Test missing region
        Province invalid = Province.builder()
            .name("Invalid")
            .code("INVALID")
            .build();
        assertThrows(IllegalStateException.class, () -> invalid.validateState(),
            "Should not validate without region");

        // Test Province status independent of Region status
        region.setStatus(CommonStatus.INACTIVE);
        province.setStatus(CommonStatus.ACTIVE);
        assertDoesNotThrow(() -> province.validateState(),
            "Should allow Province status independent of Region status");
    }
}
