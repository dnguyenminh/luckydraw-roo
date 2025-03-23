package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.service.GoldenHourService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class GoldenHourServiceImpl extends AbstractServiceImpl<GoldenHour> implements GoldenHourService {

    private final GoldenHourRepository goldenHourRepository;

    public GoldenHourServiceImpl(GoldenHourRepository goldenHourRepository) {
        super(goldenHourRepository);
        this.goldenHourRepository = goldenHourRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoldenHour> findByStatus(CommonStatus status) {
        return goldenHourRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoldenHour> findByEventLocationId(Long locationId) {
        return goldenHourRepository.findByEventLocationId(locationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoldenHour> findActiveGoldenHours(LocalDateTime currentTime) {
        return goldenHourRepository.findActiveGoldenHours(currentTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoldenHour> findActiveGoldenHoursByLocation(Long locationId, LocalDateTime currentTime) {
        return goldenHourRepository.findActiveGoldenHoursByLocation(locationId, currentTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoldenHour> findUpcomingGoldenHours(LocalDateTime currentTime) {
        return goldenHourRepository.findUpcomingGoldenHours(currentTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoldenHour> findPastGoldenHours(LocalDateTime currentTime) {
        return goldenHourRepository.findPastGoldenHours(currentTime);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGoldenHourActive(Long goldenHourId, LocalDateTime currentTime) {
        return findById(goldenHourId)
            .map(goldenHour -> 
                CommonStatus.ACTIVE.equals(goldenHour.getStatus()) &&
                goldenHour.getStartTime().isBefore(currentTime) &&
                goldenHour.getEndTime().isAfter(currentTime))
            .orElse(false);
    }
}
