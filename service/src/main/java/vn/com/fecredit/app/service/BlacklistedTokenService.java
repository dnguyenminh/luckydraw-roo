package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.service.base.AbstractService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BlacklistedTokenService extends AbstractService<BlacklistedToken> {
    Optional<BlacklistedToken> findByToken(String token);
    List<BlacklistedToken> findByTokenType(String tokenType);
    List<BlacklistedToken> findByUserId(Long userId);
    Optional<BlacklistedToken> findValidToken(String token, String tokenType, LocalDateTime currentTime);
    List<BlacklistedToken> findAllValidTokens(LocalDateTime currentTime);
    boolean isTokenBlacklisted(String token, String tokenType, LocalDateTime currentTime);
    void deleteExpiredTokens(LocalDateTime expirationTime);
}
