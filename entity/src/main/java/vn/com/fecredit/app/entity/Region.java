package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "regions",
    indexes = {
        @Index(name = "idx_region_code", columnList = "code", unique = true),
        @Index(name = "idx_region_status", columnList = "status")
    }
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"provinces", "eventLocations"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Region extends AbstractStatusAwareEntity {

    @NotBlank(message = "Region name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotBlank(message = "Region code is required")
    @Column(name = "code", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String code;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Province> provinces = new HashSet<>();

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<EventLocation> eventLocations = new HashSet<>();

    /**
     * Add a province to this region
     * @param province the province to add
     */
    public void addProvince(Province province) {
        if (province != null) {
            provinces.add(province);
            if (province.getRegion() != this) {
                province.setRegion(this);
            }
        }
    }

    /**
     * Remove a province from this region
     * @param province the province to remove
     */
    public void removeProvince(Province province) {
        if (province != null && provinces.remove(province)) {
            if (province.getRegion() == this) {
                province.setRegion(null);
            }
        }
    }

    /**
     * Add an event location to this region
     * @param location the location to add
     */
    public void addEventLocation(EventLocation location) {
        if (location != null) {
            eventLocations.add(location);
            if (location.getRegion() != this) {
                location.setRegion(this);
            }
        }
    }

    /**
     * Remove an event location from this region
     * @param location the location to remove
     */
    public void removeEventLocation(EventLocation location) {
        if (location != null && eventLocations.remove(location)) {
            if (location.getRegion() == this) {
                location.setRegion(null);
            }
        }
    }

    /**
     * Count active provinces in this region
     * @return count of active provinces
     */
    @Transient
    public long getActiveProvinceCount() {
        return provinces.stream()
            .filter(p -> p.getStatus().isActive())
            .count();
    }

    /**
     * Count active event locations in this region
     * @return count of active locations
     */
    @Transient
    public long getActiveEventLocationCount() {
        return eventLocations.stream()
            .filter(l -> l.getStatus().isActive())
            .count();
    }

    /**
     * Check if this region has any provinces that overlap with another region
     * @param other the other region to check
     * @return true if any provinces overlap
     */
    public boolean hasOverlappingProvinces(Region other) {
        if (other == null || this.equals(other)) {
            return false;
        }

        return provinces.stream()
            .anyMatch(p1 -> other.getProvinces().stream()
                .anyMatch(p2 -> p1.equals(p2)));
    }

    /**
     * Validate region state
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validateState() {
        if (code != null) {
            code = code.toUpperCase();
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Region name must be specified");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Region code must be specified");
        }

       
    }

    /**
     * Set region status and cascade to event locations
     */
    @Override
    public void setStatus(CommonStatus newStatus) {
        boolean wasActive = getStatus().isActive();
        super.setStatus(newStatus);
        
        // Cascade deactivation to event locations
        if (wasActive && newStatus != null && !newStatus.isActive()) {
            eventLocations.forEach(el -> el.setStatus(CommonStatus.INACTIVE));
        }
    }

    /**
     * Update region status based on provinces status
     * Called when province status changes
     */
    public void updateStatusBasedOnProvinces() {
        // Only check for deactivation if region is currently active
        if (getStatus().isActive()) {
            boolean allProvincesInactive = provinces.stream()
                .allMatch(p -> !p.getStatus().isActive());
                
            if (allProvincesInactive) {
                setStatus(CommonStatus.INACTIVE);
            }
        }
    }
}
