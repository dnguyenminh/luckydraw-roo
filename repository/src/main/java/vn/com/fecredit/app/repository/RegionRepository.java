package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface RegionRepository extends SimpleObjectRepository<Region, Long> {

    Optional<Region> findByCode(String code);

    boolean existsByCode(String code);

    List<Region> findByStatus(CommonStatus status);

    @Query("SELECT DISTINCT r FROM Region r " +
            "LEFT JOIN FETCH r.provinces p " +
            "WHERE r.status = 'ACTIVE' " +
            "AND EXISTS (SELECT 1 FROM Province p2 " +
            "           WHERE r MEMBER OF p2.regions)")
    List<Region> findActiveRegionsWithProvinces();

    @Query("SELECT r FROM Region r " +
            "WHERE r.status = 'ACTIVE' " +
            "AND EXISTS (SELECT 1 FROM EventLocation el " +
            "           WHERE el.region = r " +
            "           AND el.status = 'ACTIVE')")
    List<Region> findActiveRegionsWithActiveLocations();

    @Query("SELECT COUNT(p) FROM Province p JOIN p.regions r WHERE r.id = :regionId AND p.status = 'ACTIVE'")
    long countActiveProvinces(@Param("regionId") Long regionId);

    @Query("SELECT COUNT(el) FROM EventLocation el " +
            "WHERE el.region.id = :regionId " +
            "AND el.status = 'ACTIVE'")
    long countActiveEventLocations(@Param("regionId") Long regionId);

    @Query("SELECT CASE WHEN COUNT(p1) > 0 THEN true ELSE false END " +
            "FROM Region r1 JOIN r1.provinces p1 " +
            "WHERE r1.id = :regionId1 " +
            "AND EXISTS (" +
            "    SELECT 1 FROM Region r2 JOIN r2.provinces p2 " +
            "    WHERE r2.id = :regionId2 " +
            "    AND p1 = p2)")
    boolean hasOverlappingProvinces(
            @Param("regionId1") Long regionId1,
            @Param("regionId2") Long regionId2);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Region r JOIN r.provinces p " +
            "WHERE r.id = :regionId " +
            "AND p.code = :provinceCode")
    boolean hasProvinceWithCode(
            @Param("regionId") Long regionId,
            @Param("provinceCode") String provinceCode);

    @Query("SELECT r FROM Region r JOIN r.provinces p WHERE p.code = :provinceCode")
    List<Region> findByProvincesCode(@Param("provinceCode") String provinceCode);

    List<Region> findByNameContainingIgnoreCase(@NotBlank(message = "Region name is required") String name);
}
