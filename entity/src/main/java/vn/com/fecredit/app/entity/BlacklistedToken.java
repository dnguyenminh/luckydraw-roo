package vn.com.fecredit.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.com.fecredit.app.entity.base.AbstractSimplePersistableEntity;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.entity.listener.EntityAuditListener;

import java.time.LocalDateTime;

/**
 * BlacklistedToken entity for tracking invalidated authentication tokens.
 * Used to maintain security by explicitly revoking tokens that are no longer
 * valid,
 * such as when a user logs out or when a token is compromised.
 * Part of the security infrastructure to prevent token reuse and session
 * hijacking.
 */
@Entity
@Table(name = "blacklisted_tokens", indexes = {
        @Index(name = "idx_blacklisted_token_user", columnList = "user_id"),
        @Index(name = "idx_blacklisted_token_status", columnList = "status"),
        @Index(name = "idx_blacklisted_token_type", columnList = "token_type")
})
@EntityListeners(EntityAuditListener.class)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "user")
@EqualsAndHashCode(callSuper = true)
public class BlacklistedToken extends AbstractSimplePersistableEntity<Long> {

    /**
     * The actual token string that has been blacklisted
     * Contains the JWT or other token value that should no longer be accepted
     * Required field that cannot be blank
     */
    @NotBlank(message = "Token is required")
    @Column(name = "token", nullable = false)
    private String token;

    /**
     * Type of the token (e.g., ACCESS, REFRESH)
     * Distinguishes between different types of tokens in the system
     * Required field that cannot be blank
     */
    @NotBlank(message = "Token type is required")
    @Column(name = "token_type", nullable = false)
    private String tokenType;

    /**
     * Time when the token expires and can be removed from the blacklist
     * Used for blacklist cleanup to prevent unlimited growth
     * Required field that cannot be null
     */
    @NotNull(message = "Expiration time is required")
    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;

    /**
     * User who owned this token, if known
     * Optional relationship to track which user's token was invalidated
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Check if token is expired
     *
     * @param checkTime time to check against
     * @return true if expired
     */
    public boolean isExpired(LocalDateTime checkTime) {
        return expirationTime != null && checkTime.isAfter(expirationTime);
    }

    /**
     * Check if token is currently expired
     *
     * @return true if expired
     */
    @Transient
    public boolean isExpired() {
        return isExpired(LocalDateTime.now());
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
     * Validate token state
     *
     * @throws IllegalStateException if validation fails
     */
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

        // Only check future expiration for new tokens (not persisted yet)
        if (getId() == null && expirationTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Expiration time must be in the future");
        }
    }
}
