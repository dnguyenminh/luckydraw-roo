package vn.com.fecredit.app.service;

import java.util.List;
import java.util.Optional;

import vn.com.fecredit.app.entity.Role;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.entity.enums.RoleType;

/**
 * Service interface for managing roles in the system.
 * Provides methods for creating, retrieving, and managing roles.
 */
public interface RoleService {
    
    /**
     * Save a role
     * @param role the role to save
     * @return the saved role
     */
    Role save(Role role);
    
    /**
     * Find a role by ID
     * @param id the ID to search for
     * @return the role if found
     */
    Optional<Role> findById(Long id);
    
    /**
     * Find a role by its type
     * @param roleType the role type to search for
     * @return the matching role if found
     */
    Optional<Role> findByRoleType(RoleType roleType);
    
    /**
     * Find all roles
     * @return all roles in the system
     */
    List<Role> findAll();
    
    /**
     * Find roles by status
     * @param status the status to filter by
     * @return roles matching the status
     */
    List<Role> findByStatus(CommonStatus status);
    
    /**
     * Find roles by status, ordered by display order
     * @param status the status to filter by
     * @return ordered list of roles
     */
    List<Role> findByStatusOrdered(CommonStatus status);
    
    /**
     * Check if a role exists by its type
     * @param roleType the role type to check
     * @return true if exists
     */
    boolean existsByRoleType(RoleType roleType);
    
    /**
     * Delete a role
     * @param role the role to delete
     */
    void delete(Role role);
    
    /**
     * Delete a role by ID
     * @param id the ID of the role to delete
     */
    void deleteById(Long id);
    
    /**
     * Find roles by user ID
     * @param userId the user ID to search for
     * @return roles assigned to the user
     */
    List<Role> findByUserId(Long userId);
    
    /**
     * Find roles that have a specific permission
     * @param permissionName the permission name to search for
     * @return roles with the permission
     */
    List<Role> findByPermissionName(String permissionName);
}
