package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleName;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByStatus(CommonStatus status);
    
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.enabled = true " +
           "AND u.accountExpired = false AND u.accountLocked = false " +
           "AND u.credentialsExpired = false")
    List<User> findActiveUsers();
    
    @Query("SELECT u FROM User u JOIN u.roles r " +
           "WHERE r.roleName = :roleName AND r.status = 'ACTIVE' " +
           "AND u.status = 'ACTIVE' AND u.enabled = true")
    List<User> findActiveUsersByRole(@Param("roleName") RoleName roleName);
    
    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.roles r " +
           "WHERE u.id = :userId AND r.roleName = :roleName " +
           "AND r.status = 'ACTIVE' AND u.status = 'ACTIVE'")
    boolean hasRole(@Param("userId") Long userId, @Param("roleName") RoleName roleName);
}