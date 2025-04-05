package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

public class ProvinceRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ProvinceRepository provinceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Region northRegion;
    private Region southRegion;
    private Province hanoi;
    private Province bacNinh;
    private Province hcmc;
    private Province inactiveProvince;

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
        // Create regions first
        northRegion = createRegion("NORTH", "Northern Region");
        southRegion = createRegion("SOUTH", "Southern Region");

        entityManager.persist(northRegion);
        entityManager.persist(southRegion);
        entityManager.flush();

        // Create provinces
        hanoi = createProvince("HN", "Ha Noi", northRegion, CommonStatus.ACTIVE);
        bacNinh = createProvince("BN", "Bac Ninh", northRegion, CommonStatus.ACTIVE);
        hcmc = createProvince("HCM", "Ho Chi Minh City", southRegion, CommonStatus.ACTIVE);
        inactiveProvince = createProvince("IP", "Inactive Province", northRegion, CommonStatus.INACTIVE);

        // Persist provinces
        entityManager.persist(hanoi);
        entityManager.persist(bacNinh);
        entityManager.persist(hcmc);
        entityManager.persist(inactiveProvince);

        // Update bidirectional relationships
        northRegion.getProvinces().add(hanoi);
        northRegion.getProvinces().add(bacNinh);
        northRegion.getProvinces().add(inactiveProvince);
        southRegion.getProvinces().add(hcmc);

        entityManager.flush();
        entityManager.clear();
    }

    private Region createRegion(String code, String name) {
        LocalDateTime now = LocalDateTime.now();
        return Region.builder()
                .code(code)
                .name(name)
                .status(CommonStatus.ACTIVE)
                .provinces(new HashSet<>())
                .eventLocations(new HashSet<>())
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();
    }

    private Province createProvince(String code, String name, Region region, CommonStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return Province.builder()
                .code(code)
                .name(name)
                .region(region)
                .status(status)
                .version(0L)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("test-user")
                .updatedBy("test-user")
                .build();
    }

    @Test
    void findByCode_ShouldReturnProvince_WhenExists() {
        var result = provinceRepository.findByCode("HN");

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(province -> {
                    assertThat(province.getName()).isEqualTo("Ha Noi");
                    assertThat(province.getStatus()).isEqualTo(CommonStatus.ACTIVE);
                    assertThat(province.getRegion().getCode()).isEqualTo("NORTH");
                });
    }

    @Test
    void findByCode_ShouldReturnEmpty_WhenNotExists() {
        var result = provinceRepository.findByCode("NONEXISTENT");
        assertThat(result).isEmpty();
    }

    @Test
    void findByRegion_ShouldReturnAllProvincesInRegion() {
        var northProvinces = provinceRepository.findByRegion(northRegion);
        assertThat(northProvinces)
                .hasSize(3)
                .extracting("code")
                .containsExactlyInAnyOrder("HN", "BN", "IP");

        var southProvinces = provinceRepository.findByRegion(southRegion);
        assertThat(southProvinces)
                .hasSize(1)
                .extracting("code")
                .containsExactly("HCM");
    }

    @Test
    void findByRegionAndStatus_ShouldReturnFilteredProvinces() {
        var activeNorthProvinces = provinceRepository.findByRegionAndStatus(
                northRegion, CommonStatus.ACTIVE);
        assertThat(activeNorthProvinces)
                .hasSize(2)
                .extracting("code")
                .containsExactlyInAnyOrder("HN", "BN");

        var inactiveNorthProvinces = provinceRepository.findByRegionAndStatus(
                northRegion, CommonStatus.INACTIVE);
        assertThat(inactiveNorthProvinces)
                .hasSize(1)
                .extracting("code")
                .containsExactly("IP");
    }

    @Test
    void findActiveProvincesByRegion_ShouldReturnOnlyActiveProvinces() {
        var activeNorthProvinces = provinceRepository.findActiveProvincesByRegion(northRegion.getId());
        assertThat(activeNorthProvinces)
                .hasSize(2)
                .extracting("code")
                .containsExactlyInAnyOrder("HN", "BN");

        var activeSouthProvinces = provinceRepository.findActiveProvincesByRegion(southRegion.getId());
        assertThat(activeSouthProvinces)
                .hasSize(1)
                .extracting("code")
                .containsExactly("HCM");
    }

    @Test
    void existsByCodeAndRegion_ShouldCheckProvinceExistence() {
        assertThat(provinceRepository.existsByCodeAndRegion("HN", northRegion)).isTrue();
        assertThat(provinceRepository.existsByCodeAndRegion("HN", southRegion)).isFalse();
        assertThat(provinceRepository.existsByCodeAndRegion("HCM", southRegion)).isTrue();
        assertThat(provinceRepository.existsByCodeAndRegion("NONEXISTENT", northRegion)).isFalse();
    }
}