package vn.com.fecredit.app.entity;

import java.util.HashSet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractComplexPersistableEntity;

/**
 * Entity representing the allocation of rewards to specific event locations.
 * <p>
 * RewardEvent tracks the relationship between rewards and event locations,
 * including quantity allocations and daily limits. This allows for detailed
 * tracking of reward distribution across different event locations.
 * </p>
 * <p>
 * The entity uses a composite key combining the event location and reward IDs,
 * ensuring proper inventory management and allocation of rewards to specific locations.
 * </p>
 */
@Entity
@Table(name = "reward_events", indexes = {
    @Index(name = "idx_reward_location", columnList = "event_id, region_id"),
    @Index(name = "idx_reward", columnList = "reward_id"),
    @Index(name = "idx_reward_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = { "eventLocation",  "reward" })
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RewardEvent extends AbstractComplexPersistableEntity<RewardEventKey> {
    
    /**
     * The event location this reward allocation belongs to
     * Links to the specific physical location for an event
     */
    @NotNull(message = "Event location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventLocationKey")
    @JoinColumns({
        @JoinColumn(name = "event_id", nullable = false),
        @JoinColumn(name = "region_id", nullable = false)
    })
    private EventLocation eventLocation;

    /**
     * The reward being allocated to the event location
     * Links to the specific prize that can be won
     */
    @NotNull(message = "Reward is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("rewardId")
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    /**
     * Total quantity of this reward allocated to this event location
     * Overall limit of prizes available at this location
     */
    @NotBlank(message = "Reward quantity is required")
    @Column(name = "quantity", nullable = false)
    @EqualsAndHashCode.Include
    @Builder.Default
    private int quantity = 0;

    /**
     * Daily quota for this reward at this location
     * Limits how many of this reward can be won per day
     */
    @NotBlank(message = "Reward quantity remaining is required")
    @Column(name = "today_quantity", nullable = false)
    @EqualsAndHashCode.Include
    @Builder.Default
    private int todayQuantity = 0;

    /**
     * Set event location with proper bidirectional relationship
     *
     * @param newLocation the location to set
     */
    public void setEventLocation(EventLocation newLocation) {
        EventLocation oldLocation = this.eventLocation;
        if (oldLocation != null && oldLocation.getRewardEvents() != null) {
            oldLocation.getRewardEvents().remove(this);
        }
        this.eventLocation = newLocation;
        if (newLocation != null ) {
            if(newLocation.getRewardEvents() == null) {
                newLocation.setRewardEvents(new HashSet<>());
            }
            newLocation.getRewardEvents().add(this);
        }
    }

    @Override
    public void doPrePersist() {
        super.doPrePersist();
        this.validateState();
    }

    @Override
    public void doPreUpdate() {
        super.doPreUpdate();
        this.validateState();
    }

    /**
     * Validate participation state
     *
     * @throws IllegalStateException if validation fails
     */
    public void validateState() {
        if (eventLocation == null) {
            throw new IllegalStateException("Event location is required");
        }

        if (reward == null) {
            throw new IllegalStateException("Reward is required");
        }

        if (quantity < 0) {
            throw new IllegalStateException("Quantity remaining cannot be negative");
        }

        if (todayQuantity < 0) {
            throw new IllegalStateException("Today quantity remaining cannot be negative");
        }

        if (eventLocation.getMaxSpin() <= 0) {
            throw new IllegalStateException("Maximum spins must be positive");
        }
    }
}
