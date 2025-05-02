package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.base.StatusAware;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Entity representing a province or administrative division within regions.
 * <p>
 * Provinces are geographical subdivisions that can belong to multiple regions and 
 * help organize participants and track regional participation statistics.
 * </p>
 * <p>
 * The province hierarchy allows for detailed geographical analysis of participation
 * rates and helps target marketing efforts more effectively.
 * </p>
 */
@Entity
@Table(name = "provinces", indexes = {
        @Index(name = "idx_province_code", columnList = "code", unique = true),
        @Index(name = "idx_province_status", columnList = "status")
})
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = { "regions", "participants" })
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Province extends AbstractSimplePersistableEntity<Long> {

    /**
     * Human-readable name of the province
     * Used for display purposes in UI and reports
     */
    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Unique code identifier for the province
     * Used for programmatic identification and reporting
     */
    @NotBlank(message = "Code is required")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    @EqualsAndHashCode.Include
    private String code;

    /**
     * Optional detailed description of the province
     * Provides additional information about the geographical area
     */
    @Column(name = "description")
    private String description;

    /**
     * The regions this province belongs to
     * Represents the parent geographical entities containing this province
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "region_province",
        joinColumns = @JoinColumn(name = "province_id"),
        inverseJoinColumns = @JoinColumn(name = "region_id")
    )
    @Builder.Default
    private Set<Region> regions = new HashSet<>();

    /**
     * Collection of participants residing in this province
     * All users who have registered with this province as their location
     */
    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Participant> participants = new HashSet<>();

    /**
     * Add a participant to this province
     *
     * @param participant the participant to add
     */
    public void addParticipant(Participant participant) {
        if (participant != null) {
            participants.add(participant);
            participant.setProvince(this);
        }
    }

    /**
     * Remove a participant from this province
     *
     * @param participant the participant to remove
     */
    public void removeParticipant(Participant participant) {
        if (participant != null) {
            participants.remove(participant);
            participant.setProvince(null);
        }
    }

    /**
     * Add a region to this province with bidirectional relationship
     *
     * @param region the region to add
     */
    public void addRegion(Region region) {
        if (region != null) {
            regions.add(region);
            if (!region.getProvinces().contains(this)) {
                region.getProvinces().add(this);
            }
        }
    }

    /**
     * Remove a region from this province
     *
     * @param region the region to remove
     */
    public void removeRegion(Region region) {
        if (region != null && regions.remove(region)) {
            if (region.getProvinces().contains(this)) {
                region.getProvinces().remove(this);
            }
        }
    }

    /**
     * Set status with proper cascading to related entities
     * 
     * @param status the new status
     * @return this province for method chaining
     */
    @Override
    public StatusAware setStatus(CommonStatus status) {
        // Store old status for comparison
        CommonStatus oldStatus = getStatus();

        super.setStatus(status);

        // If province is deactivated and was previously active, cascade to participants
        if (status != null && !status.isActive() && (oldStatus == null || oldStatus.isActive())) {
            // Cascade to participants
            if (participants != null) {
                participants.forEach(participant -> participant.setStatus(CommonStatus.INACTIVE));
            }
        }

        return this;
    }

    /**
     * Validate province state
     *
     * @throws IllegalStateException if validation fails
     */
    public void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }
    }

    @Override
    public void doPrePersist() {
        validateState();
    }

    @Override
    public void doPreUpdate() {
        validateState();
    }

    /**
     * Creates a new Province with the specified name and code.
     * The province will be set to ACTIVE status by default.
     *
     * @param name the name of the province
     * @param code the unique code for the province
     */
    public Province(String name, String code) {
        this.name = name;
        this.code = code;
        this.setStatus(CommonStatus.ACTIVE);
    }

    /**
     * Validates the current state of the province.
     * Checks for required fields and throws exceptions if validation fails.
     *
     * @return this province entity after validation
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public AbstractStatusAwareEntity<Long> validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Province name cannot be empty");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Province code cannot be empty");
        }

        // Normalize code to uppercase
        if (code != null) {
            code = code.toUpperCase();
        }

        return this;
    }

    public void deactivate() {
        super.deactivate();
        // Check if all regions are inactive
        boolean allRegionsInactive = true;
        if (regions != null && !regions.isEmpty()) {
            allRegionsInactive = regions.stream()
                .allMatch(region -> region.getStatus() == CommonStatus.INACTIVE);
        }
        
        if (!allRegionsInactive) {
            throw new IllegalStateException("Cannot deactivate province when it belongs to active regions");
        }
    }
}
