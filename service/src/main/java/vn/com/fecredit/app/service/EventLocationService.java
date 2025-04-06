package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.service.base.AbstractService;

import java.util.List;
import java.util.Optional;

public interface EventLocationService extends AbstractService<EventLocation> {
    Optional<EventLocation> findByCode(String code);
    List<EventLocation> findByEventId(Long eventId);
    List<EventLocation> findByRegionId(Long regionId);
    List<EventLocation> findByEventIdAndStatus(Long eventId, CommonStatus status);
    boolean hasAvailableCapacity(Long locationId);
    EventLocation updateWinProbability(Long id, Double winProbability);
    EventLocation updateQuantity(Long id, Integer quantity);
    
    /**
     * Update an event location with the provided data
     * @param id the ID of the location to update
     * @param locationData the new location data
     * @return the updated location
     */
    EventLocation update(Long id, EventLocation locationData);
    
    /**
     * Create a new event location
     * @param eventLocation the location data to create
     * @return the created location
     */
    EventLocation create(EventLocation eventLocation);
    
    /**
     * Find all active locations available for spinning for a specific event
     * @param eventId the ID of the event
     * @return list of active locations with available capacity for the specified event
     */
    List<EventLocation> findActiveSpinLocations(Long eventId);
    
    /**
     * Check if an active location exists for the given event in a specified region
     * @param eventId the event ID
     * @param regionId the region ID
     * @return true if an active location exists
     */
    boolean existsActiveLocationInRegion(Long eventId, Long regionId);
    
    /**
     * Find all event locations for a specific event with the given status
     * @param event the event
     * @param status the status
     * @return list of event locations matching the criteria
     */
    List<EventLocation> findByEventAndStatus(Event event, CommonStatus status);
    
    /**
     * Find all event locations for a specific region
     * @param region the region
     * @return list of event locations in the region
     */
    List<EventLocation> findByRegion(Region region);
    
    /**
     * Find all event locations for a specific event
     * @param event the event
     * @return list of event locations for the given event
     */
    List<EventLocation> findByEvent(Event event);
}
