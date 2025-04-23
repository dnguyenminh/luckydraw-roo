package vn.com.fecredit.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.RewardEvent;
import vn.com.fecredit.app.entity.RewardEventKey;
import vn.com.fecredit.app.entity.enums.CommonStatus;

@Repository
public interface RewardEventRepository extends ComplexObjectRepository<RewardEvent, RewardEventKey> {

    /**
     * Find all reward events by reward ID
     *
     * @param rewardId the reward ID
     * @return list of reward events
     */
    @Query("SELECT re FROM RewardEvent re WHERE re.reward.id = :rewardId")
    List<RewardEvent> findByRewardId(@Param("rewardId") Long rewardId);

    /**
     * Find all reward events by event location's event ID
     *
     * @param eventId the event ID
     * @return list of reward events
     */
    @Query("SELECT re FROM RewardEvent re WHERE re.eventLocation.event.id = :eventId")
    List<RewardEvent> findByEventLocationId(@Param("eventId") Long eventId);

    /**
     * Find all reward events by reward ID and status
     *
     * @param rewardId the reward ID
     * @param status the status to filter
     * @return list of reward events
     */
    @Query("SELECT re FROM RewardEvent re WHERE re.reward.id = :rewardId AND re.status = :status")
    List<RewardEvent> findByRewardIdAndStatus(
            @Param("rewardId") Long rewardId,
            @Param("status") CommonStatus status);

}
