package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.repository.ParticipantRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceImplTest {

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    private Participant activeParticipant;
    private Participant inactiveParticipant;
    private Province province;
    private Region region;

    @BeforeEach
    void setUp() {
        region = Region.builder()
                .id(1L)
                .name("Test Region")
                .code("REG001")
                .status(CommonStatus.ACTIVE)
                .build();

        province = Province.builder()
                .id(1L)
                .name("Test Province")
                .code("PROV001")
                .region(region)
                .status(CommonStatus.ACTIVE)
                .build();

        activeParticipant = Participant.builder()
                .id(1L)
                .name("Active Participant")
                .code("PART001")
                .phone("1234567890")
                // .email("active@example.com")
                .province(province)
                .status(CommonStatus.ACTIVE)
                .build();

        inactiveParticipant = Participant.builder()
                .id(2L)
                .name("Inactive Participant")
                .code("PART002")
                .phone("0987654321")
                // .email("inactive@example.com")
                .province(province)
                .status(CommonStatus.INACTIVE)
                .build();
    }

    @Test
    void findByCode_ShouldReturnParticipant_WhenParticipantExists() {
        // Given
        when(participantRepository.findByCode("PART001")).thenReturn(Optional.of(activeParticipant));

        // When
        Optional<Participant> result = participantService.findByCode("PART001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Active Participant", result.get().getName());
        verify(participantRepository).findByCode("PART001");
    }

    @Test
    void findByProvinceId_ShouldReturnParticipants() {
        // Given
        Long provinceId = 1L;
        when(participantRepository.findByProvinceId(provinceId))
                .thenReturn(Arrays.asList(activeParticipant, inactiveParticipant));

        // When
        List<Participant> result = participantService.findByProvinceId(provinceId);

        // Then
        assertEquals(2, result.size());
        verify(participantRepository).findByProvinceId(provinceId);
    }
   
    @Test
    void findByEventId_ShouldReturnParticipants() {
        // Given
        Long eventId = 1L;
        when(participantRepository.findByEventId(eventId))
                .thenReturn(Collections.singletonList(activeParticipant));

        // When
        List<Participant> result = participantService.findByEventId(eventId);

        // Then
        assertEquals(1, result.size());
        assertEquals("Active Participant", result.get(0).getName());
        verify(participantRepository).findByEventId(eventId);
    }

    @Test
    void findByStatus_ShouldReturnFilteredParticipants() {
        // Given
        when(participantRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Collections.singletonList(activeParticipant));

        // When
        List<Participant> result = participantService.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertEquals(1, result.size());
        assertEquals("Active Participant", result.get(0).getName());
        verify(participantRepository).findByStatus(CommonStatus.ACTIVE);
    }
}
