package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.service.RewardService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class RewardServiceImpl extends AbstractServiceImpl<Reward> implements RewardService {

    private final RewardRepository rewardRepository;

    public RewardServiceImpl(RewardRepository rewardRepository) {
        super(rewardRepository);
        this.rewardRepository = rewardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reward> findByStatus(CommonStatus status) {
        return rewardRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reward> findByCode(String code) {
        return rewardRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reward> findByEventLocationId(Long locationId) {
        return rewardRepository.findByEventLocationId(locationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reward> findAvailableRewardsByLocation(Long locationId) {
        return rewardRepository.findAvailableRewardsByLocation(locationId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRewardAvailable(Long rewardId) {
        return findById(rewardId)
            .map(reward -> CommonStatus.ACTIVE.equals(reward.getStatus()) && reward.getQuantity() > 0)
            .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAvailableQuantity(Long rewardId) {
        return findById(rewardId)
            .map(reward -> reward.getQuantity() > 0)
            .orElse(false);
    }

    @Override
    @Transactional
    public Reward decreaseQuantity(Long rewardId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Decrease amount must be positive");
        }
        
        Reward reward = findById(rewardId)
            .orElseThrow(() -> new EntityNotFoundException("Reward not found with id: " + rewardId));
            
        if (reward.getQuantity() < amount) {
            throw new IllegalStateException("Not enough quantity available for reward: " + rewardId);
        }
        
        reward.setQuantity(reward.getQuantity() - amount);
        
        // If quantity reaches zero, consider disabling the reward
        if (reward.getQuantity() == 0) {
            log.info("Reward {} has zero quantity remaining", rewardId);
        }
        
        return rewardRepository.save(reward);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMaximumRewardValueAtLocation(Long locationId) {
        return rewardRepository.getMaximumRewardValueAtLocation(locationId);
    }

    @Override
    public List<Reward> findByEventId(Long eventId) {
        return rewardRepository.findByEventId(eventId);
    }
}
