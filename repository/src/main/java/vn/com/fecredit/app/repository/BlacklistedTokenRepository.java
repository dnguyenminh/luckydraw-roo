package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Repository interface for BlacklistedToken entities
 */
@Repository
public interface BlacklistedTokenRepository extends SimpleObjectRepository<BlacklistedToken> {

    /**
     * Find blacklisted token by token value
     * @param token token value
     * @return optional containing the token if found
     */
    Optional<BlacklistedToken> findByToken(String token);
    
    /**
     * Check if a token exists and is active
     * @param token the token string
     * @param status the token status
     * @return true if token is blacklisted with the given status
     */
    boolean existsByTokenAndStatus(String token, CommonStatus status);
    
    /**
     * Find tokens by status
     * @param status the status
     * @return list of tokens
     */
    List<BlacklistedToken> findByStatus(CommonStatus status);
    
    /**
     * Delete expired tokens
     * @param currentTime current time
     * @return number of tokens deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expirationTime < :currentTime")
    int deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find tokens by user ID
     * @param userId the user ID
     * @return list of tokens
     */
    List<BlacklistedToken> findByUserId(Long userId);
    
    /**
     * Find active tokens that are not expired
     * @param currentTime current time
     * @return list of valid tokens
     */
    @Query("SELECT bt FROM BlacklistedToken bt WHERE bt.status = 'ACTIVE' AND bt.expirationTime > :currentTime")
    List<BlacklistedToken> findValidTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find tokens by token type
     * @param tokenType the token type
     * @return list of tokens
     */
    List<BlacklistedToken> findByTokenType(String tokenType);
    
    /**
     * Find valid token by token value and type
     * @param token the token value
     * @param tokenType the token type
     * @param currentTime the current time
     * @return optional token
     */
    @Query("SELECT bt FROM BlacklistedToken bt WHERE bt.token = :token AND bt.tokenType = :tokenType AND bt.expirationTime > :currentTime")
    Optional<BlacklistedToken> findValidToken(@Param("token") String token, @Param("tokenType") String tokenType, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find all valid tokens
     * @param currentTime the current time
     * @return list of valid tokens
     */
    @Query("SELECT bt FROM BlacklistedToken bt WHERE bt.expirationTime > :currentTime")
    List<BlacklistedToken> findAllValidTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Check if token is blacklisted
     * @param token the token value
     * @param tokenType the token type
     * @param currentTime the current time
     * @return true if token is blacklisted
     */
    @Query("SELECT COUNT(bt) > 0 FROM BlacklistedToken bt WHERE bt.token = :token AND bt.tokenType = :tokenType AND bt.expirationTime > :currentTime AND bt.status = 'ACTIVE'")
    boolean isTokenBlacklisted(@Param("token") String token, @Param("tokenType") String tokenType, @Param("currentTime") LocalDateTime currentTime);
}