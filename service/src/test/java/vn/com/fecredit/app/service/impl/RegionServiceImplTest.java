package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.repository.RegionRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionServiceImpl regionService;

    @SuppressWarnings("unused") // Field used for test data setup
    private Region inactiveRegion;
    
    private Region activeRegion;
    private EventLocation location1;
    private EventLocation location2;

    @BeforeEach
    void setUp() {
        location1 = EventLocation.builder()
                .id(1L)
                .name("Location 1")
                .code("LOC001")
                .status(CommonStatus.ACTIVE)
                .build();

        location2 = EventLocation.builder()
                .id(2L)
                .name("Location 2")
                .code("LOC002")
                .status(CommonStatus.ACTIVE)
                .build();

        Set<EventLocation> locations = new HashSet<>();
        locations.add(location1);
        locations.add(location2);

        activeRegion = Region.builder()
                .id(1L)
                .name("Active Region")
                .code("REG001")
                .status(CommonStatus.ACTIVE)
                .eventLocations(locations)
                .build();

        inactiveRegion = Region.builder()
                .id(2L)
                .name("Inactive Region")
                .code("REG002")
                .status(CommonStatus.INACTIVE)
                .eventLocations(new HashSet<>())
                .build();
        
        // Set up bidirectional relationship
        location1.setRegion(activeRegion);
        location2.setRegion(activeRegion);
    }

    @Test
    void findByCode_ShouldReturnRegion_WhenRegionExists() {
        // Given
        when(regionRepository.findByCode("REG001")).thenReturn(Optional.of(activeRegion));

        // When
        Optional<Region> result = regionService.findByCode("REG001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Active Region", result.get().getName());
        verify(regionRepository).findByCode("REG001");
    }

    @Test
    void existsByCode_ShouldReturnTrue_WhenRegionExists() {
        // Given
        when(regionRepository.existsByCode("REG001")).thenReturn(true);

        // When
        boolean result = regionService.existsByCode("REG001");

        // Then
        assertTrue(result);
        verify(regionRepository).existsByCode("REG001");
    }

    @Test
    void findByProvinceCode_ShouldReturnRegions() {
        // Given
        String provinceCode = "PROV001";
        when(regionRepository.findByProvincesCode(provinceCode))
                .thenReturn(Collections.singletonList(activeRegion));

        // When
        List<Region> result = regionService.findByProvinceCode(provinceCode);

        // Then
        assertEquals(1, result.size());
        assertEquals("Active Region", result.get(0).getName());
        verify(regionRepository).findByProvincesCode(provinceCode);
    }

    @Test
    void findByEventLocationCode_ShouldReturnRegions() {
        // Given
        String locationCode = "LOC001";
        when(regionRepository.findByEventLocationsCode(locationCode))
                .thenReturn(Collections.singletonList(activeRegion));

        // When
        List<Region> result = regionService.findByEventLocationCode(locationCode);

        // Then
        assertEquals(1, result.size());
        assertEquals("Active Region", result.get(0).getName());
        verify(regionRepository).findByEventLocationsCode(locationCode);
    }

    @Test
    void deactivate_ShouldCascadeToLocations() {
        // Given
        when(regionRepository.findById(1L)).thenReturn(Optional.of(activeRegion));
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Region result = regionService.deactivate(1L);

        // Then
        assertEquals(CommonStatus.INACTIVE, result.getStatus());
        
        // Verify all locations are deactivated
        for (EventLocation location : result.getEventLocations()) {
            assertEquals(CommonStatus.INACTIVE, location.getStatus());
        }
        
        verify(regionRepository).findById(1L);
        verify(regionRepository).save(activeRegion);
    }

    @Test
    void findByStatus_ShouldReturnFilteredRegions() {
        // Given
        when(regionRepository.findByStatus(CommonStatus.ACTIVE))
                .thenReturn(Collections.singletonList(activeRegion));

        // When
        List<Region> result = regionService.findByStatus(CommonStatus.ACTIVE);

        // Then
        assertEquals(1, result.size());
        assertEquals("Active Region", result.get(0).getName());
        verify(regionRepository).findByStatus(CommonStatus.ACTIVE);
    }
}
