package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.EventLocationKey;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Repository for EventLocation entities
 */
@Repository
public interface EventLocationRepository extends ComplexObjectRepository<EventLocation, EventLocationKey> {

    /**
     * Find all event locations for a given event ID
     *
     * @param eventId The ID of the event
     * @return List of event locations for the event
     */
    @Query("SELECT el FROM EventLocation el WHERE el.event.id = :eventId")
    List<EventLocation> findByEventId(@Param("eventId") Long eventId);

    /**
     * Find event locations by event ID and status
     */
    @Query("SELECT el FROM EventLocation el WHERE el.event.id = :eventId AND el.status = :status")
    List<EventLocation> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") CommonStatus status);

    /**
     * Find active event locations with spins by event ID
     */
    @Query("SELECT el FROM EventLocation el JOIN FETCH el.event JOIN FETCH el.region WHERE el.event.id = :eventId AND el.status = 'ACTIVE' AND el.maxSpin > 0")
    List<EventLocation> findActiveSpinLocations(@Param("eventId") Long eventId);

    /**
     * Check if active locations exist in a region for an event
     */
    @Query("SELECT COUNT(el) > 0 FROM EventLocation el WHERE el.event.id = :eventId AND el.region.id = :regionId AND el.status = 'ACTIVE'")
    boolean existsActiveLocationInRegion(@Param("eventId") Long eventId, @Param("regionId") Long regionId);

    /**
     * Find by event
     */
    List<EventLocation> findByEvent(Event event);

    /**
     * Find by region
     */
    List<EventLocation> findByRegion(Region region);

    /**
     * Find by event and status (with eager loading of related entities)
     */
    @Query("SELECT el FROM EventLocation el JOIN FETCH el.event JOIN FETCH el.region WHERE el.event = :event AND el.status = :status")
    List<EventLocation> findByEventAndStatus(@Param("event") Event event, @Param("status") CommonStatus status);

    /**
     * Find by region id
     */
    @Query("SELECT el FROM EventLocation el WHERE el.region.id = :regionId")
    List<EventLocation> findByRegionId(@Param("regionId") Long regionId);

    List<EventLocation> findByRegionAndEvent(@NotNull(message = "Region is required") Region region, Event event);

    long countByEventIdAndStatus(Long eventId, @NotNull CommonStatus status);

    /**
     * Find event locations by status.
     *
     * @param status the status to filter by
     * @param pageable pagination information
     * @return page of event locations
     */
    Page<EventLocation> findByStatus(CommonStatus status, Pageable pageable);

    /**
     * Find event locations by region.
     *
     * @param region the region to filter by
     * @param pageable pagination information
     * @return page of event locations
     */
    Page<EventLocation> findByRegion(Region region, Pageable pageable);

    /**
     * Find active event locations for events taking place on the given date.
     * This method filters locations that are active and belong to active events
     * where the provided date falls within the event's start and end times.
     *
     * @param date the date to check against event start/end times
     * @return list of active event locations for events occurring on the given date
     */
    @Query("SELECT el FROM EventLocation el " +
           "WHERE el.status = 'ACTIVE' " +
           "AND el.event.status = 'ACTIVE' " +
           "AND :date BETWEEN el.event.startTime AND el.event.endTime")
    List<EventLocation> findActiveLocationsByEventDate(@Param("date") LocalDateTime date);
}
