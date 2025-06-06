package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.EventLocationKey;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface SpinHistoryRepository extends SimpleObjectRepository<SpinHistory, Long> {

       // Changed to use the participantEvent entity directly instead of its key
       @Query("SELECT sh FROM SpinHistory sh WHERE sh.participantEvent = :participantEvent")
       List<SpinHistory> findByParticipantEventId(@Param("participantEvent") ParticipantEvent participantEvent);

       @Query("SELECT sh FROM SpinHistory sh WHERE sh.status = :status")
       List<SpinHistory> findByStatus(@Param("status") CommonStatus status);

       @Query("SELECT sh FROM SpinHistory sh WHERE sh.participantEvent = :participantEvent " +
                     "AND sh.spinTime BETWEEN :startTime AND :endTime")
       List<SpinHistory> findSpinsInTimeRange(
                     @Param("participantEvent") ParticipantEvent participantEvent,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);

       @Query("SELECT COUNT(sh) FROM SpinHistory sh " +
                     "WHERE sh.participantEvent.eventLocation.id = :locationId " +
                     "AND sh.spinTime BETWEEN :startTime AND :endTime " +
                     "AND sh.win = true AND sh.status = :activeStatus")
       Long countWinningSpinsAtLocation(
                     @Param("locationId") EventLocationKey locationId,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime,
                     @Param("activeStatus") CommonStatus activeStatus);

       @Query("SELECT sh FROM SpinHistory sh " +
                     "WHERE sh.participantEvent.eventLocation.event.id = :eventId " +
                     "AND sh.win = true AND sh.status = :activeStatus")
       List<SpinHistory> findWinningSpinsForEvent(
                     @Param("eventId") Long eventId,
                     @Param("activeStatus") CommonStatus activeStatus);

       // Changed to use the entity directly instead of just the key
       @Query("SELECT COUNT(sh) FROM SpinHistory sh " +
                     "WHERE sh.participantEvent = :participantEvent " +
                     "AND sh.win = true AND sh.status = :activeStatus")
       Long countWinningSpins(
                     @Param("participantEvent") ParticipantEvent participantEvent,
                     @Param("activeStatus") CommonStatus activeStatus);

       // Changed to use the entity directly instead of just the key
       @Query("SELECT sh FROM SpinHistory sh WHERE sh.participantEvent = :participantEvent " +
                     "ORDER BY sh.spinTime DESC")
       List<SpinHistory> findAllByParticipantEventIdOrderBySpinTimeDesc(
                     @Param("participantEvent") ParticipantEvent participantEvent);
}
