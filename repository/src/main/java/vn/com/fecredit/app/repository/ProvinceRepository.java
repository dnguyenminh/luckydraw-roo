package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface ProvinceRepository extends SimpleObjectRepository<Province, Long> {

    Optional<Province> findByCode(String code);

    boolean existsByCode(String code);

    List<Province> findByStatus(CommonStatus status);

    // Methods with Entity parameters - using JPQL queries to handle many-to-many relationship
    @Query("SELECT p FROM Province p WHERE :region MEMBER OF p.regions")
    List<Province> findByRegion(Region region);

    @Query("SELECT p FROM Province p WHERE :region MEMBER OF p.regions AND p.status = :status")
    List<Province> findByRegionAndStatus(Region region, CommonStatus status);

    // Replace existsByCodeAndRegion method with a custom JPQL query that uses MEMBER OF operator
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Province p WHERE p.code = :code AND :region MEMBER OF p.regions")
    boolean existsByCodeAndRegion(@Param("code") String code, @Param("region") Region region);

    // Methods with ID parameters
    @Query("SELECT p FROM Province p JOIN p.regions r WHERE r.id = :regionId")
    List<Province> findByRegionId(@Param("regionId") Long regionId);

    @Query("SELECT p FROM Province p JOIN p.regions r WHERE r.id = :regionId AND p.status = :status")
    List<Province> findByRegionIdAndStatus(@Param("regionId") Long regionId, @Param("status") CommonStatus status);

    @Query("SELECT p FROM Province p JOIN p.regions r " +
           "WHERE r.id = :regionId " +
           "AND p.status = 'ACTIVE'")
    List<Province> findActiveProvincesByRegion(@Param("regionId") Long regionId);

    @Query("SELECT COUNT(p) FROM Province p JOIN p.regions r " +
           "WHERE r.id = :regionId " +
           "AND p.status = 'ACTIVE'")
    long countActiveProvincesByRegion(@Param("regionId") Long regionId);

    @Query("SELECT COUNT(pt) FROM Province p " +
           "JOIN p.participants pt " +
           "WHERE p.id = :provinceId " +
           "AND pt.status = 'ACTIVE'")
    long countActiveParticipants(@Param("provinceId") Long provinceId);

    @Query("SELECT p FROM Province p " +
           "WHERE p.status = 'ACTIVE' " +
           "AND EXISTS (SELECT 1 FROM Participant pt " +
           "           WHERE pt.province = p " +
           "           AND pt.status = 'ACTIVE')")
    List<Province> findActiveProvincesWithParticipants();

    @Query("SELECT p FROM Province p JOIN p.regions r WHERE r.code = :regionCode")
    List<Province> findByRegionCode(@Param("regionCode") String regionCode);

    List<Province> findByNameContainingIgnoreCaseAndStatus(@NotBlank(message = "Province name is required") String name, @NotNull CommonStatus status);

    @Query("SELECT COUNT(p) FROM Province p WHERE :region MEMBER OF p.regions")
    long countByRegion(@NotNull(message = "Region is required") Region region);
}
