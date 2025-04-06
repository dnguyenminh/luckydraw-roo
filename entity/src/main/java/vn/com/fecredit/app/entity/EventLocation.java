package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.base.StatusAware;
import vn.com.fecredit.app.entity.enums.CommonStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Slf4j
@Table(
    name = "event_locations",
    indexes = {
        @Index(name = "idx_location_code", columnList = "code", unique = true),
        @Index(name = "idx_location_event", columnList = "event_id"),
        @Index(name = "idx_location_region", columnList = "region_id"),
        @Index(name = "idx_location_status", columnList = "status")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"event", "region", "participantEvents", "rewards", "goldenHours"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class EventLocation extends AbstractStatusAwareEntity {

    @NotBlank(message = "Location name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Location code is required")
    @Column(name = "code", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "description")
    private String description;

    @Min(value = 1, message = "Maximum spins must be at least 1")
    @Column(name = "max_spin", nullable = false)
    @Builder.Default
    private Integer maxSpin = 100;
    
    @Min(value = 0, message = "Quantity must be non-negative")
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @DecimalMin(value = "0.0", message = "Win probability must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Win probability must be between 0 and 1")
    @Column(name = "win_probability", nullable = false)
    @Builder.Default
    private Double winProbability = 0.1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @NotNull(message = "Region is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<ParticipantEvent> participantEvents = new HashSet<>();

    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Reward> rewards = new HashSet<>();

    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<GoldenHour> goldenHours = new HashSet<>();

    /**
     * Add a reward to this location
     * @param reward the reward to add
     */
    public void addReward(Reward reward) {
        if (reward != null) {
            rewards.add(reward);
            if (reward.getEventLocation() != this) {
                reward.setEventLocation(this);
            }
        }
    }

    /**
     * Remove a reward from this location
     * @param reward the reward to remove
     */
    public void removeReward(Reward reward) {
        if (reward != null && rewards.remove(reward)) {
            if (reward.getEventLocation() == this) {
                reward.setEventLocation(null);
            }
        }
    }

    /**
     * Add a golden hour to this location
     * @param goldenHour the golden hour to add
     */
    public void addGoldenHour(GoldenHour goldenHour) {
        if (goldenHour != null) {
            goldenHours.add(goldenHour);
            if (goldenHour.getEventLocation() != this) {
                goldenHour.setEventLocation(this);
            }
        }
    }

    /**
     * Remove a golden hour from this location
     * @param goldenHour the golden hour to remove
     */
    public void removeGoldenHour(GoldenHour goldenHour) {
        if (goldenHour != null && goldenHours.remove(goldenHour)) {
            if (goldenHour.getEventLocation() == this) {
                goldenHour.setEventLocation(null);
            }
        }
    }

    /**
     * Add a participant event to this location
     * @param participantEvent the participant event to add
     */
    public void addParticipantEvent(ParticipantEvent participantEvent) {
        if (participantEvent != null) {
            participantEvents.add(participantEvent);
            if (participantEvent.getEventLocation() != this) {
                participantEvent.setEventLocation(this);
            }
        }
    }

    /**
     * Remove a participant event from this location
     * @param participantEvent the participant event to remove
     */
    public void removeParticipantEvent(ParticipantEvent participantEvent) {
        if (participantEvent != null && participantEvents.remove(participantEvent)) {
            if (participantEvent.getEventLocation() == this) {
                participantEvent.setEventLocation(null);
            }
        }
    }

    /**
     * Set region with proper bidirectional relationship management
     * @param newRegion the new region
     */
    public void setRegion(Region newRegion) {
        Region oldRegion = this.region;
        if (oldRegion != null && oldRegion.getEventLocations() != null) {
            oldRegion.getEventLocations().remove(this);
        }
        this.region = newRegion;
        if (newRegion != null && newRegion.getEventLocations() != null) {
            newRegion.getEventLocations().add(this);
            if (getStatus().isActive() && !newRegion.getStatus().isActive()) {
                setStatus(CommonStatus.INACTIVE);
            }
        }
    }

    /**
     * Set event with proper bidirectional relationship management
     * @param newEvent the new event
     */
    public void setEvent(Event newEvent) {
        Event oldEvent = this.event;
        if (oldEvent != null && oldEvent.getLocations() != null) {
            oldEvent.getLocations().remove(this);
        }
        this.event = newEvent;
        if (newEvent != null && newEvent.getLocations() != null) {
            newEvent.getLocations().add(this);
        }
    }

    /**
     * Check if location has available capacity
     * @return true if capacity available
     */
    /**
     * Check if location has available capacity based on active participant events
     * @return true if capacity available
     */
    @Transient
    public boolean hasAvailableCapacity() {
        if (participantEvents == null) {
            return true;
        }
        long activeParticipants = participantEvents.stream()
            .filter(pe -> pe.getStatus().isActive() && pe.getEventLocation() == this)
            .count();
        return activeParticipants < maxSpin;
    }

    /**
     * Check if location is available for an event
     * @param otherEvent the event to check
     * @return true if available
     */
    public boolean isAvailable(Event otherEvent) {
        if (event != null && event.equals(otherEvent)) {
            return false;
        }

        if (event != null && otherEvent != null) {
            return !event.overlaps(otherEvent);
        }

        return true;
    }

    /**
     * Set status and cascade changes to dependent entities
     *
     * @param newStatus the new status
     * @return this instance for method chaining
     */
    @Override
    public StatusAware setStatus(CommonStatus newStatus) {
        // Check if we can change to active status
        if (newStatus != null && newStatus.isActive() && region != null && !region.getStatus().isActive()) {
            throw new IllegalStateException("Cannot activate location when region is inactive");
        }

        super.setStatus(newStatus);

        // Cascade deactivation to dependent entities
        if (newStatus != null && !newStatus.isActive()) {
            participantEvents.forEach(pe -> pe.setStatus(CommonStatus.INACTIVE));
            rewards.forEach(r -> r.setStatus(CommonStatus.INACTIVE));
            goldenHours.forEach(gh -> gh.setStatus(CommonStatus.INACTIVE));
        }

        validateState();
        return this; // Fixed: was returning null
    }

    /**
     * Mark the event location as active
     * This method sets the status to ACTIVE
     */
    public void markAsActive() {
        this.setStatus(CommonStatus.ACTIVE);
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
     * Validate location state
     * @throws IllegalStateException if validation fails
     */
    public void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Location name must be specified");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Location code must be specified");
        }

        if (region == null) {
            throw new IllegalStateException("Region must be specified");
        }

        if (maxSpin == null || maxSpin < 1) {
            throw new IllegalStateException("Maximum spins must be at least 1");
        }
        
        if (quantity == null || quantity < 0) {
            throw new IllegalStateException("Quantity must be non-negative");
        }

        if (winProbability == null || winProbability < 0.0 || winProbability > 1.0) {
            throw new IllegalStateException("Win probability must be between 0 and 1");
        }

        // Debug logging for class loading issue
        if (region != null) {
            var status = region.getStatus();
            log.debug("Region status class: {}", status.getClass().getName());
            log.debug("Region status methods: {}", Arrays.toString(status.getClass().getMethods()));
            log.debug("Region status value: {}", status);
        }

        if (getStatus().isActive() && (region == null || !region.getStatus().isActive())) {
            throw new IllegalStateException("Location cannot be active in inactive region");
        }
    }
}
