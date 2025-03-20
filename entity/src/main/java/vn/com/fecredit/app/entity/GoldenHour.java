package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "golden_hours", indexes = {
        @Index(name = "idx_golden_hour_location", columnList = "location_id"),
        @Index(name = "idx_golden_hour_status", columnList = "status"),
        @Index(name = "idx_golden_hour_time", columnList = "start_time, end_time")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class GoldenHour extends AbstractStatusAwareEntity {

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @DecimalMin(value = "1.0", message = "Multiplier must be at least 1.0")
    @DecimalMax(value = "10.0", message = "Multiplier must be at most 10.0")
    @Column(name = "multiplier", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal multiplier = BigDecimal.ONE;

    @NotNull(message = "Location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private EventLocation eventLocation;

    public EventLocation getEventLocation() {
        return eventLocation;
    }

    /**
     * Set location with proper bidirectional relationship management
     * 
     * @param newLocation the location to set
     */
    public void setEventLocation(EventLocation newLocation) {
        EventLocation oldLocation = this.eventLocation;

        // Remove from old location
        if (oldLocation != null && oldLocation.getGoldenHours() != null) {
            oldLocation.getGoldenHours().remove(this);
        }

        this.eventLocation = newLocation;

        // Add to new location
        if (newLocation != null && newLocation.getGoldenHours() != null) {
            newLocation.getGoldenHours().add(this);
        }
    }

    /**
     * Check if golden hour is active at the given time
     * 
     * @param time the time to check
     * @return true if golden hour is active
     */
    public boolean isActive(LocalDateTime time) {
        return getStatus().isActive() &&
                !time.isBefore(startTime) &&
                !time.isAfter(endTime);
    }

    /**
     * Check if golden hour is currently active
     * 
     * @return true if active
     */
    @Transient
    public boolean isActive() {
        return isActive(LocalDateTime.now());
    }

    /**
     * Check if this golden hour overlaps with another
     * 
     * @param other the other golden hour to check
     * @return true if there is overlap
     */
    public boolean overlaps(GoldenHour other) {
        if (other == null || eventLocation == null || other.getEventLocation() == null) {
            return false;
        }

        return eventLocation.equals(other.getEventLocation()) &&
                !endTime.isBefore(other.getStartTime()) &&
                !startTime.isAfter(other.getEndTime());
    }

    /**
     * Validate golden hour state
     * 
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validateState() {
        if (startTime == null || endTime == null) {
            throw new IllegalStateException("Start time and end time must be specified");
        }

        if (endTime.isBefore(startTime)) {
            throw new IllegalStateException("End time must be after start time");
        }

        if (eventLocation == null) {
            throw new IllegalStateException("Location must be specified");
        }

        // Validate time window is within event time
        Event event = eventLocation.getEvent();
        if (event != null) {
            if (startTime.isBefore(event.getStartTime())) {
                throw new IllegalStateException("Golden hour cannot start before event");
            }
            if (endTime.isAfter(event.getEndTime())) {
                throw new IllegalStateException("Golden hour cannot end after event");
            }
        }

        if (multiplier == null || multiplier.compareTo(BigDecimal.ONE) < 0
                || multiplier.compareTo(BigDecimal.valueOf(10)) > 0) {
            throw new IllegalStateException("Multiplier must be between 1.0 and 10.0");
        }
    }
}
