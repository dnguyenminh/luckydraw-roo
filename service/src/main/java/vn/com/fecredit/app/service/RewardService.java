package vn.com.fecredit.app.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.service.base.AbstractService;

public interface RewardService extends AbstractService<Reward> {
    Optional<Reward> findByCode(String code);

    List<Reward> findByEventLocationId(Long locationId);

    List<Reward> findByEventId(Long eventId);

    List<Reward> findAvailableRewardsByLocation(Long locationId);

    BigDecimal getMaximumRewardValueAtLocation(Long locationId);

    /**
     * Create a new reward in the specified event location
     * 
     * @param reward          the reward to create
     * @param eventLocationId the ID of the event location
     * @return the created reward
     */
    Reward create(Reward reward, Long eventLocationId);

    /**
     * Update an existing reward
     * 
     * @param id         the ID of the reward to update
     * @param rewardData the updated reward data
     * @return the updated reward
     */
    Reward update(Long id, Reward rewardData);

    /**
     * Get the remaining quantity for a reward
     * 
     * @param rewardId the ID of the reward
     * @return the remaining quantity
     */
    int getRemainingQuantity(Long rewardId);

    /**
     * Activate a reward
     * 
     * @param id the ID of the reward to activate
     * @return the activated reward
     */
    Reward activate(Long id);

    /**
     * Deactivate a reward
     * 
     * @param id the ID of the reward to deactivate
     * @return the deactivated reward
     */
    Reward deactivate(Long id);
}
