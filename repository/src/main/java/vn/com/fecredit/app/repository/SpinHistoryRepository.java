package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface SpinHistoryRepository extends SimpleObjectRepository<SpinHistory> {

       List<SpinHistory> findByParticipantEventId(Long participantEventId);

       List<SpinHistory> findByParticipantEventIdAndStatus(Long participantEventId, CommonStatus status);

       List<SpinHistory> findByRewardId(Long rewardId);

       List<SpinHistory> findByGoldenHourId(Long goldenHourId);

       List<SpinHistory> findByStatus(CommonStatus status);

       @Query("SELECT sh FROM SpinHistory sh WHERE sh.participantEvent.id = :participantEventId " +
                     "AND sh.spinTime BETWEEN :startTime AND :endTime")
       List<SpinHistory> findSpinsInTimeRange(
                     @Param("participantEventId") Long participantEventId,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);

       @Query("SELECT COUNT(sh) FROM SpinHistory sh " +
                     "WHERE sh.participantEvent.eventLocation.id = :locationId " +
                     "AND sh.spinTime BETWEEN :startTime AND :endTime " +
                     "AND sh.win = true AND sh.status = 'ACTIVE'")
       Long countWinningSpinsAtLocation(
                     @Param("locationId") Long locationId,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);

       @Query("SELECT sh FROM SpinHistory sh " +
                     "WHERE sh.participantEvent.event.id = :eventId " +
                     "AND sh.win = true AND sh.status = 'ACTIVE'")
       List<SpinHistory> findWinningSpinsForEvent(@Param("eventId") Long eventId);

       // Add method to check win count
       @Query("SELECT COUNT(sh) FROM SpinHistory sh " +
                     "WHERE sh.participantEvent.id = :participantEventId " +
                     "AND sh.win = true AND sh.status = 'ACTIVE'")
       Long countWinningSpins(@Param("participantEventId") Long participantEventId);

       // Add method to find all by participant with sorting
       @Query("SELECT sh FROM SpinHistory sh WHERE sh.participantEvent.id = :participantEventId " +
                     "ORDER BY sh.spinTime DESC")
       List<SpinHistory> findAllByParticipantEventIdOrderBySpinTimeDesc(
                     @Param("participantEventId") Long participantEventId);
}