package vn.com.fecredit.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.ParticipantEventKey;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface ParticipantEventRepository extends ComplexObjectRepository<ParticipantEvent, ParticipantEventKey> {

    /**
     * Find all participant events by participant ID
     *
     * @param participantId the participant ID
     * @return list of participant events
     */
    @Query("SELECT pe FROM ParticipantEvent pe WHERE pe.participant.id = :participantId")
    List<ParticipantEvent> findByParticipantId(@Param("participantId") Long participantId);

    /**
     * Find all participant events by event location's event ID
     *
     * @param eventId the event ID
     * @return list of participant events
     */
    @Query("SELECT pe FROM ParticipantEvent pe WHERE pe.eventLocation.event.id = :eventId")
    List<ParticipantEvent> findByEventLocationId(@Param("eventId") Long eventId);

    /**
     * Find all participant events by participant ID and status
     *
     * @param participantId the participant ID
     * @param status the status to filter
     * @return list of participant events
     */
    @Query("SELECT pe FROM ParticipantEvent pe WHERE pe.participant.id = :participantId AND pe.status = :status")
    List<ParticipantEvent> findByParticipantIdAndStatus(
            @Param("participantId") Long participantId,
            @Param("status") CommonStatus status);

    /**
     * Find all participant events by participant ID and with spins remaining above threshold
     *
     * @param participantId the participant ID
     * @param minSpins the minimum spins remaining
     * @return list of participant events
     */
    @Query("SELECT pe FROM ParticipantEvent pe WHERE pe.participant.id = :participantId AND pe.spinsRemaining > :minSpins")
    List<ParticipantEvent> findByParticipantIdAndSpinsRemainingGreaterThan(
            @Param("participantId") Long participantId,
            @Param("minSpins") int minSpins);
}
