package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "participants",
    indexes = {
        @Index(name = "idx_participant_code", columnList = "code", unique = true),
        @Index(name = "idx_participants_status", columnList = "status"),
        @Index(name = "idx_participant_province", columnList = "province_id")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"participantEvents", "province"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Participant extends AbstractStatusAwareEntity {

    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Code is required")
    @Column(name = "code", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "phone")
    private String phone;

    @NotNull(message = "Province is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ParticipantEvent> participantEvents = new HashSet<>();

    @Builder.Default
    private boolean checkedIn = false;

    /**
     * Set the participant's province with proper bidirectional relationship
     * @param province the province to set
     */
    public void setProvince(Province province) {
        if (this.province != null) {
            this.province.getParticipants().remove(this);
        }
        this.province = province;
        if (province != null) {
            province.getParticipants().add(this);
        }
    }

    /**
     * Join an event
     * @param event the event to join
     * @return the participant event record
     */
    public ParticipantEvent joinEvent(Event event) {
        if (!getStatus().isActive()) {
            throw new IllegalStateException("Cannot join event: participant is not active");
        }

        if (!event.isActive()) {
            throw new IllegalStateException("Cannot join inactive event");
        }

        if (getEventParticipation(event) != null) {
            throw new IllegalStateException("Already participating in this event");
        }

        ParticipantEvent pe = ParticipantEvent.builder()
            .event(event)
            .eventLocation(event.getDefaultLocation())
            .participant(this)
            .spinsRemaining(10) // Default number of spins
            .status(CommonStatus.ACTIVE)
            .build();

        participantEvents.add(pe);
        return pe;
    }

    /**
     * Get total winnings across all events
     * @return total winnings value
     */
    @Transient
    public BigDecimal getTotalWinnings() {
        return participantEvents.stream()
            .map(ParticipantEvent::getTotalWinnings)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get participation record for an event
     * @param event the event to check
     * @return participation record or null
     */
    public ParticipantEvent getEventParticipation(Event event) {
        return participantEvents.stream()
            .filter(pe -> pe.getEvent().equals(event))
            .findFirst()
            .orElse(null);
    }

    /**
     * Check if participant is currently active in any event
     * @return true if active in any event
     */
    @Transient
    public boolean isActiveInAnyEvent() {
        return participantEvents.stream()
            .anyMatch(pe -> pe.getStatus().isActive() && pe.getEvent().isActive());
    }

    /**
     * Validate participant state
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }

        if (province == null) {
            throw new IllegalStateException("Province must be specified");
        }

        if (!province.getStatus().isActive() && getStatus().isActive()) {
            throw new IllegalStateException("Cannot activate participant in inactive province");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Name is required");
        }
        
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Code is required");
        }
        
        if (province == null) {
            throw new IllegalStateException("Province is required");
        }
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}
