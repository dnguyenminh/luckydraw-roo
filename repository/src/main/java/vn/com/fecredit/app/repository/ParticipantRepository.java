package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.CommonStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    
    Optional<Participant> findByCode(String code);
    
    List<Participant> findByStatus(CommonStatus status);
    
    boolean existsByCode(String code);
    
    @Query("SELECT p FROM Participant p " +
           "WHERE p.status = 'ACTIVE' " +
           "AND EXISTS (SELECT pe FROM ParticipantEvent pe " +
           "           WHERE pe.participant = p " +
           "           AND pe.event.id = :eventId " +
           "           AND pe.status = 'ACTIVE')")
    List<Participant> findActiveParticipantsInEvent(@Param("eventId") Long eventId);
}