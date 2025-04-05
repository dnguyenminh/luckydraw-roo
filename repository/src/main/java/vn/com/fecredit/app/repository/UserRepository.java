package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends SimpleObjectRepository<User> {
    
    /**
     * Find user by username
     * @param username the username
     * @return optional user
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Check if user exists by username
     * @param username the username
     * @return true if exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Find user by email
     * @param email the email address
     * @return optional user
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if user exists by email
     * @param email the email address
     * @return true if exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by status
     * @param status the status
     * @return list of users
     */
    List<User> findByStatus(CommonStatus status);
    
    /**
     * Find users by role type
     * @param role the role type
     * @return list of users
     */
    List<User> findByRole(RoleType role);
    
    /**
     * Find active users by role type
     * @param role the role type
     * @param status the status
     * @return list of users
     */
    List<User> findByRoleAndStatus(RoleType role, CommonStatus status);
    
    /**
     * Find users with a specific role assigned
     * @param roleId the role ID
     * @return list of users
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);
}