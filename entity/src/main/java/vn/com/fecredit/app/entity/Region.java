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
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.base.StatusAware;

/**
 * Entity representing a geographical region in the country.
 * <p>
 * Regions are the highest level of geographical organization in the system,
 * containing multiple provinces. They are used for organizing event locations
 * and analyzing participation data at a regional level.
 * </p>
 * <p>
 * Regions allow for administrative organization of the country into larger
 * areas that can be targeted for specific marketing campaigns or events.
 * </p>
 */
@Entity
@Table(name = "regions", indexes = {
        @Index(name = "idx_region_code", columnList = "code", unique = true),
        @Index(name = "idx_region_status", columnList = "status")
})
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = { "provinces", "eventLocations" })
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Region extends AbstractSimplePersistableEntity<Long> {

    /**
     * Human-readable name of the region
     * Used for display purposes in UI and reports
     */
    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Unique code identifier for the region
     * Used for programmatic identification and reporting
     */
    @NotBlank(message = "Code is required")
    @Column(name = "code", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String code;

    /**
     * Optional detailed description of the region
     * Provides additional information about the geographical area
     */
    @Column(name = "description")
    private String description;

    /**
     * Collection of provinces within this region
     * Administrative subdivisions of the region
     */
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Province> provinces = new HashSet<>();

    /**
     * Collection of event locations in this region
     * Places where events are held within this geographical area
     */
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventLocation> eventLocations = new HashSet<>();

    /**
     * Add a province to this region with bidirectional relationship
     * 
     * @param province the province to add
     */
    public void addProvince(Province province) {
        provinces.add(province);
        province.setRegion(this);
    }

    /**
     * Remove a province from this region
     * 
     * @param province the province to remove
     */
    public void removeProvince(Province province) {
        provinces.remove(province);
        province.setRegion(null);
    }

    /**
     * Add an event location to this region with bidirectional relationship
     * 
     * @param location the location to add
     */
    public void addEventLocation(EventLocation location) {
        eventLocations.add(location);
        location.setRegion(this);
    }

    /**
     * Remove an event location from this region
     * 
     * @param location the location to remove
     */
    public void removeEventLocation(EventLocation location) {
        eventLocations.remove(location);
        location.setRegion(null);
    }

    /**
     * Get total active provinces in this region
     * 
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
     * 
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
     * 
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
     * 
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
     * 
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
     * 
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
