package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.service.ParticipantService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional 
public class ParticipantServiceImpl extends AbstractServiceImpl<Participant> implements ParticipantService {

    private final ParticipantRepository participantRepository;

    public ParticipantServiceImpl(ParticipantRepository participantRepository) {
        super(participantRepository);
        this.participantRepository = participantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participant> findByStatus(CommonStatus status) {
        return participantRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Participant> findByCode(String code) {
        return participantRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participant> findByProvinceId(Long provinceId) {
        return participantRepository.findByProvinceId(provinceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participant> findByEventId(Long eventId) {
        return participantRepository.findByEventId(eventId);
    }
}
