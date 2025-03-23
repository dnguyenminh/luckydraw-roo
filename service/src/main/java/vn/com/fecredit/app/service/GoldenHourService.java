package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.service.base.AbstractService;

import java.time.LocalDateTime;
import java.util.List;

public interface GoldenHourService extends AbstractService<GoldenHour> {
    List<GoldenHour> findByEventLocationId(Long locationId);
    List<GoldenHour> findActiveGoldenHours(LocalDateTime currentTime);
    List<GoldenHour> findActiveGoldenHoursByLocation(Long locationId, LocalDateTime currentTime);
    List<GoldenHour> findUpcomingGoldenHours(LocalDateTime currentTime);
    List<GoldenHour> findPastGoldenHours(LocalDateTime currentTime);
    boolean isGoldenHourActive(Long goldenHourId, LocalDateTime currentTime);
}
