package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.service.base.AbstractService;

import java.util.List;
import java.util.Optional;

public interface ParticipantService extends AbstractService<Participant> {
    Optional<Participant> findByCode(String code);
    List<Participant> findByProvinceId(Long provinceId);
    List<Participant> findByEventId(Long eventId);
    List<Participant> findByStatus(CommonStatus status);
    List<Participant> findCheckedInParticipantsByEventId(Long eventId);
}
