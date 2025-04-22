package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface ParticipantRepository extends SimpleObjectRepository<Participant> {

    Optional<Participant> findByCode(String code);

    Optional<Participant> findByPhone(String phone);

    boolean existsByCode(String code);

    List<Participant> findByProvinceId(Long provinceId);

    @Query("SELECT p FROM Participant p JOIN p.participantEvents pe WHERE pe.eventLocation.event.id = :eventId")
    List<Participant> findByEventId(@Param("eventId") Long eventId);

    /**
     * Find active participants in an event
     * @param eventId the event ID
     * @return list of active participants in the event
     */
    @Query("SELECT p FROM Participant p JOIN p.participantEvents pe WHERE pe.eventLocation.event.id = :eventId AND p.status = 'ACTIVE' AND pe.status = 'ACTIVE'")
    List<Participant> findActiveParticipantsInEvent(@Param("eventId") Long eventId);

    List<Participant> findByStatus(CommonStatus status);
}
