package vn.com.fecredit.app.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.repository.config.TestConfig;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional // Add this annotation to ensure all test methods run in a transaction
public class RegionRepositoryTest {

    @Autowired
    private RegionRepository regionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Region northRegion;
    private Region southRegion;
    private Region inactiveRegion;
    private Province hanoi;
    private Province hcmc;

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
        LocalDateTime now = LocalDateTime.now();

        // Create regions first
        northRegion = createRegion("NORTH", "Northern Region", CommonStatus.ACTIVE);
        southRegion = createRegion("SOUTH", "Southern Region", CommonStatus.ACTIVE);
        inactiveRegion = createRegion("INACTIVE", "Inactive Region", CommonStatus.INACTIVE);

        entityManager.persist(northRegion);
        entityManager.persist(southRegion);
        entityManager.persist(inactiveRegion);
        entityManager.flush();

        // Create provinces
        hanoi = Province.builder()
            .code("HN")
            .name("Ha Noi")
            .region(northRegion)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();

        hcmc = Province.builder()
            .code("HCM")
            .name("Ho Chi Minh City")
            .region(southRegion)
            .status(CommonStatus.ACTIVE)
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();

        // Persist provinces
        entityManager.persist(hanoi);
        entityManager.persist(hcmc);

        // Update bidirectional relationships
        northRegion.getProvinces().add(hanoi);
        southRegion.getProvinces().add(hcmc);

        entityManager.flush();
        entityManager.clear();
    }

    private Region createRegion(String code, String name, CommonStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return Region.builder()
            .code(code)
            .name(name)
            .status(status)
            .provinces(new HashSet<>())
            .eventLocations(new HashSet<>())
            .version(0L)
            .createdAt(now)
            .updatedAt(now)
            .createdBy("test-user")
            .updatedBy("test-user")
            .build();
    }

    @Test
    void findByCode_ShouldReturnRegion_WhenExists() {
        var result = regionRepository.findByCode("NORTH");
        
        assertThat(result)
            .isPresent()
            .hasValueSatisfying(region -> {
                assertThat(region.getName()).isEqualTo("Northern Region");
                assertThat(region.getStatus()).isEqualTo(CommonStatus.ACTIVE);
            });
    }

    @Test
    void findByCode_ShouldReturnEmpty_WhenNotExists() {
        var result = regionRepository.findByCode("NONEXISTENT");
        assertThat(result).isEmpty();
    }

    @Test
    void findByStatus_ShouldReturnFilteredRegions() {
        var activeRegions = regionRepository.findByStatus(CommonStatus.ACTIVE);
        assertThat(activeRegions)
            .hasSize(2)
            .extracting("code")
            .containsExactlyInAnyOrder("NORTH", "SOUTH");

        var inactiveRegions = regionRepository.findByStatus(CommonStatus.INACTIVE);
        assertThat(inactiveRegions)
            .hasSize(1)
            .extracting("code")
            .containsExactly("INACTIVE");
    }

    @Test
    void findActiveRegionsWithProvinces_ShouldFetchProvinces() {
        List<Region> regions = regionRepository.findActiveRegionsWithProvinces();

        assertThat(regions)
            .hasSize(2)
            .extracting("code")
            .containsExactlyInAnyOrder("NORTH", "SOUTH");

        // Verify eager loading of provinces
        regions.forEach(region -> {
            assertThat(entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .isLoaded(region, "provinces")).isTrue();
        });

        // Verify provinces are correctly associated
        var northResult = regions.stream()
            .filter(r -> r.getCode().equals("NORTH"))
            .findFirst()
            .orElseThrow();
        assertThat(northResult.getProvinces())
            .extracting("code")
            .containsExactly("HN");

        var southResult = regions.stream()
            .filter(r -> r.getCode().equals("SOUTH"))
            .findFirst()
            .orElseThrow();
        assertThat(southResult.getProvinces())
            .extracting("code")
            .containsExactly("HCM");
    }

    @Test
    void hasProvinceWithCode_ShouldCheckProvinceExistence() {
        assertThat(regionRepository.hasProvinceWithCode(
            northRegion.getId(), "HN")).isTrue();
        assertThat(regionRepository.hasProvinceWithCode(
            northRegion.getId(), "HCM")).isFalse();
        assertThat(regionRepository.hasProvinceWithCode(
            southRegion.getId(), "HCM")).isTrue();
        assertThat(regionRepository.hasProvinceWithCode(
            southRegion.getId(), "HN")).isFalse();
    }
}