package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;

import java.util.List;
import java.util.Optional;

public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {
       Optional<EventLocation> findByCode(String code);

       List<EventLocation> findByEventId(Long eventId);

       List<EventLocation> findByRegionId(Long regionId);

       List<EventLocation> findByStatus(CommonStatus status);

       List<EventLocation> findByEventIdAndStatus(Long eventId, CommonStatus status);

       /**
        * Find all event locations for a specific event with the given status
        * 
        * @param event  the event
        * @param status the status
        * @return list of event locations matching the criteria
        */
       List<EventLocation> findByEventAndStatus(Event event, CommonStatus status);

       /**
        * Find all event locations for a specific event
        * 
        * @param event the event
        * @return list of event locations for the given event
        */
       List<EventLocation> findByEvent(Event event);

       /**
        * Find all event locations for a specific region
        * 
        * @param region the region
        * @return list of event locations in the region
        */
       List<EventLocation> findByRegion(Region region);

       /**
        * Find all active locations with available spin capacity for a specific event
        * 
        * @param eventId the event ID to filter by
        * @return list of active locations available for spinning for the specified
        *         event
        */
       @Query("SELECT el FROM EventLocation el WHERE el.status = 'ACTIVE' AND el.maxSpin > 0 " +
                     "AND el.event.id = :eventId ORDER BY el.id")
       List<EventLocation> findActiveSpinLocations(@Param("eventId") Long eventId);

       /**
        * Find all active locations with available spin capacity across all events
        * 
        * @return list of active locations available for spinning
        */
       @Query("SELECT el FROM EventLocation el WHERE el.status = 'ACTIVE' AND el.maxSpin > 0 " +
                     "ORDER BY el.id")
       List<EventLocation> findActiveSpinLocations();

       /**
        * Check if there's an active location for the given event in the specified
        * region
        * 
        * @param eventId  the event ID
        * @param regionId the region ID
        * @return true if an active location exists in the region for the event
        */
       @Query("SELECT CASE WHEN COUNT(el) > 0 THEN true ELSE false END FROM EventLocation el " +
                     "WHERE el.event.id = :eventId AND el.region.id = :regionId AND el.status = 'ACTIVE'")
       boolean existsActiveLocationInRegion(Long eventId, Long regionId);
}