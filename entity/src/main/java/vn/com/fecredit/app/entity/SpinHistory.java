package vn.com.fecredit.app.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

/**
 * Entity representing a single spin attempt in the lucky draw.
 * <p>
 * SpinHistory records each individual attempt by a participant to win a reward
 * through the lucky draw mechanism. It captures the time of the spin, whether
 * it resulted in a win, and the reward won (if any).
 * </p>
 * <p>
 * This entity provides a complete audit trail of all spins and serves as the
 * foundation for analytics, fraud prevention, and participant engagement tracking.
 * </p>
 */
@Entity
@Table(name = "spin_histories", indexes = {
    @Index(name = "idx_spin_participant_event", columnList = "participant_id, participant_region_id, participant_event_id"),
    @Index(name = "idx_spin_reward_event", columnList = "reward_id, reward_region_id, reward_event_id"),
    @Index(name = "idx_spin_reward", columnList = "reward_id"),
    @Index(name = "idx_spin_golden_hour", columnList = "golden_hour_id"),
    @Index(name = "idx_spin_time", columnList = "spin_time"),
    @Index(name = "idx_spin_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"participantEvent", "rewardEvent", "goldenHour"})
public class SpinHistory extends AbstractSimplePersistableEntity<Long> {

    /**
     * The timestamp when this spin was performed
     * Required for tracking spin timing and analytics
     * Also used for golden hour and daily limit calculations
     */
    @Column(name = "spin_time", nullable = false)
    @NotNull
    private LocalDateTime spinTime;


    /**
     * The participant-event record that performed this spin
     * Links the spin to the specific participant at a specific event location
     * Required relationship ensuring every spin is associated with a participant
     * Uses composite join columns to match the participant event composite key
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "participant_id", referencedColumnName = "participant_id"),
        @JoinColumn(name = "participant_event_id", referencedColumnName = "event_id"),
        @JoinColumn(name = "participant_region_id", referencedColumnName = "region_id")
    })
    private ParticipantEvent participantEvent;
    
    /**
     * The reward that was won (if any)
     * Optional relationship - populated only for winning spins
     * Uses composite join columns to match the reward event composite key
     * Null for non-winning spins
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "reward_id", referencedColumnName = "reward_id"),
            @JoinColumn(name = "reward_event_id", referencedColumnName = "event_id"),
            @JoinColumn(name = "reward_region_id", referencedColumnName = "region_id")
    })
    private RewardEvent rewardEvent;

    /**
     * The golden hour in effect during this spin (if any)
     * Optional relationship - populated only when a spin occurs during a golden hour
     * Used for applying multipliers and special conditions to the spin result
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "golden_hour_id")
    private GoldenHour goldenHour;

    /**
     * Whether this spin resulted in winning a reward
     * Note: Column name is 'win' in the database
     * Provides quick access to win status without checking reward relationship
     * Should be true if and only if rewardEvent is not null
     */
    @Column(name = "win")
    @Builder.Default
    private boolean win = false; // Column name is 'win' in database

    /**
     * Temporary unique identifier for new spins that haven't been persisted
     * Used for equality checks in collections before database persistence
     * Generated once when the object is created and never changed
     */
    @Transient
    private final String tempId = UUID.randomUUID().toString();

    /**
     * Set whether this spin is a winning spin (builder-style method)
     * Returns the instance for method chaining
     *
     * @param win true if this spin won a reward
     * @return this spin history for chaining
     */
    public SpinHistory win(boolean win) {
        this.win = win;
        return this;
    }

    /**
     * Setter that exactly matches the database column name
     * Standard setter for the win property
     *
     * @param win true if this spin won a reward
     */
    public void setWin(boolean win) {
        this.win = win;
    }

    /**
     * Standard Java boolean getter - matches JPA conventions
     * Returns whether this spin resulted in a win
     *
     * @return true if this spin is a win
     */
    public boolean isWin() {
        return this.win;
    }

    /**
     * Creates a new spin history for a participant event.
     * Factory method for creating a default spin with current timestamp
     *
     * @param participantEvent The participant event performing the spin
     * @return A new SpinHistory instance with default values and current timestamp
     */
    public static SpinHistory createNewSpin(ParticipantEvent participantEvent) {
        return SpinHistory.builder()
            .participantEvent(participantEvent)
            .spinTime(LocalDateTime.now())
            .win(false)
            .build();
    }

    /**
     * Creates a winning spin history with a reward.
     *
     * @param participantEvent The participant event performing the spin
     * @param rewardEvent      The reward won by this spin
     * @return A new SpinHistory instance marked as winning with the specified
     * reward
     */
    public static SpinHistory createWinningSpin(ParticipantEvent participantEvent, RewardEvent rewardEvent) {
        return SpinHistory.builder()
            .participantEvent(participantEvent)
            .rewardEvent(rewardEvent)
            .spinTime(LocalDateTime.now())
            .win(true)
            .build();
    }

    /**
     * Creates a losing spin history.
     *
     * @param participantEvent The participant event performing the spin
     * @return A new SpinHistory instance marked as losing with no reward
     */
    public static SpinHistory createLosingSpin(ParticipantEvent participantEvent) {
        return SpinHistory.builder()
            .participantEvent(participantEvent)
            .spinTime(LocalDateTime.now())
            .win(false)
            .build();
    }

    /**
     * Get the event location of this spin through the participant event.
     *
     * @return The event location where this spin occurred, or null if participant
     * event is missing
     */
    public EventLocation getEventLocation() {
        if (participantEvent == null) {
            return null;
        }
        return participantEvent.getEventLocation();
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
     * Validate spin history state
     *
     * @throws IllegalStateException if validation fails
     */
    public void validateState() {
        if (spinTime == null) {
            throw new IllegalStateException("Spin time must be specified");
        }

        if (participantEvent == null) {
            throw new IllegalStateException("Participant event must be specified");
        }

        if (isWin() && rewardEvent == null) {
            throw new IllegalStateException("Winning spin must have a reward");
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SpinHistory that = (SpinHistory) o;

        // Prefer using the persisted ID if present
        if (this.getId() != null || that.getId() != null) {
            return Objects.equals(this.getId(), that.getId());
        }
        // Fall back to the temporary unique identifier
        return Objects.equals(this.tempId, that.tempId);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hashCode(getId());
        }
        return Objects.hash(tempId);
    }
}
