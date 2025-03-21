package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.CommonStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    Optional<Event> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Event> findByStatus(CommonStatus status);
    
    List<Event> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT e FROM Event e " +
           "WHERE e.status = 'ACTIVE' " +
           "AND e.startTime <= :currentTime " +
           "AND e.endTime > :currentTime")
    List<Event> findActiveEvents(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e " +
           "WHERE e.id != :eventId " +
           "AND e.status = 'ACTIVE' " +
           "AND e.startTime < :endTime " +
           "AND e.endTime > :startTime")
    boolean hasOverlappingEvents(@Param("eventId") Long eventId, 
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
}
