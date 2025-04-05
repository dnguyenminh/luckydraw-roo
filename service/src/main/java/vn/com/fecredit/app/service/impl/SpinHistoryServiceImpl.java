package vn.com.fecredit.app.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.SpinHistoryRepository;
import vn.com.fecredit.app.service.SpinHistoryService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

@Slf4j
@Service
@Transactional
public class SpinHistoryServiceImpl extends AbstractServiceImpl<SpinHistory> implements SpinHistoryService {

    private final SpinHistoryRepository spinHistoryRepository;

    public SpinHistoryServiceImpl(SpinHistoryRepository spinHistoryRepository) {
        super(spinHistoryRepository);
        this.spinHistoryRepository = spinHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpinHistory> findByParticipantEventId(Long participantEventId) {
        return spinHistoryRepository.findByParticipantEventId(participantEventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpinHistory> findByRewardId(Long rewardId) {
        return spinHistoryRepository.findByRewardId(rewardId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpinHistory> findByGoldenHourId(Long goldenHourId) {
        return spinHistoryRepository.findByGoldenHourId(goldenHourId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpinHistory> findSpinsInTimeRange(
            Long participantEventId, LocalDateTime startTime, LocalDateTime endTime) {
        return spinHistoryRepository.findSpinsInTimeRange(participantEventId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public long countWinningSpinsAtLocation(
            Long locationId, LocalDateTime startTime, LocalDateTime endTime) {
        return spinHistoryRepository.countWinningSpinsAtLocation(locationId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpinHistory> findWinningSpinsForEvent(Long eventId) {
        return spinHistoryRepository.findWinningSpinsForEvent(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpinHistory> findByStatus(CommonStatus status) {
        return spinHistoryRepository.findByStatus(status);
    }
}
