package vn.com.fecredit.app.service;

import java.util.List;
import java.util.Optional;

import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.service.base.AbstractService;

public interface ParticipantEventService extends AbstractService<ParticipantEvent> {
    List<ParticipantEvent> findByEventId(Long eventId);
    List<ParticipantEvent> findByEventLocationId(Long locationId);
    List<ParticipantEvent> findByParticipantId(Long participantId); 
    List<ParticipantEvent> findByEventIdAndStatus(Long eventId, CommonStatus status);
    Optional<ParticipantEvent> findByEventIdAndParticipantId(Long eventId, Long participantId);
    boolean existsActiveParticipation(Long eventId, Long participantId);
}
