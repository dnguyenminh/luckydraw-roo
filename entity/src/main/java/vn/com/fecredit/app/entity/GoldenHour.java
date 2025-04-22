package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a special time period with enhanced rewards.
 * <p>
 * GoldenHour defines a specific time range during which participants have
 * increased chances of winning or receive enhanced rewards. Each golden hour
 * is associated with a specific event location and has a multiplier value that
 * affects the rewards won during that period.
 * </p>
 * <p>
 * Golden hours are used to drive participation during specific times and create
 * urgency for participants to engage with the lucky draw system.
 * </p>
 */
@Entity
@Table(name = "golden_hours", indexes = {
        @Index(name = "idx_golden_hour_location", columnList = "region_id, event_id"),
        @Index(name = "idx_golden_hour_time", columnList = "start_time, end_time"),
        @Index(name = "idx_golden_hour_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = { "eventLocation" })
@EqualsAndHashCode(callSuper = true)
public class GoldenHour extends AbstractSimplePersistableEntity<Long> {

    /**
     * The event location this golden hour applies to
     * Links to the specific physical location for an event
     */
    @NotNull(message = "Event location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false),
        @JoinColumn(name = "region_id", referencedColumnName = "region_id", nullable = false)
    })
    private EventLocation eventLocation;

    /**
     * Start date and time of the golden hour period
     * Defines when the enhanced rewards begin
     */
    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * End date and time of the golden hour period
     * Defines when the enhanced rewards end
     */
    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * The multiplier applied to rewards won during this period
     * Values greater than 1.0 indicate enhanced rewards
     */
    @DecimalMin(value = "1.0", message = "Multiplier must be at least 1.0")
    @Column(name = "multiplier", nullable = false)
    @Builder.Default
    private BigDecimal multiplier = BigDecimal.ONE;

    /**
     * Optional maximum number of enhanced rewards available during this period
     * Used to limit the total enhanced rewards that can be won
     */
    @Min(value = 0, message = "Maximum rewards cannot be negative")
    @Column(name = "max_rewards")
    private Integer maxRewards;

    /**
     * Optional field to track how many enhanced rewards have been claimed
     * Used to enforce the maximum rewards limit
     */
    @Min(value = 0, message = "Claimed rewards cannot be negative")
    @Column(name = "claimed_rewards")
    @Builder.Default
    private Integer claimedRewards = 0;

    /**
     * Check if a given date-time falls within this golden hour period
     *
     * @param dateTime the date-time to check
     * @return true if the date-time is within the golden hour period
     */
    @Transient
    public boolean isActive(LocalDateTime dateTime) {
        return getStatus().isActive() &&
                !dateTime.isBefore(startTime) &&
                !dateTime.isAfter(endTime);
    }

    /**
     * Check if the current time falls within this golden hour period
     *
     * @return true if the current time is within the golden hour period
     */
    @Transient
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        if (eventLocation != null && eventLocation.getEvent() != null) {
            now = eventLocation.getEvent().getCurrentServerTime();
        }
        return isActive(now);
    }

    /**
     * Check if this golden hour overlaps with another golden hour
     *
     * @param other the other golden hour to check
     * @return true if the golden hours overlap in time
     */
    public boolean overlaps(GoldenHour other) {
        if (other == null || startTime == null || endTime == null
                || other.startTime == null || other.endTime == null) {
            return false;
        }

        // Two time periods overlap if the start of one is before the end of the other
        // and the end of the first is after the start of the other
        return !endTime.isBefore(other.startTime) && !startTime.isAfter(other.endTime);
    }

    /**
     * Set the eventLocation with proper bidirectional relationship management
     *
     * @param newLocation the new event location
     */
    public void setEventLocation(EventLocation newLocation) {
        EventLocation oldLocation = this.eventLocation;

        if (oldLocation != null && oldLocation.getGoldenHours() != null) {
            oldLocation.getGoldenHours().remove(this);
        }

        this.eventLocation = newLocation;

        if (newLocation != null && newLocation.getGoldenHours() != null) {
            newLocation.getGoldenHours().add(this);
        }
    }

    @Override
    public void doPrePersist() {
        super.doPrePersist();
        validateState();
    }

    @Override
    public void doPreUpdate() {
        super.doPreUpdate();
        validateState();
    }

    /**
     * Validate the golden hour state
     *
     * @throws IllegalStateException if validation fails
     */
    public void validateState() {
        if (startTime == null) {
            throw new IllegalStateException("Start time is required");
        }

        if (endTime == null) {
            throw new IllegalStateException("End time is required");
        }

        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new IllegalStateException("End time must be after start time");
        }

        if (eventLocation == null) {
            throw new IllegalStateException("Event location is required");
        }

        if (getStatus().isActive() && !eventLocation.getStatus().isActive()) {
            throw new IllegalStateException("Cannot activate golden hour for inactive event location");
        }

        if (multiplier == null || multiplier.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalStateException("Multiplier must be at least 1.0");
        }

        if (maxRewards != null && maxRewards < 0) {
            throw new IllegalStateException("Maximum rewards cannot be negative");
        }

        if (claimedRewards != null && claimedRewards < 0) {
            throw new IllegalStateException("Claimed rewards cannot be negative");
        }

        if (maxRewards != null && claimedRewards != null && claimedRewards > maxRewards) {
            throw new IllegalStateException("Claimed rewards cannot exceed maximum rewards");
        }
    }
}
