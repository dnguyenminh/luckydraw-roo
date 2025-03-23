package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.service.base.AbstractService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SpinHistoryService extends AbstractService<SpinHistory> {
    List<SpinHistory> findByParticipantEventId(Long participantEventId);
    List<SpinHistory> findByRewardId(Long rewardId);
    List<SpinHistory> findByGoldenHourId(Long goldenHourId);
    List<SpinHistory> findSpinsInTimeRange(Long participantEventId, LocalDateTime startTime, LocalDateTime endTime);
    BigDecimal calculateTotalWinnings(Long participantEventId);
    long countWinningSpinsAtLocation(Long locationId, LocalDateTime startTime, LocalDateTime endTime);
    List<SpinHistory> findWinningSpinsForEvent(Long eventId);
}
