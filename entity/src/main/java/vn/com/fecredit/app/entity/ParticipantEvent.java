package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "participant_events",
    indexes = {
        @Index(name = "idx_participant_event", columnList = "event_id"),
        @Index(name = "idx_participant_location", columnList = "event_location_id"),
        @Index(name = "idx_participant", columnList = "participant_id"),
        @Index(name = "idx_participant_status", columnList = "status")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"event", "eventLocation", "participant", "spinHistories"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ParticipantEvent extends AbstractStatusAwareEntity {

    @NotNull(message = "Event is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull(message = "Event location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_location_id", nullable = false)
    private EventLocation eventLocation;

    @NotNull(message = "Participant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Min(value = 0, message = "Spins remaining cannot be negative")
    @Column(name = "spins_remaining", nullable = false)
    private int spinsRemaining;

    @OneToMany(mappedBy = "participantEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("spinTime ASC")
    @Builder.Default
    private List<SpinHistory> spinHistories = new ArrayList<>();

    /**
     * Get count of spins used today
     * @return number of spins used today
     */
    public long getTodaySpinCount() {
        LocalDateTime currentTime = event != null ? event.getCurrentServerTime() : LocalDateTime.now();
        LocalDate today = currentTime.toLocalDate();

        synchronized (spinHistories) {
            return spinHistories.stream()
                .filter(spin -> spin != null &&
                              spin.getSpinTime() != null &&
                              spin.getSpinTime().toLocalDate().equals(today))
                .count();
        }
    }

    /**
     * Check if participant can spin
     * @return true if can spin
     */
    public boolean canSpin() {
        if (!getStatus().isActive() ||
            event == null || !event.getStatus().isActive() ||
            eventLocation == null || !eventLocation.getStatus().isActive() ||
            spinsRemaining <= 0) {
            return false;
        }

        long todayCount = getTodaySpinCount();
        return todayCount < eventLocation.getMaxSpin();
    }

    /**
     * Add spin history with proper bidirectional relationship
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
     * Set event with proper bidirectional relationship
     * @param newEvent the event to set
     */
    public void setEvent(Event newEvent) {
        Event oldEvent = this.event;
        if (oldEvent != null && oldEvent.getParticipantEvents() != null) {
            oldEvent.getParticipantEvents().remove(this);
        }
        this.event = newEvent;
        if (newEvent != null && newEvent.getParticipantEvents() != null) {
            newEvent.getParticipantEvents().add(this);
        }
    }

    /**
     * Set event location with proper bidirectional relationship
     * @param newLocation the location to set
     */
    public void setEventLocation(EventLocation newLocation) {
        EventLocation oldLocation = this.eventLocation;
        if (oldLocation != null && oldLocation.getParticipantEvents() != null) {
            oldLocation.getParticipantEvents().remove(this);
        }
        this.eventLocation = newLocation;
        if (newLocation != null && newLocation.getParticipantEvents() != null) {
            newLocation.getParticipantEvents().add(this);
        }
    }

    /**
     * Perform a spin
     * @return spin history record
     * @throws IllegalStateException if cannot spin
     */
    public synchronized SpinHistory spin() {
        if (!canSpin()) {
            if (getTodaySpinCount() >= eventLocation.getMaxSpin()) {
                throw new IllegalStateException("Maximum spins reached");
            }
            throw new IllegalStateException("Cannot spin");
        }

        spinsRemaining--;

        // Create new spin history
        SpinHistory spinHistory = SpinHistory.builder()
            .spinTime(event.getCurrentServerTime())
            .status(CommonStatus.ACTIVE)
            .build();

        // Establish bidirectional relationship
        spinHistory.setParticipantEvent(this);
        spinHistories.add(spinHistory);

        return spinHistory;
    }

    /**
     * Calculate total winnings from all spins
     * @return total winnings
     */
    public BigDecimal getTotalWinnings() {
        return spinHistories.stream()
            .map(SpinHistory::calculateEffectiveValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validate participation state
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validateState() {
        if (event == null) {
            throw new IllegalStateException("Event is required");
        }

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
}
