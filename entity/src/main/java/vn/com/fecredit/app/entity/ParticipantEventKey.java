package vn.com.fecredit.app.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.fecredit.app.entity.base.SerializableKey;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Embeddable
public class ParticipantEventKey implements SerializableKey {
    private static final long serialVersionUID = 1L;

    private EventLocationKey eventLocationKey;
    private Long participantId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantEventKey that = (ParticipantEventKey) o;
        return Objects.equals(eventLocationKey, that.eventLocationKey) &&
            Objects.equals(participantId, that.participantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventLocationKey, participantId);
    }

}
