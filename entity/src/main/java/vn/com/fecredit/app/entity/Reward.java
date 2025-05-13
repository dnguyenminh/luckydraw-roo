package vn.com.fecredit.app.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;
import vn.com.fecredit.app.entity.base.StatusAware; // Changed from interfaces to base package
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Represents a reward that can be won in the lucky draw system.
 * Rewards are associated with specific event locations and have attributes
 * like probability, quantity, and status.
 */
@Entity
@Table(name = "rewards", indexes = {
    @Index(name = "idx_reward_code", columnList = "code", unique = true),
    @Index(name = "idx_reward_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"rewardEvents"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Reward extends AbstractSimplePersistableEntity<Long> {

    @NotBlank(message = "Reward name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Reward code is required")
    @Column(name = "code", nullable = false)
    @EqualsAndHashCode.Include
    private String code;

    // @NotBlank(message = "Reward today quantity remaining is required")
    // @Column(name = "todayRemaining", nullable = false)
    // @EqualsAndHashCode.Include
    // @Builder.Default
    // private Integer todayRemaining = 0;

    @Column(name = "description")
    private String description;

    /**
     * Monetary value of the reward.
     * Column name is quoted because 'value' is a reserved keyword in H2.
     */
    @Column(name = "prize_value")
    private BigDecimal prizeValue;

    @OneToMany(mappedBy = "reward", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<RewardEvent> rewardEvents = new HashSet<>();

    // @Transient
    // public boolean isAvailable() {
    // return getStatus().isActive() &&
    // eventLocation != null &&
    // eventLocation.getStatus().isActive() &&
    // todayRemaining > 0;
    // }

    // /**
    // * Sets the event location for this reward with proper bidirectional
    // relationship management.
    // * Removes the reward from the old location's collection and adds it to the
    // new location's.
    // *
    // * @param newLocation The new event location to associate this reward with
    // */
    // public void setEventLocation(RewardEvent rewardEvent) {
    // EventLocation oldLocation = this.eventLocation;

    // if (oldLocation != null && oldLocation.getRewards() != null) {
    // oldLocation.getRewards().remove(this);
    // }

    // this.eventLocation = newLocation;

    // if (newLocation != null && newLocation.getRewards() != null) {
    // newLocation.getRewards().add(this);
    // }
    // }

    public void addRewardEvent(RewardEvent rewardEvent) {
        if (null != rewardEvent && !rewardEvents.contains(rewardEvent)) {
            rewardEvents.add(rewardEvent);
            rewardEvent.setReward(this);
        }
    }

    public void removeRewardEvent(RewardEvent rewardEvent) {
        if (null != rewardEvent && !rewardEvents.contains(rewardEvent)) {
            rewardEvents.remove(rewardEvent);
            rewardEvent.setReward(null);
        }
    }

    @Override
    public StatusAware setStatus(CommonStatus newStatus) {
        return super.setStatus(newStatus);
    }

    /**
     * Marks this reward as active.
     * Will throw an exception if the event location is inactive.
     *
     * @throws IllegalStateException if the event location is inactive or null
     */
    public void markAsActive() {
        super.setStatus(CommonStatus.ACTIVE);
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
     * Validates the state of this reward.
     * Ensures that required fields are populated and consistent.
     *
     * @throws IllegalStateException if validation fails
     */
    public void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Reward name is required");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Reward code is required");
        }

    }

}
