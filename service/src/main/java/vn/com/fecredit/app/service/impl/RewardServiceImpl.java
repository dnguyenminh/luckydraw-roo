package vn.com.fecredit.app.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.service.EventLocationService;
import vn.com.fecredit.app.service.RewardService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

@Slf4j
@Service
@Transactional
public class RewardServiceImpl extends AbstractServiceImpl<Reward> implements RewardService {

    private final RewardRepository rewardRepository;
    private final EventLocationService eventLocationService;

    @Autowired
    public RewardServiceImpl(RewardRepository rewardRepository, EventLocationService eventLocationService) {
        super(rewardRepository);
        this.rewardRepository = rewardRepository;
        this.eventLocationService = eventLocationService;
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
        return rewardRepository.findAvailableRewardsForLocation(locationId);
    }

    @Override
    public Reward create(Reward reward, Long eventLocationId) {
        EventLocation eventLocation = eventLocationService.findById(eventLocationId)
                .orElseThrow(() -> new EntityNotFoundException("EventLocation not found with ID: " + eventLocationId));
        
        reward.setEventLocation(eventLocation);
        
        // Validate state before saving
        reward.validateState();
        
        return rewardRepository.save(reward);
    }

    @Override
    public Reward update(Long id, Reward rewardData) {
        Reward existingReward = findById(id).orElse(null);
        
        // Update properties if provided
        if (rewardData.getName() != null) {
            existingReward.setName(rewardData.getName());
        }
        
        if (rewardData.getCode() != null) {
            existingReward.setCode(rewardData.getCode());
        }
        
        if (rewardData.getDescription() != null) {
            existingReward.setDescription(rewardData.getDescription());
        }
        
        // Update status if provided
        if (rewardData.getStatus() != null) {
            existingReward.setStatus(rewardData.getStatus());
        }
        
        return rewardRepository.save(existingReward);
    }

    @Override
    public int getRemainingQuantity(Long rewardId) {
        Reward reward = findById(rewardId).orElseThrow(() -> 
            new EntityNotFoundException("Reward not found with ID: " + rewardId));
        return reward.getRemainingQuantity();
    }

    @Override
    public Reward activate(Long id) {
        Reward reward = findById(id).orElseThrow();
        reward.setStatus(CommonStatus.ACTIVE);
        return rewardRepository.save(reward);
    }

    @Override
    public Reward deactivate(Long id) {
        Reward reward = findById(id).orElseThrow();
        reward.setStatus(CommonStatus.INACTIVE);
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
