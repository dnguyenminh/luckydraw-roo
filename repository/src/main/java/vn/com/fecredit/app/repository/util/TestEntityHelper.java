package vn.com.fecredit.app.repository.util;

import jakarta.persistence.EntityManager;
import lombok.experimental.UtilityClass;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import java.time.LocalDateTime;

/**
 * Utility class for test data creation that bypasses JPA's entity management for composite key entities
 */
@UtilityClass
public class TestEntityHelper {

    /**
     * Insert a ParticipantEvent record directly using SQL
     */
    public static void insertParticipantEvent(EntityManager entityManager, 
                                           Event event, 
                                           Region region, 
                                           Participant participant,
                                           int spinsRemaining,
                                           CommonStatus status,
                                           LocalDateTime createdAt,
                                           String createdBy) {
        
        // Clear session before SQL operation to avoid conflicts with existing entities
        entityManager.flush();
        entityManager.clear();
        
        entityManager.createNativeQuery(
            "INSERT INTO participant_events (event_id, region_id, participant_id, created_at, created_by, " +
            "updated_at, updated_by, status, spins_remaining, version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
            .setParameter(1, event.getId())
            .setParameter(2, region.getId())
            .setParameter(3, participant.getId())
            .setParameter(4, createdAt)
            .setParameter(5, createdBy)
            .setParameter(6, createdAt) // same as created_at for tests
            .setParameter(7, createdBy) // same as created_by for tests
            .setParameter(8, status.name())
            .setParameter(9, spinsRemaining)
            .setParameter(10, 0L) // version
            .executeUpdate();
        
        // Don't flush here - let the caller manage session flushing
    }
    
    /**
     * Insert a RewardEvent record directly using SQL
     */
    public static void insertRewardEvent(EntityManager entityManager,
                                      Event event,
                                      Region region,
                                      Reward reward,
                                      int quantity,
                                      int todayQuantity,
                                      CommonStatus status,
                                      LocalDateTime createdAt,
                                      String createdBy) {
        
        // Clear session before SQL operation to avoid conflicts with existing entities
        entityManager.flush();
        entityManager.clear();
        
        entityManager.createNativeQuery(
            "INSERT INTO reward_events (event_id, region_id, reward_id, created_at, created_by, " +
            "updated_at, updated_by, status, quantity, today_quantity, version) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
            .setParameter(1, event.getId())
            .setParameter(2, region.getId())
            .setParameter(3, reward.getId())
            .setParameter(4, createdAt)
            .setParameter(5, createdBy)
            .setParameter(6, createdAt)
            .setParameter(7, createdBy)
            .setParameter(8, status.name())
            .setParameter(9, quantity)
            .setParameter(10, todayQuantity)
            .setParameter(11, 0L)
            .executeUpdate();
        
        // Don't flush here - let the caller manage session flushing
    }
}
