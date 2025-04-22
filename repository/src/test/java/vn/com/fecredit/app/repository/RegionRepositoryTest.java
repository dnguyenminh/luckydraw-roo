package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class RegionRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RegionRepository regionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Region activeRegion1;
    private Region activeRegion2;
    private Region inactiveRegion;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM event_locations").executeUpdate(); 
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        activeRegion1 = Region.builder()
                .code("NR")
                .name("North Region")
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        activeRegion1.setVersion(0L);
        activeRegion1.setCreatedBy("test-user");
        activeRegion1.setUpdatedBy("test-user");
        activeRegion1.setCreatedAt(now);
        activeRegion1.setUpdatedAt(now);
        activeRegion1 = regionRepository.save(activeRegion1);

        activeRegion2 = Region.builder()
                .code("SR")
                .name("South Region")
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        activeRegion2.setVersion(0L);
        activeRegion2.setCreatedBy("test-user");
        activeRegion2.setUpdatedBy("test-user");
        activeRegion2.setCreatedAt(now);
        activeRegion2.setUpdatedAt(now);
        activeRegion2 = regionRepository.save(activeRegion2);

        inactiveRegion = Region.builder()
                .code("ER")
                .name("East Region")
                .status(CommonStatus.INACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        inactiveRegion.setVersion(0L);
        inactiveRegion.setCreatedBy("test-user");
        inactiveRegion.setUpdatedBy("test-user");
        inactiveRegion.setCreatedAt(now);
        inactiveRegion.setUpdatedAt(now);
        inactiveRegion = regionRepository.save(inactiveRegion);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByCode_ShouldReturnRegion_WhenExists() {
        Optional<Region> result = regionRepository.findByCode("NR");
        
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("North Region");
    }

    @Test
    void findByCode_ShouldReturnEmpty_WhenNotExists() {
        Optional<Region> result = regionRepository.findByCode("XX");
        
        assertThat(result).isEmpty();
    }

    @Test
    void findByStatus_ShouldReturnFilteredRegions() {
        List<Region> activeRegions = regionRepository.findByStatus(CommonStatus.ACTIVE);
        List<Region> inactiveRegions = regionRepository.findByStatus(CommonStatus.INACTIVE);
        
        assertThat(activeRegions).hasSize(2);
        assertThat(activeRegions).extracting("code").containsExactlyInAnyOrder("NR", "SR");
        
        assertThat(inactiveRegions).hasSize(1);
        assertThat(inactiveRegions).extracting("code").containsExactly("ER");
    }
    
    @Test
    void findByNameContaining_ShouldFindMatchingRegions() {
        List<Region> regionsWithRegion = regionRepository.findByNameContainingIgnoreCase("Region");
        List<Region> northRegions = regionRepository.findByNameContainingIgnoreCase("North");
        
        assertThat(regionsWithRegion).hasSize(3);
        assertThat(northRegions).hasSize(1);
        assertThat(northRegions.get(0).getCode()).isEqualTo("NR");
    }

    @Test
    void findAll_WithPagination_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 2);
        var page = regionRepository.findAll(pageable);
        
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void save_ShouldPersistNewRegion() {
        // Create a new region with all required fields
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST-REG")
            .status(CommonStatus.ACTIVE)
            .provinces(new HashSet<>())
            .eventLocations(new HashSet<>())
            .build();
            
        // Set the required audit fields
        LocalDateTime now = LocalDateTime.now();
        region.setCreatedAt(now);
        region.setUpdatedAt(now);
        region.setCreatedBy("test-user");
        region.setUpdatedBy("test-user");
        region.setVersion(0L);
        
        // Save and verify
        Region savedRegion = regionRepository.save(region);
        
        assertThat(savedRegion.getId()).isNotNull();
        assertThat(savedRegion.getName()).isEqualTo("Test Region");
    }

    @Test
    void update_ShouldUpdateExistingRegion() {
        activeRegion1.setName("Updated North Region");
        regionRepository.save(activeRegion1);
        entityManager.flush();
        entityManager.clear();
        
        Region updated = regionRepository.findById(activeRegion1.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated North Region");
    }
}