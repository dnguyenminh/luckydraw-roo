package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Repository interface for ParticipantEvent entities.
 */
@Repository
public interface ParticipantEventRepository extends SimpleObjectRepository<ParticipantEvent> {

    /**
     * Find participant event by participant and event
     * 
     * @param participant the participant
     * @param event       the event
     * @return the optional participant event
     */
    Optional<ParticipantEvent> findByParticipantAndEvent(Participant participant, Event event);

    /**
     * Find participant events by event ID
     * 
     * @param eventId the event ID
     * @return list of participant events
     */
    List<ParticipantEvent> findByEventId(Long eventId);

    /**
     * Find participant events by event
     * 
     * @param event the event
     * @return list of participant events
     */
    List<ParticipantEvent> findByEvent(Event event);

    /**
     * Count participant events by event
     * 
     * @param event the event
     * @return count of participant events
     */
    long countByEvent(Event event);

    /**
     * Count participant events by event and status
     * 
     * @param event  the event
     * @param status the status
     * @return count of participant events
     */
    long countByEventAndStatus(Event event, CommonStatus status);

    /**
     * Find participant events by event and status
     * 
     * @param event  the event
     * @param status the status
     * @return list of participant events
     */
    List<ParticipantEvent> findByEventAndStatus(Event event, CommonStatus status);

    /**
     * Find participant events by event location ID
     * 
     * @param eventLocationId the event location ID
     * @return list of participant events
     */
    List<ParticipantEvent> findByEventLocationId(Long eventLocationId);

    /**
     * Find participant events by participant ID
     * 
     * @param participantId the participant ID
     * @return list of participant events
     */
    List<ParticipantEvent> findByParticipantId(Long participantId);

    /**
     * Find participant events by event ID and status
     * 
     * @param eventId the event ID
     * @param status  the status
     * @return list of participant events
     */
    List<ParticipantEvent> findByEventIdAndStatus(Long eventId, CommonStatus status);

    /**
     * Find participant event by event ID and participant ID
     * 
     * @param eventId       the event ID
     * @param participantId the participant ID
     * @return list of participant events
     */
    List<ParticipantEvent> findByEventIdAndParticipantId(Long eventId, Long participantId);

    /**
     * Find participant events by participant ID and event ID
     * 
     * @param participantId the participant ID
     * @param eventId       the event ID
     * @return list of participant events
     */
    List<ParticipantEvent> findByParticipantIdAndEventId(Long participantId, Long eventId);

    /**
     * Check if an active participation exists
     * 
     * @param eventId       the event ID
     * @param participantId the participant ID
     * @return true if active participation exists
     */
    @Query("SELECT COUNT(pe) > 0 FROM ParticipantEvent pe WHERE pe.event.id = :eventId AND pe.participant.id = :participantId AND pe.status = 'ACTIVE'")
    boolean existsActiveParticipation(@Param("eventId") Long eventId, @Param("participantId") Long participantId);

    /**
     * Find participant events by status
     * 
     * @param status the status
     * @return list of participant events
     */
    List<ParticipantEvent> findByStatus(CommonStatus status);
}
