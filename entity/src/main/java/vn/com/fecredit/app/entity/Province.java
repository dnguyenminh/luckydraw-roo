package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.HashSet;
import java.util.Set;

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

    @NotBlank(message = "Province name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Province code is required")
    @Column(name = "code", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String code;

    @Column(name = "description")
    private String description;

    @NotNull(message = "Region is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Participant> participants = new HashSet<>();

    /**
     * Add a participant to this province
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
     * @param participant the participant to remove
     */
    public void removeParticipant(Participant participant) {
        if (participant != null) {
            participants.remove(participant);
            participant.setProvince(null);
        }
    }

    /**
     * Set province status and notify region
     */
    @Override
    public void setStatus(CommonStatus newStatus) {
        super.setStatus(newStatus);
        
        // Always notify region of status changes
        if (region != null) {
            region.updateStatusBasedOnProvinces();
        }
        
        validateState();
    }

    /**
     * Validate province state
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validateState() {
        if (region == null) {
            throw new IllegalStateException("Region must be specified");
        }

        if (code != null) {
            code = code.toUpperCase();
        }
    }
}
