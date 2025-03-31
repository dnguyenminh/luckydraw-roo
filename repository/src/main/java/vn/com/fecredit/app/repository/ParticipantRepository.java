package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.Participant;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    
    Optional<Participant> findByCode(String code);
    
    Optional<Participant> findByPhone(String phone);
        
    boolean existsByCode(String code);
            
    List<Participant> findByProvinceId(Long provinceId);
    
    @Query("SELECT p FROM Participant p JOIN p.participantEvents pe WHERE pe.event.id = :eventId")
    List<Participant> findByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT p FROM Participant p JOIN p.participantEvents pe WHERE pe.event.id = :eventId AND p.status = 'ACTIVE' AND pe.status = 'ACTIVE'")
    List<Participant> findActiveParticipantsInEvent(@Param("eventId") Long eventId);
    
    List<Participant> findByStatus(CommonStatus status);
    
    List<Participant> findByEventIdAndCheckedInTrue(Long eventId);
}