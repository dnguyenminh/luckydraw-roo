package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.base.StatusAware;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Province entity representing administrative divisions within regions.
 * Provinces belong to regions and contain participants, forming part of the
 * geographical hierarchy in the system.
 */
@Entity
@Table(
    name = "provinces",
    indexes = {
        @Index(name = "idx_province_code", columnList = "code", unique = true),
        @Index(name = "idx_province_region", columnList = "region_id"),
        @Index(name = "idx_province_status", columnList = "status")
    }
)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"region", "participants"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Province extends AbstractStatusAwareEntity {

    // private static final long serialVersionUID = 1L;

    /**
     * Name of the province
     */
    @NotBlank(message = "Province name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Unique code identifier for this province
     */
    @NotBlank(message = "Province code is required")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    @EqualsAndHashCode.Include
    private String code;

    /**
     * Extended description of the province
     */
    @Column(name = "description")
    private String description;

    /**
     * Region to which this province belongs
     */
    @NotNull(message = "Region is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    /**
     * Participants registered in this province
     */
    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
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
     * Set status with proper cascading to related entities
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
            
            // Check if this is the last active province in the region
            if (region != null && region.getProvinces() != null) {
                boolean anyOtherActiveProvinces = region.getProvinces().stream()
                    .filter(p -> !p.equals(this)) // Exclude this province 
                    .anyMatch(p -> p.getStatus() != null && p.getStatus().isActive());
                
                if (!anyOtherActiveProvinces && region.getStatus() != null && region.getStatus().isActive()) {
                    // Deactivate region if no other active provinces
                    region.setStatus(CommonStatus.INACTIVE);
                }
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
        if (region == null) {
            throw new IllegalStateException("Region must be specified");
        }

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
     * Creates a new Province with the specified name, code and region.
     * The province will be set to ACTIVE status by default.
     *
     * @param name   the name of the province
     * @param code   the unique code for the province
     * @param region the region to which this province belongs
     */
    public Province(String name, String code, Region region) {
        this.name = name;
        this.code = code;
        this.region = region;
        this.setStatus(CommonStatus.ACTIVE);
    }

    /**
     * Validates the current state of the province.
     * Checks for required fields and throws exceptions if validation fails.
     *
     * @return this province entity after validation
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public AbstractStatusAwareEntity validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Province name cannot be empty");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Province code cannot be empty");
        }

        if (region == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }

        // Normalize code to uppercase
        if (code != null) {
            code = code.toUpperCase();
        }

        return this;
    }

    public void deactivate() {
        super.deactivate();
        if (region != null && region.getStatus() == CommonStatus.INACTIVE) {
            throw new IllegalStateException("Cannot activate province with inactive region");
        }
    }
}
