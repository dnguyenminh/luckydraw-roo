package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull; // Add this import for NotNull
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * SpinHistory entity that records each spin attempt in a lucky draw.
 * Tracks when a spin was made, by which participant, at which event location,
 * whether it resulted in a win, and any associated rewards or bonus
 * multipliers.
 *
 * This entity forms the core activity record for the lucky draw system.
 */
@Entity
@Table(name = "spin_histories", indexes = {
        @Index(name = "idx_spin_participant", columnList = "participant_event_id"),
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
@ToString(callSuper = true, exclude = { "participantEvent", "reward", "goldenHour" })
public class SpinHistory extends AbstractStatusAwareEntity {

    /**
     * The exact time when the spin was performed
     */
    @Column(name = "spin_time", nullable = false)
    @NotNull
    private LocalDateTime spinTime;

    /**
     * The participant-event record that performed this spin
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_event_id", nullable = false)
    private ParticipantEvent participantEvent;

    /**
     * The reward that was won (if any)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    /**
     * The golden hour in effect during this spin (if any)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "golden_hour_id")
    private GoldenHour goldenHour;

    /**
     * Whether this spin resulted in winning a reward
     * Note: Column name is 'win' in the database
     */
    @Column(name = "win")
    @Builder.Default
    private boolean win = false; // Change field name to match database column

    /**
     * Multiplier applied to reward value (e.g., from golden hour)
     */
    @Column(name = "multiplier")
    @Builder.Default
    private BigDecimal multiplier = BigDecimal.ONE;

    /**
     * Temporary unique identifier for new spins that haven't been persisted
     * Used for equality checks in collections before database persistence
     */
    @Transient
    private final String tempId = UUID.randomUUID().toString();

    /**
     * Set whether this spin is a winning spin (builder-style method)
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
     * 
     * @param win true if this spin won a reward
     */
    public void setWin(boolean win) {
        this.win = win;
    }

    /**
     * Standard Java boolean getter - matches JPA conventions
     * 
     * @return true if this spin is a win
     */
    public boolean isWin() {
        return this.win;
    }

    /**
     * Get the multiplier value considering golden hour
     * 
     * @return the effective multiplier
     */
    @Transient
    public BigDecimal getMultiplier() {
        if (this.multiplier != null && this.multiplier.compareTo(BigDecimal.ZERO) > 0) {
            return this.multiplier;
        }

        if (goldenHour != null && goldenHour.getMultiplier() != null) {
            return goldenHour.getMultiplier();
        }

        return BigDecimal.ONE;
    }

    /**
     * Creates a new spin history for a participant event
     */
    public static SpinHistory createNewSpin(ParticipantEvent participantEvent) {
        return SpinHistory.builder()
                .participantEvent(participantEvent)
                .spinTime(LocalDateTime.now())
                .win(false)
                .build();
    }

    /**
     * Creates a winning spin history with a reward
     */
    public static SpinHistory createWinningSpin(ParticipantEvent participantEvent, Reward reward) {
        return SpinHistory.builder()
                .participantEvent(participantEvent)
                .reward(reward)
                .spinTime(LocalDateTime.now())
                .win(true)
                .build();
    }

    /**
     * Creates a losing spin history
     */
    public static SpinHistory createLosingSpin(ParticipantEvent participantEvent) {
        return SpinHistory.builder()
                .participantEvent(participantEvent)
                .spinTime(LocalDateTime.now())
                .win(false)
                .build();
    }

    /**
     * Get the event location of this spin through the participant event
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

        if (isWin() && reward == null) {
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
