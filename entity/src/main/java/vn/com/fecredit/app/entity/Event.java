package vn.com.fecredit.app.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "events",
    indexes = {
        @Index(name = "idx_event_code", columnList = "code", unique = true),
        @Index(name = "idx_event_status", columnList = "status"),
        @Index(name = "idx_event_dates", columnList = "start_time, end_time")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"locations", "participantEvents"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Event extends AbstractStatusAwareEntity {

    private static final Logger log = LoggerFactory.getLogger(Event.class);

    @NotBlank(message = "Event name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotBlank(message = "Event code is required")
    @Column(name = "code", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "description")
    private String description;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @OrderBy("id asc")
    @Builder.Default
    private Set<EventLocation> locations = new LinkedHashSet<>();
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ParticipantEvent> participantEvents = new HashSet<>();

    /**
     * Get current server time (used for spins and other time-sensitive operations)
     * @return current time
     */
    @Transient
    public LocalDateTime getCurrentServerTime() {
        return LocalDateTime.now();
    }

    /**
     * Add a location to this event
     * @param location the location to add
     * @throws IllegalArgumentException if location's region overlaps with existing locations
     */
    public void addLocation(EventLocation location) {
        if (location == null) {
            return;
        }

        // Check for overlapping regions/provinces
        if (location.getRegion() != null) {
            for (EventLocation existing : locations) {
                if (existing.getRegion() != null 
                    && !existing.equals(location) 
                    && existing.getRegion().hasOverlappingProvinces(location.getRegion())) {
                    throw new IllegalArgumentException("Cannot add location - region has overlapping provinces");
                }
            }
        }

        locations.add(location);
        if (location.getEvent() != this) {
            location.setEvent(this);
        }
    }

    /**
     * Remove a location from this event
     * @param location the location to remove
     */
    public void removeLocation(EventLocation location) {
        if (location != null && locations.remove(location)) {
            if (location.getEvent() == this) {
                location.setEvent(null);
            }
        }
    }

    /**
     * Add a participant event with proper relationship management
     * @param participantEvent the participant event to add
     * @throws IllegalStateException if event is inactive
     */
    public void addParticipantEvent(ParticipantEvent participantEvent) {
        if (!isActive()) {
            throw new IllegalStateException("Cannot add participants to inactive event");
        }
        
        if (participantEvent != null) {
            participantEvents.add(participantEvent);
            if (participantEvent.getEvent() != this) {
                participantEvent.setEvent(this);
            }
        }
    }

    /**
     * Remove a participant event record
     * @param participantEvent the participant event to remove
     */
    public void removeParticipantEvent(ParticipantEvent participantEvent) {
        if (participantEvent != null && participantEvents.remove(participantEvent)) {
            if (participantEvent.getEvent() == this) {
                participantEvent.setEvent(null);
            }
        }
    }

    /**
     * Check if this event overlaps with another event
     * @param other the other event to check
     * @return true if events overlap in time
     */
    public boolean overlaps(Event other) {
        if (other == null || startTime == null || endTime == null 
            || other.startTime == null || other.endTime == null) {
            return false;
        }
        
        return !endTime.isBefore(other.startTime) && 
               !startTime.isAfter(other.endTime);
    }

    /**
     * Check if the event is currently active
     * @return true if the event is active and within its time period
     */
    @Override
    @Transient
    public boolean isActive() {
        if (startTime == null || endTime == null) {
            return false;
        }

        LocalDateTime now = getCurrentServerTime();
        return super.isActive() && 
               !isDeleted() && 
               startTime.isBefore(now) && 
               endTime.isAfter(now);
    }

    /**
     * Get the default location for this event (first added location)
     * @return the default location or null if no locations
     */
    @Transient
    public EventLocation getDefaultLocation() {
        // Now returns the first added location in insertion order
        return locations.isEmpty() ? null : locations.iterator().next();
    }

    /**
     * Validate the event's time period and constraints
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validateState() {
        if (endTime == null || startTime == null) {
            throw new IllegalStateException("Event start time and end time must be specified");
        }
        
        if (endTime.isBefore(startTime)) {
            throw new IllegalStateException("Event end time cannot be before start time");
        }
        
        if (endTime.equals(startTime)) {
            throw new IllegalStateException("Event end time cannot be equal to start time");
        }

        if (startTime.plusMinutes(30).isAfter(endTime)) {
            throw new IllegalStateException("Event must be at least 30 minutes long");
        }
        
        // Add diagnostic logging
        log.info("Validating event duration - Start: {}, End: {}", startTime, endTime);
        
        // Calculate duration in hours including fractional part
        var duration = java.time.Duration.between(startTime, endTime);
        var durationHours = duration.toHours() + (duration.toMinutesPart() / 60.0);
        
        log.info("Duration in hours: {}", durationHours);
        
        if (durationHours > 24.0) {
            log.error("Event duration exceeds 24 hours - Start: {}, End: {}, Duration: {} hours",
                startTime, endTime, durationHours);
            throw new IllegalStateException("Event cannot be longer than 24 hours");
        }

    }
}
