package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "spin_histories",
    indexes = {
        @Index(name = "idx_spin_participant", columnList = "participant_event_id"),
        @Index(name = "idx_spin_reward", columnList = "reward_id"),
        @Index(name = "idx_spin_golden_hour", columnList = "golden_hour_id"),
        @Index(name = "idx_spin_time", columnList = "spin_time"),
        @Index(name = "idx_spin_status", columnList = "status")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"participantEvent", "reward", "goldenHour"})
@EqualsAndHashCode(callSuper = true)
public class SpinHistory extends AbstractStatusAwareEntity {

    @Column(name = "spin_time", nullable = false)
    private LocalDateTime spinTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_event_id", nullable = false)
    private ParticipantEvent participantEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "golden_hour_id")
    private GoldenHour goldenHour;

    @Column(name = "win", nullable = false)
    @Builder.Default
    private boolean win = false;

    @Column(name = "multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal multiplier = BigDecimal.ONE;

    // New transient unique field for unsaved entities
    @Transient
    private final String tempId = UUID.randomUUID().toString();

    /**
     * Calculate effective value considering golden hour multiplier
     * @return effective value or zero if no reward
     */
    @Transient
    public BigDecimal calculateEffectiveValue() {
        if (reward == null || !win) {
            return BigDecimal.ZERO;
        }

        return reward.getValue().multiply(multiplier);
    }

    /**
     * Check if this spin was a win
     * @return true if this spin won a reward
     */
    public boolean isWin() {
        return win;
    }

    /**
     * Set whether this spin was a win
     * @param win true if this spin won a reward
     */
    public void setWin(boolean win) {
        this.win = win;
    }

    /**
     * Get the multiplier for this spin
     * @return the multiplier value
     */
    public BigDecimal getMultiplier() {
        return multiplier != null ? multiplier : BigDecimal.ONE;
    }

    /**
     * Set the multiplier for this spin
     * @param multiplier the multiplier to set
     */
    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier != null ? multiplier : BigDecimal.ONE;
    }

    /**
     * Validate spin history state
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validateState() {
        if (spinTime == null) {
            throw new IllegalStateException("Spin time must be specified");
        }

        if (participantEvent == null) {
            throw new IllegalStateException("Participant event must be specified");
        }

        if (win && reward == null) {
            throw new IllegalStateException("Winning spin must have a reward");
        }

        if (multiplier != null && multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Multiplier must be positive");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
