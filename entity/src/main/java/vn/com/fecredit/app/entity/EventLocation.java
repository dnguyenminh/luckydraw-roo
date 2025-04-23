package vn.com.fecredit.app.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.base.AbstractComplexPersistableEntity;
import vn.com.fecredit.app.entity.base.StatusAware;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Entity representing a physical location where events take place.
 * <p>
 * EventLocation links events to specific geographical locations and manages
 * capacity tracking, reward distribution, and participant management for a given
 * location. It maintains relationships with regions, events, rewards, golden hours, and
 * participant events.
 * </p>
 *
 * <p>
 * The default no-argument constructor is provided by Lombok's
 * {@code @NoArgsConstructor} annotation and is required for JPA entity instantiation.
 * </p>
 */
@Entity
@Slf4j
@Table(name = "event_locations", indexes = {
    @Index(name = "idx_location_event", columnList = "event_id"),
    @Index(name = "idx_location_region", columnList = "region_id"),
    @Index(name = "idx_location_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Creates a default no-args constructor required by JPA
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"event", "region", "participantEvents", "rewardEvents", "goldenHours"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class EventLocation extends AbstractComplexPersistableEntity<EventLocationKey> {

    /**
     * Additional description about the event location
     * Provides more details about the location's facilities or characteristics
     */
    @Column(name = "description")
    private String description;

    /**
     * Maximum number of spins allowed at this location
     * Represents the capacity limit for participation at this venue
     * Must be a non-negative value
     */
    @Min(value = 0, message = "Maximum spins must be not negative number")
    @Column(name = "max_spin", nullable = false)
    @Builder.Default
    private int maxSpin = 100;

    /**
     * Number of spins allowed today at this location
     * Used for daily quotas to manage participation rates
     * Must be a non-negative value
     */
    @Min(value = 0, message = "Today spins must be not negative number")
    @Column(name = "today_spin", nullable = false)
    @Builder.Default
    private int todaySpin = 100;

    /**
     * Rate at which daily spins are distributed
     * Controls the distribution of spins throughout the day
     * Must be a non-negative value
     * 
     * This rate determines how quickly the daily spin allocation is consumed
     * A higher rate means spins are distributed faster throughout the day
     */
    @Min(value = 0, message = "Remaining spins must be not negative number")
    @Column(name = "daily_spin_dist_rate", nullable = false)
    @Builder.Default
    private double dailySpinDistributingRate = 0;

    /**
     * Parent event to which this location belongs
     * Establishes a many-to-one relationship with Event entity
     * Forms part of the composite primary key (EventLocationKey)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    /**
     * Geographical region where this location is situated
     * Required field establishing a many-to-one relationship with Region entity
     * Forms part of the composite primary key (EventLocationKey)
     */
    @NotNull(message = "Region is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("regionId")
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    /**
     * Collection of participant events occurring at this location
     * Tracks participant engagement through a one-to-many relationship
     */
    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<ParticipantEvent> participantEvents = new HashSet<>();

    /**
     * Collection of rewards available at this location
     * Manages the prizes that can be won at this specific venue
     */
    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<RewardEvent> rewardEvents = new HashSet<>();

    /**
     * Collection of golden hours scheduled at this location
     * Defines special time periods with increased rewards or chances
     */
    @OneToMany(mappedBy = "eventLocation", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<GoldenHour> goldenHours = new HashSet<>();

    /**
     * Add a reward to this location with proper bidirectional relationship
     * This method ensures the reward event references this location as its owner
     *
     * @param rewardEvent the rewardEvent to add to this location
     */
    public void addRewardEvent(RewardEvent rewardEvent) {
        if (rewardEvent != null) {
            rewardEvents.add(rewardEvent);
            if (rewardEvent.getEventLocation() != this) {
                rewardEvent.setEventLocation(this);
            }
        }
    }

    /**
     * Add a golden hour to this location with proper bidirectional relationship
     * This method ensures the golden hour references this location as its owner
     *
     * @param goldenHour the golden hour to add to this location
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
     * Remove a golden hour from this location with proper relationship cleanup
     * This ensures bidirectional relationship consistency is maintained
     *
     * @param goldenHour the golden hour to remove from this location
     */
    public void removeGoldenHour(GoldenHour goldenHour) {
        if (goldenHour != null && goldenHours.remove(goldenHour)) {
            if (goldenHour.getEventLocation() == this) {
                goldenHour.setEventLocation(null);
            }
        }
    }

    /**
     * Add a participant event to this location with proper bidirectional relationship
     * This method ensures the participant event references this location as its owner
     *
     * @param participantEvent the participant event to add to this location
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
     * Remove a participant event from this location with proper relationship cleanup
     * This ensures bidirectional relationship consistency is maintained
     *
     * @param participantEvent the participant event to remove from this location
     */
    public void removeParticipantEvent(RewardEvent participantEvent) {
        if (participantEvent != null && participantEvents.remove(participantEvent)) {
            if (participantEvent.getEventLocation() == this) {
                participantEvent.setEventLocation(null);
            }
        }
    }

    /**
     * Set region with proper bidirectional relationship management
     * Updates both sides of the relationship and handles the previous relationship cleanup
     * Also updates status based on region status and initializes the composite key
     *
     * @param newRegion the new region to associate with this location
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

        // Initialize or update the ID whenever the region is set
        initializeOrUpdateId();
    }

    /**
     * Set event with proper bidirectional relationship management
     * Updates both sides of the relationship and handles the previous relationship cleanup
     * Also initializes the composite key with the new event ID
     *
     * @param newEvent the new event to associate with this location
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

        // Initialize or update the ID whenever the event is set
        initializeOrUpdateId();
    }

    /**
     * Initialize or update the composite ID based on event and region
     * This private helper method ensures the ID is always consistent with the relationship entities
     * Called whenever event or region is set to maintain ID integrity
     */
    private void initializeOrUpdateId() {
        // Only try to create the ID when both event and region are present
        if (this.event != null && this.region != null) {
            // Get existing ID or create new one
            EventLocationKey key = getId();
            if (key == null) {
                key = new EventLocationKey();
                setId(key);
            }

            // Set the ID fields
            key.setEventId(this.event.getId());
            key.setRegionId(this.region.getId());
        }
    }

    /**
     * Check if location has available capacity based on active participant events
     * Calculates whether more participants can be added to this location
     * based on the maximum spin limit and current participant count
     *
     * @return true if there is available capacity for more participants
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
     * Set status and cascade changes to dependent entities
     *
     * @param newStatus the new status
     * @return this instance for method chaining
     */
    @Override
    public StatusAware setStatus(CommonStatus newStatus) {
        // Check if we can change to active status
        if (newStatus != null && newStatus.isActive()) {
            if (region != null && !region.getStatus().isActive()) {
                throw new IllegalStateException("Cannot activate location when region is inactive");
            } else if (event != null && !event.getStatus().isActive()) {
                throw new IllegalStateException("Cannot activate location when event is inactive");
            }
        }

        super.setStatus(newStatus);

//        // Cascade deactivation to dependent entities
//        if (newStatus != null && !newStatus.isActive()) {
//            participantEvents.forEach(pe -> pe.setStatus(CommonStatus.INACTIVE));
//            rewardEvents.forEach(r -> r.setStatus(CommonStatus.INACTIVE));
//            goldenHours.forEach(gh -> gh.setStatus(CommonStatus.INACTIVE));
//        }

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

        // Ensure ID is initialized before persisting
        initializeOrUpdateId();

        this.validateState();
    }

    @Override
    public void doPreUpdate() {
        super.doPreUpdate();
        this.validateState();
    }

    /**
     * Validate location state
     *
     * @throws IllegalStateException if validation fails
     */
    public void validateState() {
        if (region == null) {
            throw new IllegalStateException("Region must be specified");
        }

        if (maxSpin < 0) {
            throw new IllegalStateException("Maximum spins must be non-negative");
        }

        if (todaySpin < 0) {
            throw new IllegalStateException("Today Spin must be non-negative");
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
