package vn.com.fecredit.app.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.fecredit.app.entity.base.SerializableKey;

import java.util.Objects;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RewardEventKey implements SerializableKey {
    private static final long serialVersionUID = 1L;

    private EventLocationKey eventLocationKey;
    private Long rewardId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewardEventKey that = (RewardEventKey) o;
        return Objects.equals(eventLocationKey, that.eventLocationKey) &&
            Objects.equals(rewardId, that.rewardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventLocationKey, rewardId);
    }

}
