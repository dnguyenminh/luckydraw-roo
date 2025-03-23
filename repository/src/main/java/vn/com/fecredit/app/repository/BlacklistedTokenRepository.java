package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.CommonStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    
    Optional<BlacklistedToken> findByToken(String token);
    
    List<BlacklistedToken> findByTokenType(String tokenType);
    
    List<BlacklistedToken> findByUserId(Long userId);
    
    List<BlacklistedToken> findByUserIdAndTokenType(Long userId, String tokenType);
    
    List<BlacklistedToken> findByStatus(CommonStatus status);
    
    @Query("SELECT bt FROM BlacklistedToken bt " +
           "WHERE bt.token = :token " +
           "AND bt.tokenType = :tokenType " +
           "AND bt.status = 'ACTIVE' " +
           "AND bt.expirationTime > :currentTime")
    Optional<BlacklistedToken> findValidToken(
        @Param("token") String token,
        @Param("tokenType") String tokenType,
        @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT bt FROM BlacklistedToken bt " +
           "WHERE bt.status = 'ACTIVE' " +
           "AND bt.expirationTime > :currentTime")
    List<BlacklistedToken> findAllValidTokens(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(bt) > 0 FROM BlacklistedToken bt " +
           "WHERE bt.token = :token " +
           "AND bt.tokenType = :tokenType " +
           "AND bt.status = 'ACTIVE' " +
           "AND bt.expirationTime > :currentTime")
    boolean isTokenBlacklisted(
        @Param("token") String token,
        @Param("tokenType") String tokenType,
        @Param("currentTime") LocalDateTime currentTime);
        
    @Modifying
    @Query("DELETE FROM BlacklistedToken bt " +
           "WHERE bt.expirationTime <= :expirationTime")
    void deleteExpiredTokens(@Param("expirationTime") LocalDateTime expirationTime);
}