package vn.com.fecredit.app.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.fecredit.app.entity.base.SerializableKey;

/**
 * Composite primary key for the RewardEvent entity.
 * <p>
 * This key consists of an EventLocationKey (itself a composite key of event and region IDs)
 * and a reward ID. Together, these uniquely identify a reward's allocation to a specific
 * event location.
 * </p>
 * <p>
 * The key is used in relationships with SpinHistory entities that need to reference
 * a specific reward event through its composite key.
 * </p>
 * <p>
 * The default no-argument constructor is required by JPA for embeddable classes
 * and initializes an empty key that must be populated before use.
 * </p>
 */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode // Added Lombok annotation for equals and hashCode
public class RewardEventKey implements SerializableKey {
    private static final long serialVersionUID = 1L;

    /**
     * The event location key component of this composite key
     * Contains both the event ID and region ID that identify the location
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "eventId", column = @Column(name = "event_id")),
        @AttributeOverride(name = "regionId", column = @Column(name = "region_id"))
    })
    private EventLocationKey eventLocationKey;
    
    /**
     * The ID of the reward
     * Identifies which reward is being allocated to this event location
     */
    @Column(name = "reward_id")
    private Long rewardId;
}
