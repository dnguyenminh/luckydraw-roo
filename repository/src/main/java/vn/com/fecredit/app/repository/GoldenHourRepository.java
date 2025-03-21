package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.CommonStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoldenHourRepository extends JpaRepository<GoldenHour, Long> {
    
    List<GoldenHour> findByEventLocationId(Long locationId);
    
    List<GoldenHour> findByEventLocationIdAndStatus(Long locationId, CommonStatus status);
    
    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.eventLocation.id = :locationId " +
           "AND gh.status = 'ACTIVE' " +
           "AND gh.startTime <= :currentTime " +
           "AND gh.endTime > :currentTime")
    List<GoldenHour> findActiveGoldenHours(
        @Param("locationId") Long locationId,
        @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.eventLocation.id = :locationId " +
           "AND gh.status = 'ACTIVE' " +
           "AND gh.startTime < :endTime " +
           "AND gh.endTime > :startTime " +
           "ORDER BY gh.startTime")
    List<GoldenHour> findGoldenHoursInTimeRange(
        @Param("locationId") Long locationId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT CASE WHEN COUNT(gh) > 0 THEN true ELSE false END " +
           "FROM GoldenHour gh " +
           "WHERE gh.eventLocation.id = :locationId " +
           "AND gh.id != COALESCE(:excludeId, -1) " +
           "AND gh.status = 'ACTIVE' " +
           "AND NOT (" +
           "  gh.endTime <= :startTime OR " +  // Ends before start
           "  gh.startTime >= :endTime" +      // Starts after end
           ")")  
    boolean hasOverlappingGoldenHours(
        @Param("locationId") Long locationId,
        @Param("excludeId") Long excludeId, 
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
        
    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.eventLocation.event.id = :eventId " +
           "AND gh.status = 'ACTIVE' " +
           "ORDER BY gh.startTime")
    List<GoldenHour> findActiveGoldenHoursForEvent(@Param("eventId") Long eventId);
}