package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.CommonStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    Optional<Reward> findByCode(String code);

    boolean existsByCode(String code);

    List<Reward> findByStatus(CommonStatus status);

    @Query("SELECT r FROM Reward r " +
            "WHERE r.eventLocation.id = :locationId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.eventLocation.event.id = :eventId")
    List<Reward> findByEventId(Long eventId);

    List<Reward> findByEventLocationId(Long locationId);

    List<Reward> findByEventLocationIdAndStatus(Long locationId, CommonStatus status);

    @Query("SELECT r FROM Reward r " +
            "WHERE r.eventLocation.id = :locationId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.eventLocation.status = 'ACTIVE' " +
            "AND (r.quantity - (SELECT COUNT(sh) FROM SpinHistory sh " +
            "                   WHERE sh.reward = r " +
            "                   AND sh.status = 'ACTIVE' " +
            "                   AND sh.win = true)) > 0")
    List<Reward> findAvailableRewardsForLocation(@Param("locationId") Long locationId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reward r " +
            "WHERE r.id = :rewardId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.eventLocation.status = 'ACTIVE' " +
            "AND (r.quantity - (SELECT COUNT(sh) FROM SpinHistory sh " +
            "                   WHERE sh.reward = r " +
            "                   AND sh.status = 'ACTIVE' " +
            "                   AND sh.win = true)) > 0")
    boolean isRewardAvailable(@Param("rewardId") Long rewardId);

    @Query("SELECT r.quantity - (SELECT COUNT(sh) FROM SpinHistory sh " +
            "                     WHERE sh.reward = r " +
            "                     AND sh.status = 'ACTIVE' " +
            "                     AND sh.win = true) " +
            "FROM Reward r " +
            "WHERE r.id = :rewardId")
    int getRemainingQuantity(@Param("rewardId") Long rewardId);

    @Query("SELECT r FROM Reward r " +
            "WHERE r.eventLocation.id = :locationId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.quantity > 0")
    List<Reward> findAvailableRewardsByLocation(@Param("locationId") Long locationId);

    @Query("SELECT COALESCE(MAX(r.value), 0) FROM Reward r " +
            "WHERE r.eventLocation.id = :locationId " +
            "AND r.status = 'ACTIVE'")
    BigDecimal getMaximumRewardValueAtLocation(@Param("locationId") Long locationId);
}