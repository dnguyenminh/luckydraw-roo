package vn.com.fecredit.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.base.StatusAware;

/**
 * Region entity representing geographical regions in the system.
 * Regions contain provinces and event locations, forming a geographical hierarchy.
 * This entity supports regional organization of events and participants.
 */
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

    /**
     * Name of the geographical region
     */
    @NotBlank(message = "Region name is required")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Unique code identifier for this region
     */
    @NotBlank(message = "Region code is required")
    @Column(name = "code", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String code;

    /**
     * Description of the geographical region
     */
    @Column(name = "description")
    private String description;

    /**
     * Provinces contained within this region
     */
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Province> provinces = new HashSet<>();

    /**
     * Event locations situated in this region
     */
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EventLocation> eventLocations = new HashSet<>();

    /**
     * Add a province to this region with bidirectional relationship
     * @param province the province to add
     */
    public void addProvince(Province province) {
        provinces.add(province);
        province.setRegion(this);
    }

    /**
     * Remove a province from this region
     * @param province the province to remove
     */
    public void removeProvince(Province province) {
        provinces.remove(province);
        province.setRegion(null);
    }

    /**
     * Add an event location to this region with bidirectional relationship
     * @param location the location to add
     */
    public void addEventLocation(EventLocation location) {
        eventLocations.add(location);
        location.setRegion(this);
    }

    /**
     * Remove an event location from this region
     * @param location the location to remove
     */
    public void removeEventLocation(EventLocation location) {
        eventLocations.remove(location);
        location.setRegion(null);
    }

    /**
     * Get total active provinces in this region
     * @return count of active provinces
     */
    @Transient
    public long getActiveProvinceCount() {
        return provinces.stream()
            .filter(p -> p.getStatus().isActive())
            .count();
    }

    /**
     * Get total active event locations in this region
     * @return count of active locations
     */
    @Transient
    public long getActiveEventLocationCount() {
        return eventLocations.stream()
            .filter(e -> e.getStatus().isActive())
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
     * Validate region state
     * @throws IllegalStateException if validation fails
     */
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
     * Set status with proper cascading to related entities
     * @param status the new status
     * @return this region for method chaining
     */
    @Override
    public StatusAware setStatus(CommonStatus status) {
        super.setStatus(status);
        
        // If region is inactive, cascade to provinces and event locations
        if (status != null && !status.isActive()) {
            if (provinces != null) {
                provinces.forEach(province -> province.setStatus(CommonStatus.INACTIVE));
            }
            
            if (eventLocations != null) {
                eventLocations.forEach(location -> location.setStatus(CommonStatus.INACTIVE));
            }
        }
        
        return this;
    }

    /**
     * Update region status based on provinces status
     * Called when province status changes
     */
    public void updateStatusBasedOnProvinces() {
        // Only check for deactivation if region is currently active
        if (getStatus() != null && getStatus().isActive()) {
            boolean allProvincesInactive = !provinces.isEmpty() && provinces.stream()
                .allMatch(p -> !p.getStatus().isActive());

            if (allProvincesInactive) {
                setStatus(CommonStatus.INACTIVE);
            }
        }
    }

    /**
     * Mark the region as active and propagate to related entities if needed
     */
    public void markAsActive() {
        this.setStatus(CommonStatus.ACTIVE);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    /**
     * Check if any provinces in this region are active
     * @return true if at least one province is active
     */
    @Transient
    public boolean hasActiveProvinces() {
        if (provinces == null || provinces.isEmpty()) {
            return false;
        }
        return provinces.stream()
                .anyMatch(p -> p.getStatus() != null && p.getStatus().isActive());
    }
}
