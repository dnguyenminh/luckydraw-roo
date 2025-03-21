package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.CommonStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantEventRepository extends JpaRepository<ParticipantEvent, Long> {
    
    List<ParticipantEvent> findByEventId(Long eventId);
    
    List<ParticipantEvent> findByEventLocationId(Long locationId);
    
    List<ParticipantEvent> findByParticipantId(Long participantId);
    
    List<ParticipantEvent> findByEventIdAndStatus(Long eventId, CommonStatus status);
    
    Optional<ParticipantEvent> findByEventIdAndParticipantId(Long eventId, Long participantId);
    
    @Query("SELECT pe FROM ParticipantEvent pe " +
           "WHERE pe.eventLocation.id = :locationId " +
           "AND pe.status = 'ACTIVE' " +
           "AND EXISTS (SELECT 1 FROM SpinHistory sh " +
           "           WHERE sh.participantEvent = pe " +
           "           AND sh.spinTime >= :startTime " +
           "           AND sh.spinTime < :endTime)")
    List<ParticipantEvent> findActiveParticipationsByLocationAndTimeRange(
        @Param("locationId") Long locationId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(sh) FROM ParticipantEvent pe " +
           "JOIN pe.spinHistories sh " +
           "WHERE pe.id = :participantEventId " +
           "AND sh.spinTime >= :startTime " +
           "AND sh.spinTime < :endTime")
    long countSpinsInTimeRange(
        @Param("participantEventId") Long participantEventId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
        
    @Query("SELECT CASE WHEN COUNT(pe) > 0 THEN true ELSE false END " +
           "FROM ParticipantEvent pe " +
           "WHERE pe.event.id = :eventId " +
           "AND pe.participant.id = :participantId " +
           "AND pe.status = 'ACTIVE'")
    boolean existsActiveParticipation(
        @Param("eventId") Long eventId,
        @Param("participantId") Long participantId);
}
