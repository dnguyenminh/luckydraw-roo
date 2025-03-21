package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.CommonStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {

    List<SpinHistory> findByParticipantEventId(Long participantEventId);
    
    List<SpinHistory> findByParticipantEventIdAndStatus(Long participantEventId, CommonStatus status);
    
    List<SpinHistory> findByRewardId(Long rewardId);
    
    List<SpinHistory> findByGoldenHourId(Long goldenHourId);
    
    @Query("SELECT sh FROM SpinHistory sh " +
           "WHERE sh.participantEvent.id = :participantEventId " +
           "AND sh.spinTime >= :startTime " +
           "AND sh.spinTime < :endTime " +
           "AND sh.status = 'ACTIVE' " +
           "ORDER BY sh.spinTime")
    List<SpinHistory> findSpinsInTimeRange(
        @Param("participantEventId") Long participantEventId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COALESCE(SUM(CASE WHEN sh.win = true " +
           "THEN sh.reward.value * sh.multiplier ELSE 0 END), 0) " +
           "FROM SpinHistory sh " +
           "WHERE sh.participantEvent.id = :participantEventId " +
           "AND sh.status = 'ACTIVE'")
    BigDecimal calculateTotalWinnings(@Param("participantEventId") Long participantEventId);
    
    @Query("SELECT COUNT(sh) FROM SpinHistory sh " +
           "WHERE sh.participantEvent.eventLocation.id = :locationId " +
           "AND sh.win = true " +
           "AND sh.spinTime >= :startTime " +
           "AND sh.spinTime < :endTime " +
           "AND sh.status = 'ACTIVE'")
    long countWinningSpinsAtLocation(
        @Param("locationId") Long locationId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT sh FROM SpinHistory sh " +
           "WHERE sh.participantEvent.event.id = :eventId " +
           "AND sh.win = true " +
           "AND sh.status = 'ACTIVE' " +
           "ORDER BY sh.spinTime DESC")
    List<SpinHistory> findWinningSpinsForEvent(@Param("eventId") Long eventId);
}