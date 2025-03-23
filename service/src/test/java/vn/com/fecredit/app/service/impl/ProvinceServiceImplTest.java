package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.repository.ProvinceRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvinceServiceImplTest {

    @Mock
    private ProvinceRepository provinceRepository;

    @InjectMocks
    private ProvinceServiceImpl provinceService;

    private Province activeProvince;
    private Province inactiveProvince;
    private Region region;

    @BeforeEach
    void setUp() {
        region = Region.builder()
                .id(1L)
                .name("Test Region")
                .code("REG001")
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .build();

        activeProvince = Province.builder()
                .id(1L)
                .name("Active Province")
                .code("PROV001")
                .status(CommonStatus.ACTIVE)
                .region(region)
                .build();

        inactiveProvince = Province.builder()
                .id(2L)
                .name("Inactive Province")
                .code("PROV002")
                .status(CommonStatus.INACTIVE)
                .region(region)
                .build();
        
        // Set up bidirectional relationship
        region.getProvinces().add(activeProvince);
        region.getProvinces().add(inactiveProvince);
    }

    @Test
    void findByCode_ShouldReturnProvince_WhenProvinceExists() {
        // Given
        when(provinceRepository.findByCode("PROV001")).thenReturn(Optional.of(activeProvince));

        // When
        Optional<Province> result = provinceService.findByCode("PROV001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Active Province", result.get().getName());
        verify(provinceRepository).findByCode("PROV001");
    }

    @Test
    void findByCode_ShouldReturnEmpty_WhenProvinceDoesNotExist() {
        // Given
        when(provinceRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When
        Optional<Province> result = provinceService.findByCode("NONEXISTENT");

        // Then
        assertFalse(result.isPresent());
        verify(provinceRepository).findByCode("NONEXISTENT");
    }

    @Test
    void existsByCode_ShouldReturnTrue_WhenProvinceExists() {
        // Given
        when(provinceRepository.existsByCode("PROV001")).thenReturn(true);

        // When
        boolean result = provinceService.existsByCode("PROV001");

        // Then
        assertTrue(result);
        verify(provinceRepository).existsByCode("PROV001");
    }

    @Test
    void existsByCode_ShouldReturnFalse_WhenProvinceDoesNotExist() {
        // Given
        when(provinceRepository.existsByCode("NONEXISTENT")).thenReturn(false);

        // When
        boolean result = provinceService.existsByCode("NONEXISTENT");

        // Then
        assertFalse(result);
        verify(provinceRepository).existsByCode("NONEXISTENT");
    }

    @Test
    void findByRegionId_ShouldReturnProvinces() {
        // Given
        Long regionId = 1L;
        when(provinceRepository.findByRegionId(regionId))
                .thenReturn(Arrays.asList(activeProvince, inactiveProvince));

        // When
        List<Province> result = provinceService.findByRegionId(regionId);

        // Then
        assertEquals(2, result.size());
        verify(provinceRepository).findByRegionId(regionId);
    }

    @Test
    void findByRegionCode_ShouldReturnProvinces() {
        // Given
        String regionCode = "REG001";
        when(provinceRepository.findByRegionCode(regionCode))
                .thenReturn(Arrays.asList(activeProvince, inactiveProvince));

        // When
        List<Province> result = provinceService.findByRegionCode(regionCode);

        // Then
        assertEquals(2, result.size());
        verify(provinceRepository).findByRegionCode(regionCode);
    }

    @Test
    void findByStatus_ShouldReturnFilteredProvinces() {
        // Given
        when(provinceRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Collections.singletonList(activeProvince));

        // When
        List<Province> result = provinceService.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertEquals(1, result.size());
        assertEquals("Active Province", result.get(0).getName());
        verify(provinceRepository).findByStatus(CommonStatus.ACTIVE);
    }

    @Test
    void deactivate_ShouldDeactivateProvince() {
        // Given
        when(provinceRepository.findById(1L)).thenReturn(Optional.of(activeProvince));
        when(provinceRepository.save(any(Province.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Province result = provinceService.deactivate(1L);

        // Then
        assertEquals(CommonStatus.INACTIVE, result.getStatus());
        verify(provinceRepository).findById(1L);
        verify(provinceRepository).save(activeProvince);
    }
}
