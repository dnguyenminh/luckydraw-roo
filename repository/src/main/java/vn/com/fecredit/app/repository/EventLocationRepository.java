package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.CommonStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {

       Optional<EventLocation> findByCode(String code);

       boolean existsByCode(String code);

       List<EventLocation> findByStatus(CommonStatus status);

       // Methods accepting Entity
       List<EventLocation> findByEvent(Event event);

       List<EventLocation> findByRegion(Region region);

       @Query("SELECT DISTINCT el FROM EventLocation el " +
                     "LEFT JOIN FETCH el.event " +
                     "LEFT JOIN FETCH el.region " +
                     "WHERE el.event = :event " +
                     "AND el.status = :status")
       List<EventLocation> findByEventAndStatus(@Param("event") Event event,
                     @Param("status") CommonStatus status);

       List<EventLocation> findByRegionId(Long regionId);

       List<EventLocation> findByEventIdAndStatus(Long eventId, CommonStatus status);

       @Query("SELECT DISTINCT el FROM EventLocation el " +
             "LEFT JOIN FETCH el.event " +
             "LEFT JOIN FETCH el.region " +
             "WHERE el.event.id = :eventId " +
             "AND el.status = 'ACTIVE' " +
             "AND el.maxSpin >= 3")
       List<EventLocation> findActiveSpinLocations(@Param("eventId") Long eventId);

       @Query("SELECT COUNT(el) > 0 FROM EventLocation el " +
                     "WHERE el.event.id = :eventId " +
                     "AND el.region.id = :regionId " +
                     "AND el.status = 'ACTIVE'")
       boolean existsActiveLocationInRegion(@Param("eventId") Long eventId,
                     @Param("regionId") Long regionId);

       @Query("SELECT COUNT(pe) < el.maxSpin FROM EventLocation el " +
                     "LEFT JOIN el.participantEvents pe " +
                     "WITH pe.status = 'ACTIVE' " +
                     "WHERE el.id = :locationId " +
                     "GROUP BY el.id, el.maxSpin")
       boolean hasAvailableCapacity(@Param("locationId") Long locationId);

       /**
        * Find all event locations for a specific event ID
        * 
        * @param eventId the ID of the event
        * @return list of event locations
        */
       @Query("SELECT el FROM EventLocation el WHERE el.event.id = :eventId")
       List<EventLocation> findByEventId(@Param("eventId") Long eventId);
}