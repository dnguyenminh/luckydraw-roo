package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.service.EventLocationService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

@Slf4j
@Service
@Transactional
public class EventLocationServiceImpl extends AbstractServiceImpl<EventLocation> implements EventLocationService {

    private final EventLocationRepository eventLocationRepository;

    @Autowired
    public EventLocationServiceImpl(EventLocationRepository eventLocationRepository) {
        super(eventLocationRepository);
        this.eventLocationRepository = eventLocationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findByStatus(CommonStatus status) {
        return eventLocationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventLocation> findByCode(String code) {
        return eventLocationRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findByEventId(Long eventId) {
        return eventLocationRepository.findByEventId(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findByRegionId(Long regionId) {
        return eventLocationRepository.findByRegionId(regionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findByEventIdAndStatus(Long eventId, CommonStatus status) {
        return eventLocationRepository.findByEventIdAndStatus(eventId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAvailableCapacity(Long locationId) {
        EventLocation location = findById(locationId)
            .orElseThrow(() -> new EntityNotFoundException("EventLocation not found with ID: " + locationId));
        return location.hasAvailableCapacity();
    }

    // @Override
    // @Transactional(readOnly = true)
    // public List<EventLocation> findActiveSpinLocations() {
    //     return eventLocationRepository.findActiveSpinLocations();
    // }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findActiveSpinLocations(Long eventId) {
        return eventLocationRepository.findActiveSpinLocations(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveLocationInRegion(Long eventId, Long regionId) {
        return eventLocationRepository.existsActiveLocationInRegion(eventId, regionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findByEventAndStatus(Event event, CommonStatus status) {
        return eventLocationRepository.findByEventAndStatus(event, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findByRegion(Region region) {
        return eventLocationRepository.findByRegion(region);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocation> findByEvent(Event event) {
        return eventLocationRepository.findByEvent(event);
    }

    @Override
    public EventLocation deactivate(Long id) {
        EventLocation location = super.deactivate(id);
        // Additional cleanup logic if needed
        return location;
    }

    @Override
    public EventLocation updateWinProbability(Long id, Double winProbability) {
        EventLocation eventLocation = findById(id)
            .orElseThrow(() -> new EntityNotFoundException("EventLocation not found with ID: " + id));
        eventLocation.setWinProbability(winProbability);
        return eventLocationRepository.save(eventLocation);
    }

    @Override
    public EventLocation updateQuantity(Long id, Integer quantity) {
        EventLocation eventLocation = findById(id)
            .orElseThrow(() -> new EntityNotFoundException("EventLocation not found with ID: " + id));
        eventLocation.setQuantity(quantity);
        return eventLocationRepository.save(eventLocation);
    }

    @Override
    public EventLocation update(Long id, EventLocation locationData) {
        EventLocation existingLocation = findById(id)
            .orElseThrow(() -> new EntityNotFoundException("EventLocation not found with ID: " + id));
        
        // Update basic properties if provided
        if (locationData.getName() != null) {
            existingLocation.setName(locationData.getName());
        }
        
        if (locationData.getCode() != null) {
            existingLocation.setCode(locationData.getCode());
        }
        
        if (locationData.getDescription() != null) {
            existingLocation.setDescription(locationData.getDescription());
        }
        
        if (locationData.getMaxSpin() != null) {
            existingLocation.setMaxSpin(locationData.getMaxSpin());
        }
        
        // Update the migrated properties
        if (locationData.getQuantity() != null) {
            existingLocation.setQuantity(locationData.getQuantity());
        }
        
        if (locationData.getWinProbability() != null) {
            existingLocation.setWinProbability(locationData.getWinProbability());
        }
        
        // Update relationships if provided
        if (locationData.getRegion() != null && 
                (existingLocation.getRegion() == null || 
                !existingLocation.getRegion().getId().equals(locationData.getRegion().getId()))) {
            existingLocation.setRegion(locationData.getRegion());
        }
        
        if (locationData.getEvent() != null &&
                (existingLocation.getEvent() == null || 
                !existingLocation.getEvent().getId().equals(locationData.getEvent().getId()))) {
            existingLocation.setEvent(locationData.getEvent());
        }
        
        // Update status if provided
        if (locationData.getStatus() != null) {
            existingLocation.setStatus(locationData.getStatus());
        }
        
        return eventLocationRepository.save(existingLocation);
    }

    @Override
    public EventLocation create(EventLocation eventLocation) {
        // Ensure the code is uppercase
        if (eventLocation.getCode() != null) {
            eventLocation.setCode(eventLocation.getCode().toUpperCase());
        }
        
        // Validate before saving
        eventLocation.validateState();
        
        return eventLocationRepository.save(eventLocation);
    }
}
