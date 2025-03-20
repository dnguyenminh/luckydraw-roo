package vn.com.fecredit.app.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import vn.com.fecredit.app.entity.CommonStatus;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractStatusAwareEntity extends AbstractAuditEntity implements StatusAware {
    
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;

    @Override
    public void setStatus(CommonStatus status) {
        this.status = status;
    }

    @Override
    public CommonStatus getStatus() {
        return status;
    }
}