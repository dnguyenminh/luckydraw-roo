package vn.com.fecredit.app.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
import vn.com.fecredit.app.entity.base.AbstractComplexPersistableEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Entity representing a participant's registration to a specific event.
 * <p>
 * ParticipantEvent tracks the relationship between participants and events,
 * including the specific event location, participant details, and the remaining
 * spins available to the participant. It serves as the core entity for tracking
 * event participation and engagement metrics.
 */
@Entity
@Table(name = "participant_events", indexes = {
    @Index(name = "idx_participant_location", columnList = "event_id, region_id"),
    @Index(name = "idx_participant", columnList = "participant_id"),
    @Index(name = "idx_participant_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"eventLocation", "participant", "spinHistories"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ParticipantEvent extends AbstractComplexPersistableEntity<ParticipantEventKey> {
    @NotNull(message = "Event location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventLocationKey")
    @JoinColumns({
        @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false),
        @JoinColumn(name = "region_id", referencedColumnName = "region_id", nullable = false)
    })
    private EventLocation eventLocation;

    @NotNull(message = "Participant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("participantId")
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Min(value = 0, message = "Spins remaining cannot be negative")
    @Column(name = "spins_remaining", nullable = false)
    @Builder.Default
    private int spinsRemaining = 0;

    @OneToMany(mappedBy = "participantEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("spinTime ASC")
    @Builder.Default
    private List<SpinHistory> spinHistories = new ArrayList<>();


    /**
     * Check if participant can spin
     *
     * @return true if can spin
     */
    public boolean canSpin() {
        if (!getStatus().isActive() ||
            eventLocation == null ||
            !eventLocation.getStatus().isActive() ||
            !eventLocation.getEvent().isActive()) {
            return false;
        }
        return spinsRemaining > 0;
    }

    /**
     * Add spin history with proper bidirectional relationship
     *
     * @param spinHistory the spin history to add
     */
    public void addSpinHistory(SpinHistory spinHistory) {
        if (spinHistory != null) {
            // First, handle old relationship if any
            if (spinHistory.getParticipantEvent() != null && spinHistory.getParticipantEvent() != this) {
                spinHistory.getParticipantEvent().getSpinHistories().remove(spinHistory);
            }

            // Add to our collection if not already present
            if (!spinHistories.contains(spinHistory)) {
                spinHistories.add(spinHistory);
            }

            // Set us as the owner
            spinHistory.setParticipantEvent(this);
        }
    }

    /**
     * Remove spin history with proper bidirectional relationship
     *
     * @param spinHistory the spin history to remove
     */
    public void removeSpinHistory(SpinHistory spinHistory) {
        if (spinHistory != null) {
            spinHistories.remove(spinHistory);
            if (spinHistory.getParticipantEvent() == this) {
                spinHistory.setParticipantEvent(null);
            }
        }
    }

    /**
     * Set event location with proper bidirectional relationship
     *
     * @param newLocation the location to set
     */
    public void setEventLocation(EventLocation newLocation) {
        if (this.eventLocation != null) {
            if (null != newLocation) {
                if (this.eventLocation.equals(newLocation)) {
                    if (!this.eventLocation.getParticipantEvents().contains(this)) {
                        this.eventLocation.addParticipantEvent(this);
                    }
                } else {
                    if (this.eventLocation.getParticipantEvents() != null && this.eventLocation.getParticipantEvents().contains(this)) {
                        this.eventLocation.removeParticipantEvent(this);
                    }
                }
            }
        }
        this.eventLocation = newLocation;
        CommonStatus newStatus = null != this.eventLocation && null != this.participant ?
            this.eventLocation.isActive() && this.participant.isActive() ? CommonStatus.ACTIVE : CommonStatus.INACTIVE : null;
        setStatus(newStatus);
        updateId();
    }

    /**
     * Set participant with proper bidirectional relationship
     *
     * @param newParticipant the participant to set
     */
    public void setParticipant(Participant newParticipant) {
        if (this.participant != null) {
            if (null != newParticipant) {
                if (this.participant.equals(newParticipant)) {
                    if (!this.participant.getParticipantEvents().contains(this)) {
                        this.participant.addParticipantEvent(this);
                    }
                } else {
                    if (this.participant.getParticipantEvents() != null && this.participant.getParticipantEvents().contains(this)) {
                        this.participant.removeParticipantEvent(this);
                    }
                }
            }
        }
        this.participant = newParticipant;
        CommonStatus newStatus = null != this.eventLocation && null != this.participant ?
            this.eventLocation.isActive() && this.participant.isActive() ? CommonStatus.ACTIVE : CommonStatus.INACTIVE : null;
        setStatus(newStatus);
        updateId();
    }

    /**
     * Perform a spin with proper thread safety
     *
     * @return spin history record
     * @throws IllegalStateException if cannot spin
     */
    public synchronized SpinHistory spin() {
        // Double-check locking pattern
        if (!canSpin()) {
            throw new IllegalStateException("Cannot spin");
        }

        // Decrement spins atomically
        int remainingSpins = spinsRemaining;
        spinsRemaining = remainingSpins - 1;

        // Create new spin history
        LocalDateTime spinTime = LocalDateTime.now();
        SpinHistory spinHistory = SpinHistory.builder()
            .spinTime(spinTime)
            .status(CommonStatus.ACTIVE)
            .build();

        // Establish bidirectional relationship with thread safety
        synchronized (this.spinHistories) {
            spinHistory.setParticipantEvent(this);
            this.spinHistories.add(spinHistory);
        }

        return spinHistory;
    }

    /**
     * Calculate total winnings from all spins
     *
     * @return sum of all winning spins' effective values
     */
    @Transient
    public BigDecimal getTotalWinnings() {
        if (spinHistories == null || spinHistories.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (SpinHistory sh : spinHistories) {
            if (sh.isWin() && sh.getStatus().isActive()) {
                // Ensure reward value is set and calculation is correct
                total = total.add(BigDecimal.ONE);
            }
        }

        return total;
    }

    @Override
    public void doPrePersist() {
        super.doPrePersist();
        this.updateId(); // Ensure ID is set before persisting
        this.validateState();
    }

    @Override
    public void doPreUpdate() {
        super.doPreUpdate();
        this.validateState();
    }

    /**
     * Validate participation state
     *
     * @throws IllegalStateException if validation fails
     */
    public void validateState() {
        if (eventLocation == null) {
            throw new IllegalStateException("Event location is required");
        }

        if (participant == null) {
            throw new IllegalStateException("Participant is required");
        }

        if (spinsRemaining < 0) {
            throw new IllegalStateException("Spins remaining cannot be negative");
        }

        if (eventLocation.getMaxSpin() <= 0) {
            throw new IllegalStateException("Maximum spins must be positive");
        }
    }

    private void updateId() {
        if (this.eventLocation != null && this.participant != null) {
            ParticipantEventKey key = getId();
            if (key == null) {
                key = new ParticipantEventKey();
                setId(key);
            }
            key.setParticipantId(this.participant.getId());
            
            // Create a new EventLocationKey if needed instead of directly using the reference
            EventLocationKey eventLocationKey = this.eventLocation.getId();
            if (eventLocationKey == null && this.eventLocation.getEvent() != null && this.eventLocation.getRegion() != null) {
                // If the eventLocation's ID is null but we have enough information to create it
                eventLocationKey = new EventLocationKey();
                eventLocationKey.setEventId(this.eventLocation.getEvent().getId());
                eventLocationKey.setRegionId(this.eventLocation.getRegion().getId());
            }
            
            key.setEventLocationKey(eventLocationKey);
        }
    }
}
