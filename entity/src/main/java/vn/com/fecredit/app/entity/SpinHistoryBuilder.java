package vn.com.fecredit.app.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Utility class to create pre-configured SpinHistory instances.
 * This ensures that SpinHistory objects are properly created and configured.
 */
public final class SpinHistoryBuilder {

    private SpinHistoryBuilder() {
        // Prevent instantiation
    }
    
    /**
     * Create a winning SpinHistory object
     */
    public static SpinHistory createWinningSpin(
            ParticipantEvent participantEvent,
            Reward reward,
            GoldenHour goldenHour,
            BigDecimal multiplier) {
            
        LocalDateTime now = LocalDateTime.now();
        // Use safer approach to access event
        try {
            if (participantEvent != null) {
                Event event = participantEvent.getEvent();
                if (event != null) {
                    now = event.getCurrentServerTime();
                }
            }
        } catch (Exception e) {
            // Fallback to current time if any issue
        }
        
        // Manually construct instead of using builder pattern
        SpinHistory spin = new SpinHistory();
        spin.setParticipantEvent(participantEvent);
        spin.setReward(reward);
        spin.setGoldenHour(goldenHour);
        spin.setWin(true);
        spin.setSpinTime(now);
        spin.setStatus(CommonStatus.ACTIVE);
                
        if (multiplier != null) {
            spin.setMultiplier(multiplier);
        }
                
        if (participantEvent != null) {
            try {
                participantEvent.addSpinHistory(spin);
            } catch (Exception e) {
                // Handle if addSpinHistory() is not accessible
            }
        }
        
        return spin;
    }
    
    /**
     * Create a losing SpinHistory object
     */
    public static SpinHistory createLosingSpin(ParticipantEvent participantEvent) {
        LocalDateTime now = LocalDateTime.now();
        // Use safer approach to access event
        try {
            if (participantEvent != null) {
                Event event = participantEvent.getEvent();
                if (event != null) {
                    now = event.getCurrentServerTime();
                }
            }
        } catch (Exception e) {
            // Fallback to current time if any issue
        }
        
        // Manually construct instead of using builder pattern
        SpinHistory spin = new SpinHistory();
        spin.setParticipantEvent(participantEvent);
        spin.setWin(false);
        spin.setSpinTime(now);
        spin.setStatus(CommonStatus.ACTIVE);
                
        if (participantEvent != null) {
            try {
                participantEvent.addSpinHistory(spin);
            } catch (Exception e) {
                // Handle gracefully
            }
        }
        
        return spin;
    }
}
