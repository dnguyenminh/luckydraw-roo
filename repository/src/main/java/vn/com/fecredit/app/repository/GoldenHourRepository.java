package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.EventLocationKey;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface GoldenHourRepository extends SimpleObjectRepository<GoldenHour> {

       List<GoldenHour> findByEventLocationId(EventLocationKey locationId);

       List<GoldenHour> findByEventLocationIdAndStatus(EventLocationKey locationId, CommonStatus status);

       @Query("SELECT gh FROM GoldenHour gh " +
                     "WHERE gh.eventLocation.id = :locationId " +
                     "AND gh.status = 'ACTIVE' " +
                     "AND gh.startTime <= :currentTime " +
                     "AND gh.endTime > :currentTime")
       GoldenHour findActiveGoldenHours(
                     @Param("locationId") EventLocationKey locationId,
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
                     "  gh.endTime <= :startTime OR " + // Ends before start
                     "  gh.startTime >= :endTime" + // Starts after end
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

       List<GoldenHour> findByStatus(CommonStatus status);

       @Query("SELECT gh FROM GoldenHour gh " +
                     "WHERE gh.status = 'ACTIVE' " +
                     "AND gh.startTime <= :currentTime " +
                     "AND gh.endTime > :currentTime")
       List<GoldenHour> findActiveGoldenHours(@Param("currentTime") LocalDateTime currentTime);

       @Query("SELECT gh FROM GoldenHour gh " +
                     "WHERE gh.eventLocation.id = :locationId " +
                     "AND gh.status = 'ACTIVE' " +
                     "AND gh.startTime <= :currentTime " +
                     "AND gh.endTime > :currentTime")
       List<GoldenHour> findActiveGoldenHoursByLocation(
                     @Param("locationId") Long locationId,
                     @Param("currentTime") LocalDateTime currentTime);

       @Query("SELECT gh FROM GoldenHour gh " +
                     "WHERE gh.status = 'ACTIVE' " +
                     "AND gh.startTime > :currentTime")
       List<GoldenHour> findUpcomingGoldenHours(@Param("currentTime") LocalDateTime currentTime);

       @Query("SELECT gh FROM GoldenHour gh " +
                     "WHERE gh.status = 'ACTIVE' " +
                     "AND gh.endTime <= :currentTime")
       List<GoldenHour> findPastGoldenHours(@Param("currentTime") LocalDateTime currentTime);

       @Query("SELECT gh FROM GoldenHour gh WHERE gh.eventLocation.event.id = :eventId")
       List<GoldenHour> findByEventId(@Param("eventId") Long eventId);

       @Query("SELECT gh FROM GoldenHour gh WHERE gh.eventLocation.event.id = :eventId AND gh.status = :status")
       List<GoldenHour> findByEventIdAndStatus(
                     @Param("eventId") Long eventId,
                     @Param("status") CommonStatus status);

       @Query("SELECT gh FROM GoldenHour gh " +
                     "WHERE gh.eventLocation.event.id = :eventId " +
                     "AND gh.startTime >= :startTime " +
                     "AND gh.endTime <= :endTime " +
                     "AND gh.status = :status " +
                     "ORDER BY gh.startTime")
       List<GoldenHour> findByEventIdAndTimeRangeAndStatus(
                     @Param("eventId") Long eventId,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime,
                     @Param("status") CommonStatus status);

       @Query("SELECT gh FROM GoldenHour gh WHERE gh.status = 'ACTIVE' " +
              "AND gh.eventLocation = :eventLocation " +
              "AND gh.startTime < :endTime AND gh.endTime > :startTime " +
              "ORDER BY gh.startTime")
       List<GoldenHour> findActiveGoldenHoursInPeriod(
              @Param("eventLocation") EventLocation eventLocation, 
              @Param("startTime") LocalDateTime startTime, 
              @Param("endTime") LocalDateTime endTime);

       @Query("SELECT COUNT(gh) FROM GoldenHour gh WHERE gh.status = 'ACTIVE' " +
              "AND gh.eventLocation = :eventLocation " +
              "AND gh.startTime < :endTime AND gh.endTime > :startTime " +
              "AND (gh.id != :excludeId OR :excludeId IS NULL)")
       long countOverlappingActiveHoursExcluding(
              @Param("eventLocation") EventLocation eventLocation, 
              @Param("startTime") LocalDateTime startTime, 
              @Param("endTime") LocalDateTime endTime, 
              @Param("excludeId") Long excludeId);
}
