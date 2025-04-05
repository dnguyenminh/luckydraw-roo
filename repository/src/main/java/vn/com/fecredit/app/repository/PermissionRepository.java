package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Permission;
import vn.com.fecredit.app.entity.enums.CommonStatus;

/**
 * Repository interface for Permission entity.
 */
@Repository
public interface PermissionRepository extends SimpleObjectRepository<Permission> {

    /**
     * Find permission by name
     * @param name the permission name
     * @return optional permission
     */
    Optional<Permission> findByName(String name);
    
    /**
     * Check if permission exists by name
     * @param name the permission name
     * @return true if exists
     */
    boolean existsByName(String name);
    
    /**
     * Find permissions by status
     * @param status the status
     * @return list of permissions
     */
    List<Permission> findByStatus(CommonStatus status);
    
    /**
     * Find permissions by role ID
     * @param roleId the role ID
     * @return list of permissions
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * Find active permissions by role ID
     * @param roleId the role ID
     * @return list of active permissions
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId AND p.status = 'ACTIVE'")
    List<Permission> findActiveByRoleId(@Param("roleId") Long roleId);
    
    /**
     * Find permissions not assigned to a role
     * @param roleId the role ID
     * @return list of permissions not in the role
     */
    @Query("SELECT p FROM Permission p WHERE p NOT IN (SELECT p2 FROM Permission p2 JOIN p2.roles r WHERE r.id = :roleId)")
    List<Permission> findNotInRole(@Param("roleId") Long roleId);
}
