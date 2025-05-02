package vn.com.fecredit.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.fecredit.app.entity.base.SerializableKey;

/**
 * Composite primary key for the EventLocation entity.
 * <p>
 * This embeddable class represents the composite key consisting of
 * event ID and region ID, which together uniquely identify a specific
 * event location. The class implements SerializableKey to support
 * serialization and comparison operations.
 * </p>
 * <p>
 * EventLocationKey is used in relationships with other entities like
 * ParticipantEvent and RewardEvent that need to reference a specific
 * event location through its composite key.
 * </p>
 * <p>
 * The default no-argument constructor is required by JPA for embeddable classes
 * and creates an empty key that must have its fields populated before use.
 * </p>
 */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode // Added Lombok annotation for equals and hashCode
public class EventLocationKey implements SerializableKey {

    private static final long serialVersionUID = 1L;

    /**
     * The ID of the associated event
     * Forms the first part of the composite key
     */
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    /**
     * The ID of the associated region
     * Forms the second part of the composite key
     */
    @Column(name = "region_id", nullable = false)
    private Long regionId;

    /**
     * Create a new EventLocationKey with the specified event and region IDs.
     * Validates that both IDs are non-null before creating the key.
     * 
     * @param eventId the ID of the event
     * @param regionId the ID of the region
     * @return a new EventLocationKey instance
     * @throws IllegalArgumentException if either ID is null
     */
    public static EventLocationKey of(Long eventId, Long regionId) {
        if (eventId == null || regionId == null) {
            throw new IllegalArgumentException("EventLocationKey requires non-null eventId and regionId");
        }
        return EventLocationKey.builder()
            .eventId(eventId)
            .regionId(regionId)
            .build();
    }

    /**
     * Returns a string representation of this key.
     * Includes both the eventId and regionId values for debugging purposes.
     * 
     * @return string representation of the key
     */
    @Override
    public String toString() {
        return "EventLocationKey{" +
               "eventId=" + eventId +
               ", regionId=" + regionId +
               '}';
    }
}
