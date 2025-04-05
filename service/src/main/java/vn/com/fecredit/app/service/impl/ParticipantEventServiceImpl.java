package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.ParticipantEventRepository;
import vn.com.fecredit.app.service.ParticipantEventService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

@Slf4j
@Service
@Transactional
public class ParticipantEventServiceImpl extends AbstractServiceImpl<ParticipantEvent> implements ParticipantEventService {

    private final ParticipantEventRepository participantEventRepository;

    public ParticipantEventServiceImpl(ParticipantEventRepository participantEventRepository) {
        super(participantEventRepository);
        this.participantEventRepository = participantEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantEvent> findByEventId(Long eventId) {
        return participantEventRepository.findByEventId(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantEvent> findByEventLocationId(Long locationId) {
        return participantEventRepository.findByEventLocationId(locationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantEvent> findByParticipantId(Long participantId) {
        return participantEventRepository.findByParticipantId(participantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantEvent> findByEventIdAndStatus(Long eventId, CommonStatus status) {
        return participantEventRepository.findByEventIdAndStatus(eventId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ParticipantEvent> findByEventIdAndParticipantId(Long eventId, Long participantId) {
        return participantEventRepository.findByParticipantIdAndEventId(participantId, eventId)
            .stream()
            .findFirst(); // Convert List to Optional
    }

    @Override
    @Transactional(readOnly = true) 
    public boolean existsActiveParticipation(Long eventId, Long participantId) {
        return participantEventRepository.existsActiveParticipation(eventId, participantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantEvent> findByStatus(CommonStatus status) {
        return participantEventRepository.findByStatus(status);
    }
}
