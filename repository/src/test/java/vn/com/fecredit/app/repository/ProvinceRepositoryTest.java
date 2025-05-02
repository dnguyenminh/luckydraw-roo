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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

class ProvinceRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private RegionRepository regionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalDateTime now = LocalDateTime.now();
    private Region region1, region2;
    private Province province1, province2, province3;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestData();
    }

    private void cleanDatabase() {
        entityManager.createNativeQuery("DELETE FROM provinces").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM regions").executeUpdate();
        entityManager.flush();
    }

    private void createTestData() {
        // Create Regions with builder pattern
        region1 = Region.builder()
                .code("NR")
                .name("North Region")
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        region1.setVersion(0L);
        region1.setCreatedBy("test-user");
        region1.setUpdatedBy("test-user");
        region1.setCreatedAt(now);
        region1.setUpdatedAt(now);
        region1 = regionRepository.save(region1);

        region2 = Region.builder()
                .code("SR")
                .name("South Region")
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .build();
        region2.setVersion(0L);
        region2.setCreatedBy("test-user");
        region2.setUpdatedBy("test-user");
        region2.setCreatedAt(now);
        region2.setUpdatedAt(now);
        region2 = regionRepository.save(region2);
        // Create Provinces with builder pattern
        province1 = Province.builder()
                .code("P1")
                .name("Province 1")
                .description("Northern Province")
                .regions(new HashSet<>() {
                    {
                        add(region1);
                    }
                })
                .status(CommonStatus.ACTIVE)
                .participants(new HashSet<>())
                .build();
        province1.setVersion(0L);
        province1.setCreatedBy("test-user");
        province1.setUpdatedBy("test-user");
        province1.setCreatedAt(now);
        province1.setUpdatedAt(now);
        province1 = provinceRepository.save(province1);
        region1.getProvinces().add(province1); // Maintain bidirectional relationship
        regionRepository.save(region1);
        
        province2 = Province.builder()
                .code("P2")
                .name("Province 2")
                .description("Southern Province")
                .regions(new HashSet<>() {
                    {
                        add(region2);
                    }
                })
                .status(CommonStatus.ACTIVE)
                .participants(new HashSet<>())
                .build();
        province2.setVersion(0L);
        province2.setCreatedBy("test-user");
        province2.setUpdatedBy("test-user");
        province2.setCreatedAt(now);
        province2.setUpdatedAt(now);
        province2 = provinceRepository.save(province2);
        region2.getProvinces().add(province2); // Maintain bidirectional relationship
        regionRepository.save(region2);

        province3 = Province.builder()
                .code("P3")
                .name("Province 3")
                .description("Northern Inactive Province")
                .regions(new HashSet<>() {
                    {
                        add(region1);
                    }
                })
                .status(CommonStatus.INACTIVE)
                .participants(new HashSet<>())
                .build();
        province3.setVersion(0L);
        province3.setCreatedBy("test-user");
        province3.setUpdatedBy("test-user");
        province3.setCreatedAt(now);
        province3.setUpdatedAt(now);
        province3 = provinceRepository.save(province3);
        region1.getProvinces().add(province3); // Maintain bidirectional relationship
        regionRepository.save(region1);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByCode_ShouldReturnProvince_WhenExists() {
        Optional<Province> result = provinceRepository.findByCode("P1");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Province 1");
    }

    @Test
    void findByRegion_ShouldReturnProvincesInRegion() {
        List<Province> northProvinces = provinceRepository.findByRegion(region1);

        assertThat(northProvinces).hasSize(2);
        assertThat(northProvinces).extracting("code").containsExactlyInAnyOrder("P1", "P3");
    }

    @Test
    void findByRegionAndStatus_ShouldFilterByBoth() {
        List<Province> activeNorthProvinces = provinceRepository.findByRegionAndStatus(region1, CommonStatus.ACTIVE);

        assertThat(activeNorthProvinces).hasSize(1);
        assertThat(activeNorthProvinces.get(0).getCode()).isEqualTo("P1");
    }

    @Test
    void findByNameContainingAndStatus_ShouldFilterByBoth() {
        List<Province> activeProvincesWithName =
                provinceRepository.findByNameContainingIgnoreCaseAndStatus("Province", CommonStatus.ACTIVE);

        assertThat(activeProvincesWithName).hasSize(2);
        assertThat(activeProvincesWithName).extracting("code").containsExactlyInAnyOrder("P1", "P2");
    }

    @Test
    void countByRegionId_ShouldReturnCorrectCount() {
        long count = provinceRepository.countByRegion(region1);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findWithPagination_ShouldReturnPagedResults() {
        var page = provinceRepository.findAll(PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void save_ShouldPersistNewProvince() {
        // Create a region to associate with the province
        Region region = createTestRegion();
        entityManager.persist(region);
        entityManager.flush();

        // Create a new province with all required fields
        Province province = Province.builder()
            .name("Test Province")
            .code("TEST-PROV")
            .description("Test Province Description")
            .regions(new HashSet<>() {
                {
                    add(region);
                }
            })
            .status(CommonStatus.ACTIVE)
            .build();
            
        // Set the required audit fields
        LocalDateTime now = LocalDateTime.now();
        province.setCreatedAt(now);
        province.setUpdatedAt(now);
        province.setCreatedBy("test-user");
        province.setUpdatedBy("test-user");
        province.setVersion(0L);
        
        // Save and verify
        Province savedProvince = provinceRepository.save(province);
        
        assertThat(savedProvince.getId()).isNotNull();
        assertThat(savedProvince.getName()).isEqualTo("Test Province");
        assertThat(savedProvince.getRegions()).contains(region);
    }

    private Region createTestRegion() {
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST-REG")
            .status(CommonStatus.ACTIVE)
            .build();
            
        // Set audit fields for region too
        LocalDateTime now = LocalDateTime.now();
        region.setCreatedAt(now);
        region.setUpdatedAt(now);
        region.setCreatedBy("test-user");
        region.setUpdatedBy("test-user");
        region.setVersion(0L);
        
        return region;
    }

    @Test
    void update_ShouldUpdateExistingProvince() {
        province1.setName("Updated Province Name");
        provinceRepository.save(province1);
        entityManager.flush();
        entityManager.clear();

        Province updated = provinceRepository.findById(province1.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Province Name");
    }
}
