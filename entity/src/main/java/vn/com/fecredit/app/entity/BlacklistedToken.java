package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "blacklisted_tokens",
    indexes = {
        @Index(name = "idx_blacklisted_token_user", columnList = "user_id"),
        @Index(name = "idx_blacklisted_token_status", columnList = "status"),
        @Index(name = "idx_blacklisted_token_type", columnList = "token_type")
    }
)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "user")
@EqualsAndHashCode(callSuper = true)
public class BlacklistedToken extends AbstractStatusAwareEntity {

    @NotBlank(message = "Token is required")
    @Column(name = "token", nullable = false)
    private String token;

    @NotBlank(message = "Token type is required")
    @Column(name = "token_type", nullable = false)
    private String tokenType;

    @NotNull(message = "Expiration time is required")
    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Check if token is expired
     * @param checkTime time to check against
     * @return true if expired
     */
    public boolean isExpired(LocalDateTime checkTime) {
        return expirationTime != null && checkTime.isAfter(expirationTime);
    }

    /**
     * Check if token is currently expired
     * @return true if expired
     */
    @Transient
    public boolean isExpired() {
        return isExpired(LocalDateTime.now());
    }

    /**
     * Validate token state
     * @throws IllegalStateException if validation fails
     */
    @PrePersist
    @PreUpdate
    public void validateState() {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Token must not be empty");
        }
        
        if (tokenType == null || tokenType.isBlank()) {
            throw new IllegalStateException("Token type must not be empty");
        }

        if (expirationTime == null) {
            throw new IllegalStateException("Expiration time must be specified");
        }

        if (expirationTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Expiration time must be in the future");
        }
    }
}
