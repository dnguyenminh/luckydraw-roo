package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.EventLocation;
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
    public boolean hasAvailableCapacity(Long locationId) {
        return findById(locationId)
            .map(EventLocation::hasAvailableCapacity)
            .orElseThrow(() -> new EntityNotFoundException("EventLocation not found with id: " + locationId));
    }

    @Override
    public EventLocation deactivate(Long id) {
        EventLocation location = super.deactivate(id);
        // Additional cleanup logic if needed
        return location;
    }
}
