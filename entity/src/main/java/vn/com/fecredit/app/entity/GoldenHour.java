package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GoldenHour entity representing special time periods during events
 * when reward values are multiplied.
 */
@Entity
@Table(
    name = "golden_hours",
    indexes = {
        @Index(name = "idx_golden_hour_location", columnList = "event_location_id"),
        @Index(name = "idx_golden_hour_time", columnList = "start_time, end_time"),
        @Index(name = "idx_golden_hour_status", columnList = "status")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "eventLocation")
@EqualsAndHashCode(callSuper = true)
public class GoldenHour extends AbstractStatusAwareEntity {

    @NotNull(message = "Event location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id", nullable = false)
    private EventLocation eventLocation;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @DecimalMin(value = "1.0", message = "Multiplier must be at least 1.0")
    @Column(name = "multiplier", nullable = false)
    @Builder.Default
    private BigDecimal multiplier = BigDecimal.ONE;

    /**
     * Check if the given time falls within this golden hour
     * @param checkTime the time to check
     * @return true if the time is within the golden hour period
     */
    @Transient
    public boolean isActive(LocalDateTime checkTime) {
        return getStatus().isActive() &&
               !checkTime.isBefore(startTime) &&
               !checkTime.isAfter(endTime);
    }

    /**
     * Check if this golden hour is currently active
     * @return true if currently active
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
    }
}
