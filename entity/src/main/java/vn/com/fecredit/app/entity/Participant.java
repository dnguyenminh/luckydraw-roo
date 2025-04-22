package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an individual who participates in events.
 * <p>
 * Participants are the users who engage with the lucky draw events. They register
 * with their personal information and province, then participate in events at specific
 * locations. Each participant can join multiple events and has a tracking record of
 * their participation and spin history.
 * </p>
 * <p>
 * The participant's province is important for regional analysis and targeting specific
 * demographic groups with tailored events and rewards.
 * </p>
 */
@Entity
@Table(name = "participants", indexes = {
        @Index(name = "idx_participant_code", columnList = "code", unique = true),
        @Index(name = "idx_participants_status", columnList = "status"),
        @Index(name = "idx_participant_province", columnList = "province_id")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor // Creates a default no-args constructor required by JPA
@AllArgsConstructor
@ToString(callSuper = true, exclude = { "province", "participantEvents" })
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Participant extends AbstractSimplePersistableEntity<Long> {

    /**
     * Full name of the participant
     */
    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Unique code identifier for the participant
     * Often used for quick lookups and QR code generation
     */
    @NotBlank(message = "Code is required")
    @Column(name = "code", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String code;

    /**
     * Contact phone number of the participant
     */
    @Column(name = "phone")
    private String phone;

    /**
     * Physical address of the participant
     */
    @Column(name = "address")
    private String address;

    /**
     * Tracks the last number of spins added to this participant
     * Used for auditing and preventing abuse
     */
    @Column(name = "lastAddingSpin")
    private int lastAddingSpin;

    /**
     * Province where the participant is located
     * Important for demographic analysis and regional targeting
     */
    @NotNull(message = "Province is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    /**
     * Collection of event participation records for this participant
     * Tracks all events the participant has joined
     */
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ParticipantEvent> participantEvents = new HashSet<>();

    /**
     * Set the participant's province with proper bidirectional relationship
     *
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
     * Get participation record for an event
     *
     * @param event the event to check
     * @return participation record or null
     */
    public ParticipantEvent getEventParticipation(Event event) {
        return participantEvents.stream()
                .filter(pe -> pe.getEventLocation().getEvent().equals(event))
                .findFirst()
                .orElse(null);
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
     * Validate participant state
     *
     * @throws IllegalStateException if validation fails
     */
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

}
