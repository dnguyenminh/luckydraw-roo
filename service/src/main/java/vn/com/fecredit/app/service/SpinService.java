package vn.com.fecredit.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;
import vn.com.fecredit.app.service.exception.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service for managing spin operations
 */
@Service
@Transactional
public class SpinService {

    private final RewardRepository rewardRepository;
    private final SpinHistoryRepository spinHistoryRepository;
    private final Random random = new Random();

    @Autowired
    public SpinService(RewardRepository rewardRepository, SpinHistoryRepository spinHistoryRepository) {
        this.rewardRepository = rewardRepository;
        this.spinHistoryRepository = spinHistoryRepository;
    }

    /**
     * Process a spin for a reward
     *
     * @param rewardId the ID of the reward
     * @param participantEvent the participant event making the spin
     * @return the spin history record
     */
    public SpinHistory processSpin(Long rewardId, ParticipantEvent participantEvent) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with ID: " + rewardId));

        boolean isWin = calculateWinChance(rewardId);
        
        // If we have a win but no remaining quantity, override to a loss
        if (isWin && reward.getRemainingQuantity() <= 0) {
            isWin = false;
        }
        
        SpinHistory history = SpinHistory.builder()
                .reward(reward)
                .participantEvent(participantEvent)
                .spinTime(LocalDateTime.now())
                .win(isWin)
                .status(CommonStatus.ACTIVE)
                .build();
                
        return spinHistoryRepository.save(history);
    }

    /**
     * Calculate if a spin results in a win based on the reward's win probability
     *
     * @param rewardId the ID of the reward
     * @return true if the spin is a win, false otherwise
     */
    public boolean calculateWinChance(Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new EntityNotFoundException("Reward not found with ID: " + rewardId));
                
        Double winProbability = reward.getWinProbability();
        
        if (winProbability == null || winProbability <= 0.0) {
            return false;
        }
        
        double randomValue = generateRandomProbability();
        return randomValue <= winProbability;
    }
    
    /**
     * Generate a random probability between 0 and 1
     * Extracted for testing purposes
     *
     * @return a random double value between 0 and 1
     */
    protected double generateRandomProbability() {
        return random.nextDouble();
    }
}
