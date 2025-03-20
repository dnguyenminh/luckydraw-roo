package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "rewards",
    indexes = {
        @Index(name = "idx_reward_code", columnList = "code", unique = true),
        @Index(name = "idx_reward_location", columnList = "event_location_id"),
        @Index(name = "idx_reward_status", columnList = "status")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"spinHistories", "eventLocation"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Reward extends AbstractStatusAwareEntity {

    @NotBlank(message = "Reward name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Reward code is required")
    @Column(name = "code", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "description")
    private String description;

    @DecimalMin(value = "0", message = "Value must be positive")
    @Column(name = "value", nullable = false)
    @Builder.Default
    private BigDecimal value = BigDecimal.ONE;

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
    @JoinColumn(name = "event_location_id", nullable = false)
    @NotNull(message = "Event location is required")
    private EventLocation eventLocation;

    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<SpinHistory> spinHistories = new HashSet<>();

    /**
     * Get remaining available quantity of this reward
     * @return remaining quantity
     */
    @Transient
    public int getRemainingQuantity() {
        if (quantity == null) return 0;
        
        long usedQuantity = spinHistories.stream()
            .filter(sh -> sh.getStatus().isActive() && sh.isWin())
            .count();
            
        return (int) Math.max(0, quantity - usedQuantity);
    }

    /**
     * Check if reward is available to be won
     * @return true if available
     */
    @Transient
    public boolean isAvailable() {
        return getStatus().isActive() &&
               eventLocation != null &&
               eventLocation.getStatus().isActive() &&
               getRemainingQuantity() > 0;
    }

    /**
     * Set event location with proper bidirectional relationship
     * @param newLocation the location to set
     */
    public void setEventLocation(EventLocation newLocation) {
        EventLocation oldLocation = this.eventLocation;
        
        if (oldLocation != null && oldLocation.getRewards() != null) {
            oldLocation.getRewards().remove(this);
        }
        
        this.eventLocation = newLocation;
        
        if (newLocation != null && newLocation.getRewards() != null) {
            newLocation.getRewards().add(this);
        }
    }

    @Override
    public void setStatus(CommonStatus newStatus) {
        // Call validation logic before setting the status
        validateStatusChange(newStatus);
        super.setStatus(newStatus);
    }

    @Override
    public void markAsActive() {
        if (eventLocation == null || !eventLocation.getStatus().isActive()) {
            throw new IllegalStateException("Cannot activate reward when event location is inactive");
        }
        super.markAsActive();
    }

    // Move lifecycle validation to a no-arg method
    @PrePersist
    @PreUpdate
    public void validateBeforeSave() {
        validateState(); // Reuse the existing validation method
    }

    public void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Reward name is required");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Reward code is required");
        }

        if (eventLocation == null) {
            throw new IllegalStateException("Event location is required");
        }

        if (quantity == null || quantity < 0) {
            throw new IllegalStateException("Quantity must be non-negative");
        }

        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Value must be positive");
        }

        if (winProbability == null || winProbability < 0.0 || winProbability > 1.0) {
            throw new IllegalStateException("Win probability must be between 0 and 1");
        }

        if (getStatus() != null && getStatus().isActive() && !eventLocation.getStatus().isActive()) {
            throw new IllegalStateException("Cannot be active when event location is inactive");
        }
    }

    // Separate method for status change validation
    private void validateStatusChange(CommonStatus newStatus) {
        if (newStatus != null && newStatus.isActive() &&
            (eventLocation == null || !eventLocation.getStatus().isActive())) {
            throw new IllegalStateException("Cannot activate reward when event location is inactive");
        }
    }
}