package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.service.base.AbstractService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RewardService extends AbstractService<Reward> {
    Optional<Reward> findByCode(String code);
    List<Reward> findByEventLocationId(Long locationId);
    List<Reward> findAvailableRewardsByLocation(Long locationId);
    boolean isRewardAvailable(Long rewardId);
    boolean hasAvailableQuantity(Long rewardId);
    Reward decreaseQuantity(Long rewardId, int amount);
    BigDecimal getMaximumRewardValueAtLocation(Long locationId);
}
