package vn.com.fecredit.app.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.fecredit.app.entity.base.SerializableKey;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EventLocationKey implements SerializableKey {

    private static final long serialVersionUID = 1L;

    // Add Column annotations to explicitly name the database columns
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    @Column(name = "region_id", nullable = false)
    private Long regionId;

    /**
     * Create a new EventLocationKey with the specified event and region IDs.
     * 
     * @param eventId the ID of the event
     * @param regionId the ID of the region
     * @return a new EventLocationKey instance
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventLocationKey that = (EventLocationKey) o;
        return Objects.equals(eventId, that.eventId) && Objects.equals(regionId, that.regionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, regionId);
    }

    @Override
    public String toString() {
        return "EventLocationKey{" +
               "eventId=" + eventId +
               ", regionId=" + regionId +
               '}';
    }
}
