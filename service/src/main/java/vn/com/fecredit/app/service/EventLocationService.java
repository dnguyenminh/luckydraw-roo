package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.service.base.AbstractService;

import java.util.List;
import java.util.Optional;

public interface EventLocationService extends AbstractService<EventLocation> {
    Optional<EventLocation> findByCode(String code);
    List<EventLocation> findByEventId(Long eventId);
    List<EventLocation> findByRegionId(Long regionId);
    boolean hasAvailableCapacity(Long locationId);
}
