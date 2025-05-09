package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface EventRepository extends SimpleObjectRepository<Event, Long> {

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

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' " +
        "AND e.startTime <= :currentTime AND e.endTime > :currentTime ORDER BY e.startTime")
    List<Event> findCurrentEvents(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' " +
        "AND e.startTime > :currentTime ORDER BY e.startTime")
    List<Event> findUpcomingEvents(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT e FROM Event e WHERE e.status = :status " +
        "AND e.endTime < :now")
    List<Event> findPastEvents(@Param("now") LocalDateTime now);

    Optional<Event> findByCodeAndStatus(@NotBlank(message = "Event code is required") String code, @NotNull CommonStatus status);

    List<Event> findByNameContainingIgnoreCase(@NotBlank(message = "Event name is required") String name);
}
