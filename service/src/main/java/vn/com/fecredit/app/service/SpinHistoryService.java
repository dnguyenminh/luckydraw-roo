package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.List;

import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.service.base.AbstractService;

public interface SpinHistoryService extends AbstractService<SpinHistory> {
    List<SpinHistory> findByParticipantEventId(Long participantEventId);
    List<SpinHistory> findByRewardId(Long rewardId);
    List<SpinHistory> findByGoldenHourId(Long goldenHourId);
    List<SpinHistory> findSpinsInTimeRange(Long participantEventId, LocalDateTime startTime, LocalDateTime endTime);
    long countWinningSpinsAtLocation(Long locationId, LocalDateTime startTime, LocalDateTime endTime);
    List<SpinHistory> findWinningSpinsForEvent(Long eventId);
}
